package info.voidptr.nomads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class NomadViewer extends Activity {
	Handler handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();  
        setContentView(R.layout.main);
        updateText();
    }

	private void updateText() {
		new Thread(new Runnable() { @Override
		public void run() {
			String url = "http://nomad.heroku.com/activity/plain.json";
			showText("Connecting to " + url);
			AndroidHttpClient client = AndroidHttpClient.newInstance("Nomad App");
			HttpRequest plainActivity = new HttpGet(url);
			try {
				HttpResponse res = client.execute((HttpUriRequest) plainActivity);
				showText("Got statuses...");
				BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
				String actions = reader.readLine();
				
				Log.i("NomadViewer", actions);
				showText(formatActions(actions));
			} catch (IOException e) {
				showText("Failure: " + e.getClass().getName() + " -- " + e.getMessage());
				Log.w("NomadViewer", e.getMessage());
			}
		} }).start();
	}
	
	private String formatActions(String actions) {
		String ret = "<error parsing suggestions>";
		try {
			JSONArray json = new JSONArray(actions);
			ret = "";
			for(int i = 0; i < json.length(); i++) {
				ret = ret + json.getString(i) + "\n\n";
			}
		} catch (JSONException e) {
			Log.w("NomadViewer", "Failed to parse actions: " + e.getClass().getName() + " -- " + e.getMessage());
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
		updateText();
	}
    
}