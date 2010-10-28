package info.voidptr.nomads;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseUpdater {
	SQLiteDatabase conn;
	private final String TAG = "DatabaseUpdater";
	
    public DatabaseUpdater(SQLiteDatabase conn) {
    	this.conn = conn;
    }
    
    public void updateSuggestion(JSONObject suggestion) throws JSONException {
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
    
    public void updatePost(JSONObject post) throws JSONException {
    	
    	Log.d(TAG, "Parsing post: " + post.toString());
    	
		String id = post.getString("id");
		String lat = post.getString("lat");
		String lon = post.getString("lon");
		String created_at = post.getString("created_at");
		String updated_at = post.getString("updated_at");
		String title = post.getString("title");
		String content = post.getString("content");
		String user_id = post.getString("user_id");

		String sql = "INSERT INTO posts"
		       + "(_id, lat, lon, created_at, updated_at, title, content, user_id) "
		       + "values ("
		       + id + ", "
		       + lat + ", "
		       + lon + ", "
		       + "'" + created_at + "', "
		       + "'" + updated_at + "', "
		       + "'" + title.replace("'", "''") + "', "
		       + "'" + content.replace("'", "''") + "', "
		       + user_id
		       + ")";
		Log.i(TAG, "inserting post " + title);
		conn.execSQL(sql);
    }
    
    public void updateComment(JSONObject comment) throws JSONException {
		String id = comment.getString("id");
		String created_at = comment.getString("created_at");
		String updated_at = comment.getString("updated_at");
		String body = comment.getString("body");
		String position_type = comment.getString("position_type");
		String position_id = comment.getString("position_id");
		String user_id = comment.getString("user_id");

		String sql = "INSERT INTO comments"
		       + "(_id, created_at, updated_at, body, position_type, position_id, user_id) "
		       + "values ("
		       + id + ", "
		       + "'" + created_at + "', "
		       + "'" + updated_at + "', "
		       + "'" + body.replace("'", "''") + "', "
		       + "'" + position_type.replace("'", "''") + "', "
		       + position_id + ", "
		       + user_id
		       + ")";
		Log.i(TAG, "inserting comment " + id);
		conn.execSQL(sql);
    }
    
    public void updateUser(JSONObject user) throws JSONException {
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
    
    public void close() {
    	conn.close();
    }
}
