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

import android.app.ListActivity;
import android.content.Intent;
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
	private static String POSTS_URL = "http://nomad.heroku.com/posts.json";
	private static String COMMENTS_URL = "http://nomad.heroku.com/comments.json";
	
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
	
	/**
	 * Handles list item click events
	 */
	@Override
	public void onListItemClick(ListView parent, View v, int i, long id) {
		Log.i(TAG, "Opening url: " + links[i]);
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(links[i]));
		startActivity(browserIntent);
	}

	/**
	 * Fetches db updates in the background. 
	 */
	private void fetchUpdates() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "Fetching activity from " + ACTIVITY_URL);
				AndroidHttpClient client = AndroidHttpClient.newInstance("Nomad App");
				DatabaseUpdater updater = new DatabaseUpdater(db.getWritableDatabase());

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
				long age = time - lastUpdate;
				lastUpdate = time;
				
				if(age < 10000) {
					Log.i(TAG, "Not fetching updates, age is only " + age);
					return;
				}
				else {
					Log.i(TAG, "Fetching suggestion and user updates, age is " + age);
				}
	
				// Fetch Users table
				try {
					String ts = "?since=" + updater.getMostRecentTimestamp("users");
					String url = USERS_URL + (ts.equals("?since=null") ? "" : ts);
					Log.i(TAG, "Fetching users from " + url);
					
					HttpResponse res = client.execute((HttpUriRequest) new HttpGet(url));
					BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
					StringBuffer content_buffer = new StringBuffer();
					String line = reader.readLine();
					while(line != null) {
						content_buffer.append(line + "\n");
						line = reader.readLine();
					}
					updater.handleJson(content_buffer.toString());
					Log.i(TAG, "Fetched Users table");
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
	
				// Fetch Suggestions table
				try {
					String ts = "?since=" + updater.getMostRecentTimestamp("suggestions");
					String url = SUGGESTIONS_URL + (ts.equals("?since=null") ? "" : ts);
					Log.i(TAG, "Fetching suggestions from " + url);

					HttpResponse res = client.execute((HttpUriRequest) new HttpGet(url));
					BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()), 4000);
					StringBuffer content_buffer = new StringBuffer();
					String line = reader.readLine();
					while(line != null) {
						content_buffer.append(line + "\n");
						line = reader.readLine();
					}
					updater.handleJson(content_buffer.toString());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				
				// Fetch Posts table
				try {
					String ts = "?since=" + updater.getMostRecentTimestamp("posts");
					String url = POSTS_URL + (ts.equals("?since=null") ? "" : ts);
					Log.i(TAG, "Fetching posts from " + url);

					HttpResponse res = client.execute((HttpUriRequest) new HttpGet(url));
					BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()), 4000);
					StringBuffer content_buffer = new StringBuffer();
					String line = reader.readLine();
					while(line != null) {
						content_buffer.append(line + "\n");
						line = reader.readLine();
					}
					updater.handleJson(content_buffer.toString());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				
				// Fetch Comments table
				try {
					String ts = "?since=" + updater.getMostRecentTimestamp("comments");
					String url = COMMENTS_URL + (ts.equals("?since=null") ? "" : ts);
					Log.i(TAG, "Fetching comments from " + url);

					HttpResponse res = client.execute((HttpUriRequest) new HttpGet(url));
					BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()), 4000);
					StringBuffer content_buffer = new StringBuffer();
					String line = reader.readLine();
					while(line != null) {
						content_buffer.append(line + "\n");
						line = reader.readLine();
					}
					updater.handleJson(content_buffer.toString());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
	
				client.close();
			} 
		}).start();
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

	/**
	 * Shows actions in a listview. Can be called from any thread. 
	 * @param txt
	 */
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