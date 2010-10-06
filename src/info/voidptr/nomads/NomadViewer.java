package info.voidptr.nomads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NomadViewer extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        updateText();
    }

	private void updateText() {
		TextView t = (TextView)findViewById(R.id.display);
		t.setText("Connecting to nomad.heroku.com");

		AndroidHttpClient client = AndroidHttpClient.newInstance("Nomad App");
		HttpRequest plainActivity = new HttpGet("http://nomad.heroku.com/activity/plain.json");
		try {
			HttpResponse res = client.execute((HttpUriRequest) plainActivity);
			t.setText("Got statuses...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
			String actions = reader.readLine();
			Log.i("NomadViewer", "Actions: " + actions);
			t.setText(actions);
		} catch (IOException e) {
			t.setText("Failure: " + e.getClass().getName() + " -- " + e.getMessage());
			Log.w("NomadViewer", e.getMessage());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateText();
	}
    
}