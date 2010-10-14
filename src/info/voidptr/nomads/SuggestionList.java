package info.voidptr.nomads;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SuggestionList extends ListActivity {
	Handler handler;
	DatabaseHelper db;
	private final String TAG = "SuggestionList";
	private Suggestion[] suggestions;
	private Location currentLocation;
	
	private class Suggestion {
		public int id;
		public String title;
//		public String content;
//		public int iconId;
//		public String username;
		public Location location;
		private float distanceFromHere = -1.0f;
		
		String link() {
			return "http://nomad.heroku.com/suggestions/" + id;
		}
		
		float distance() {
			if(distanceFromHere < 0) {
				distanceFromHere = currentLocation.distanceTo(location);
			}
			return distanceFromHere;
		}
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        handler = new Handler();
        db = new DatabaseHelper(this);
        setContentView(R.layout.suggestions);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		updateLocation();
		fetchAndShowSuggestions();
	}
	
	private void updateLocation() {
		Criteria crit=new Criteria();
		crit.setCostAllowed(true);
		crit.setSpeedRequired(false);
		crit.setBearingRequired(false);
		crit.setAltitudeRequired(false);
		
		LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String bestProvider = mgr.getBestProvider(crit, true);
		if(bestProvider == null) {
			Log.w(TAG, "Couldn't find location");
			for(String p : mgr.getAllProviders())
				Log.i(TAG, "Provider option: " + p);
			currentLocation = new Location("NoIdeaProvider");
		}
		else {
			currentLocation = mgr.getLastKnownLocation(bestProvider);
		}
	}

	@Override
	public void onListItemClick(ListView parent, View v, int i, long id) {
		String link = suggestions[i].link();
		Log.i(TAG, "Opening url: " + link);
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(link));
		startActivity(browserIntent);
	}
	
	private void fetchAndShowSuggestions() {
		SQLiteDatabase conn = db.getReadableDatabase();
		Cursor cur = conn.rawQuery("SELECT suggestions._id as id, title, content, lat, lon, icon_id, users.name as name" +
								   "  FROM suggestions INNER JOIN users" +
								   "  ON suggestions.user_id = users._id",
								   new String[] {});

		List<Suggestion> my_suggestions = new LinkedList<Suggestion>();
		int count = cur.getCount();
		
		for(int i = 0; i < count; i++) {
			cur.moveToPosition(i);
			Suggestion s = new Suggestion();
			s.id = cur.getInt(0);
			s.title = cur.getString(1);
			//s.content = cur.getString(2);
			Location l = new Location("Nomads");
			l.setLatitude(cur.getFloat(3));
			l.setLongitude(cur.getFloat(4));
			s.location = l;
			//s.iconId = cur.getInt(5);
			//s.username = cur.getString(6);
			
			my_suggestions.add(s);
		}
		Collections.sort(my_suggestions, new Comparator<Suggestion>() {
			@Override
			public int compare(Suggestion a, Suggestion b) {
				return Float.compare(a.distance(), b.distance());
			}
		});
		
		suggestions = new Suggestion[count];
		String[] txt = new String[count];
		int i = 0;
		for(Suggestion s : my_suggestions) {
			suggestions[i] = s;
			txt[i] = s.title;
			i++;
		}
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, txt));
	}
}
