package info.voidptr.nomads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class NomadViewer extends Activity {
	public static final String TAG = "NomadViewer";
	private static String ACTIVITY_URL = "http://nomad.heroku.com/activity/plain.json";
	private static String USERS_URL = "http://nomad.heroku.com/users.json";
	private static String SUGGESTIONS_URL = "http://nomad.heroku.com/suggestions.json";

	Handler handler;
	DatabaseHelper db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        db = new DatabaseHelper(this);
        setContentView(R.layout.main);
        fetchUpdates();
    }

	private void fetchUpdates() {
		new Thread(new Runnable() { @Override
		public void run() {
			showText("Fetching activity from " + ACTIVITY_URL);
			AndroidHttpClient client = AndroidHttpClient.newInstance("Nomad App");

			// Fetch recent activity
			try {
				HttpResponse res = client.execute((HttpUriRequest) new HttpGet(ACTIVITY_URL));
				showText("Got statuses...");
				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
				String actions = reader.readLine();

				Log.i(TAG, "Actions: " + actions);
				showText(formatActions(actions));
			} catch (IOException e) {
				showText("Failure fetching recent activity: " + e.getClass().getName() + " -- " + e.getMessage());
				Log.w(TAG, e.getMessage());
			}

			// Fetch Users table
			try {
				HttpResponse res = client.execute((HttpUriRequest) new HttpGet(USERS_URL));
				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
				StringBuffer content_buffer = new StringBuffer();
				String line = reader.readLine();
				while(line != null) {
					content_buffer.append(line + "\n");
					line = reader.readLine();
				}
				String json_str = content_buffer.toString();
				json_str = json_str + "";

				Log.i(TAG, "Fetched Users table");
			} catch (IOException e) {
				showText("Failure fetching users: " + e.getClass().getName() + " -- " + e.getMessage());
				Log.w(TAG, e.getMessage());
			}

			// Fetch Suggestions table
			try {
				HttpResponse res = client.execute((HttpUriRequest) new HttpGet(SUGGESTIONS_URL));
				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
				StringBuffer content_buffer = new StringBuffer();
				String line = reader.readLine();
				while(line != null) {
					content_buffer.append(line + "\n");
					line = reader.readLine();
				}
				String json_str = content_buffer.toString();
				updateSuggestions(json_str);
			} catch (IOException e) {
				showText("Failure: " + e.getClass().getName() + " -- " + e.getMessage());
				Log.w(TAG, e.getMessage());
			}

			client.close();
		} }).start();
	}
	
	private List<Map<String,String>> updateSuggestions(String json) {
		List<Map<String,String>> ret = new LinkedList<Map<String,String>>();
		SQLiteDatabase conn = db.getWritableDatabase();

		try {
			JSONArray array = new JSONArray(json);
			for(int i = 0; i < array.length(); i++) {
				JSONObject suggestion = array.getJSONObject(i).getJSONObject("suggestion");

				String id = suggestion.getString("id");
				String lat = suggestion.getString("lat");
				String lon = suggestion.getString("lon");
				String created_at = suggestion.getString("created_at");
				String updated_at = suggestion.getString("updated_at");
				String icon_id = suggestion.getString("icon_id");
				String title = suggestion.getString("title");
				String content = suggestion.getString("content");
				String user_id = suggestion.getString("user_id");

				String sql = "INSERT INTO suggestions"
					       + "(_id, lat, lon, created_at, updated_at, icon_id, title, content, user_id) "
			               + "values ("
			               + id + ", "
			               + lat + ", "
			               + lon + ", "
			               + "'" + created_at + "', "
			               + "'" + updated_at + "', "
			               + icon_id + ", "
			               + "'" + title.replace("'", "''") + "', "
			               + "'" + content.replace("'", "''") + "', "
			               + user_id
			               + ")";
				Log.i("inserting", title);
				conn.execSQL(sql);
				Log.i("inserted", title);
			}
		} catch (JSONException e) {
			Log.w(TAG, "Failed to parse or insert suggestion: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		finally {
			conn.close();
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private String updateUsers(String json) {
		String ret = "<error parsing suggestions>";
		try {
			JSONArray array = new JSONArray(json);
			ret = "";
			for(int i = 0; i < array.length(); i++) {
				JSONObject user = array.getJSONObject(i);
			}
		} catch (JSONException e) {
			Log.w(TAG, "Failed to parse actions: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		return ret;
	}

	private String formatActions(String actions) {
		String ret = "<error parsing actions>";
		try {
			JSONArray json = new JSONArray(actions);
			ret = "";
			for(int i = 0; i < json.length(); i++) {
				ret = ret + json.getString(i) + "\n\n";
			}
		} catch (JSONException e) {
			Log.w(TAG, "Failed to parse actions: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		return ret;
	}

	private void showText(final String txt) {
		// Don't you just love Java? 
		Runnable r = new Runnable() {
			@Override
			public void run() {
				TextView display = (TextView) findViewById(R.id.display);
				display.setText(txt);
			}
		};
		handler.post(r);
	}

	@Override
	protected void onResume() {
		super.onResume();
		fetchUpdates();
	}
}