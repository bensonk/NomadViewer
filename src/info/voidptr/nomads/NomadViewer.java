package info.voidptr.nomads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NomadViewer extends ListActivity {
	public static final String TAG = "NomadViewer";
	private static final int SUGGESTIONS_ID = Menu.FIRST + 10;
	private static String ACTIVITY_URL = "http://nomad.heroku.com/activity/plain.json";
	private static String USERS_URL = "http://nomad.heroku.com/users.json";
	private static String SUGGESTIONS_URL = "http://nomad.heroku.com/suggestions.json";
	
	private String[] links;
	
	// For later:
	// private static String POSTS_URL = "http://nomad.heroku.com/posts.json";
	// private static String COMMENTS_URL = "http://nomad.heroku.com/comments.json";

	Handler handler;
	DatabaseHelper db;
	long lastUpdate = 0;
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        handler = new Handler();
        db = new DatabaseHelper(this);
        setContentView(R.layout.main);
        fetchUpdates();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		m.add(Menu.NONE, SUGGESTIONS_ID, Menu.NONE, "Suggestions");
		return super.onCreateOptionsMenu(m);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem m) {
		
		return handleMenuSelection(m) || super.onOptionsItemSelected(m);
	}
	
	private boolean handleMenuSelection(MenuItem m) {
		switch(m.getItemId()) {
		case SUGGESTIONS_ID:
			startActivity(new Intent(this, SuggestionList.class));
			return true;
		}
		return false;
	}
	
	@Override
	public void onListItemClick(ListView parent, View v, int i, long id) {
		Log.i(TAG, "Opening url: " + links[i]);
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(links[i]));
		startActivity(browserIntent);
	}

	private void fetchUpdates() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "Fetching activity from " + ACTIVITY_URL);
				AndroidHttpClient client = AndroidHttpClient.newInstance("Nomad App");

				// Fetch recent activity
				try {
					HttpResponse res = client.execute((HttpUriRequest) new HttpGet(ACTIVITY_URL));
					BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
					String actions = reader.readLine();
	
					Log.i(TAG, "Actions: " + actions);
					parseAndShowActions(actions);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				
				long time = Calendar.getInstance().getTimeInMillis();
				long age = lastUpdate - time;
				lastUpdate = time;
				if(age < 10000) return;
	
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
					updateUsers(json_str);
					Log.i(TAG, "Fetched Users table");
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
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
					Log.e(TAG, e.getMessage());
				}
	
				client.close();
			} 
		}).start();
	}

	private void updateSuggestions(String json) {
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
				Log.i(TAG, "inserting suggestion " + title);
				conn.execSQL(sql);
			}
		} catch (JSONException e) {
			Log.w(TAG, "Failed to parse or insert suggestion: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		catch (IllegalStateException e) {
			Log.w(TAG, "Failure parsing suggestions: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		conn.close();
	}

	private void updateUsers(String json) {
		SQLiteDatabase conn = db.getWritableDatabase();
		try {
			JSONArray array = new JSONArray(json);
			for(int i = 0; i < array.length(); i++) {
				JSONObject user = array.getJSONObject(i);
				String id = user.getString("id");
				String name = user.getString("name");
				String created_at = user.getString("created_at");
				String updated_at = user.getString("updated_at");
				String fullname = user.getString("fullname");
				String admin = (user.getString("admin").compareTo("true") == 0) ? "1" : "0";

				String sql = "INSERT INTO users"
				           + "(_id, name, created_at, updated_at, fullname, admin) "
		                   + "values ("
		                   + id + ", "
		                   + "'" + name + "', "
			               + "'" + created_at + "', "
			               + "'" + updated_at + "', "
			               + "'" + fullname + "', "
		                   + admin
		                   + ")";
				Log.i(TAG, "inserting user " + name);
				conn.execSQL(sql);
			}
		} catch (JSONException e) {
			Log.w(TAG, "Failed to parse actions: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		catch (IllegalStateException e) {
			Log.w(TAG, "Failure parsing users: " + e.getClass().getName() + " -- " + e.getMessage());
		}
		conn.close();
	}

	private void parseAndShowActions(String actions) {
		String[] items = new String[] { "<error parsing actions>" };
		try {
			JSONArray json = new JSONArray(actions);
			items = new String[json.length()];
			links = new String[json.length()];
			for(int i = 0; i < json.length(); i++) {
				JSONArray pair = json.getJSONArray(i);
				items[i] = pair.getString(0);
				links[i] = pair.getString(1);
			}
			showActions(items);
		} catch (JSONException e) {
			Log.w(TAG, "Failed to parse actions: " + e.getClass().getName() + " -- " + e.getMessage());
		}
	}

	private void showActions(final String[] txt) {
		// Don't you just love Java? 
		final ListActivity parent = this;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				setListAdapter(new ArrayAdapter<String>(parent, android.R.layout.simple_list_item_1, txt));
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