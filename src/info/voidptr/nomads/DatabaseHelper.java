package info.voidptr.nomads;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Suggestions table
 */
final class Suggestions implements BaseColumns {
    // This class cannot be instantiated
    private Suggestions() {}
    public static final String DEFAULT_SORT_ORDER = "modified DESC";
    
    // Columns
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String ICON_ID = "icon_id";
    public static final String USER_ID = "user_id";
}

/**
 * Users table
 */
final class Users implements BaseColumns {
    // This class cannot be instantiated
    private Users() {}
    public static final String DEFAULT_SORT_ORDER = "_id DESC";

    // Columns
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String FULLNAME = "fullname";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String ADMIN = "admin";
}

/**
 * Posts table
 */
final class Posts implements BaseColumns {
    // This class cannot be instantiated
    private Posts() {}
    public static final String DEFAULT_SORT_ORDER = "_id DESC";

    // Columns
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String USER_ID = "user_id";
}

/**
 * Comments table
 */
final class Comments implements BaseColumns {
    // This class cannot be instantiated
    private Comments() {}
    public static final String DEFAULT_SORT_ORDER = "_id DESC";

    // Columns
    public static final String BODY = "body";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String POSITION_TYPE = "position_type";
    public static final String POSITION_ID = "position_id";
    public static final String USER_ID = "user_id";
}

public class DatabaseHelper extends SQLiteOpenHelper {
	private static String DATABASE_NAME = "nomad_db";
	private static int DATABASE_VERSION = 4;
	private static String TAG = "NomadViewerDB";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
        		+ Users._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE,"
        		+ Users.NAME + " VARCHAR(255),"
        	    + Users.EMAIL + " VARCHAR(255),"
        	    + Users.FULLNAME + " VARCHAR(255),"
        	    + Users.CREATED_AT + " TIMESTAMP,"
        	    + Users.UPDATED_AT + " TIMESTAMP,"
        	    + Users.ADMIN + " BOOLEAN"
                + ");");

        db.execSQL("CREATE TABLE suggestions ("
                + Suggestions._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE,"
                + Suggestions.TITLE + " VARCHAR(255),"
                + Suggestions.CONTENT + " TEXT,"
                + Suggestions.LAT + " FLOAT,"
                + Suggestions.LON + " FLOAT,"
                + Suggestions.CREATED_AT + " TIMESTAMP,"
                + Suggestions.UPDATED_AT + " TIMESTAMP,"
                + Suggestions.ICON_ID + " INTEGER,"
                + Suggestions.USER_ID + " INTEGER"
                + ");");
        
        db.execSQL("CREATE TABLE posts (" // TODO: Fix this schema
                + Posts._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE,"
                + Posts.TITLE + " VARCHAR(255),"
                + Posts.CONTENT + " TEXT,"
                + Posts.LAT + " FLOAT,"
                + Posts.LON + " FLOAT,"
                + Posts.CREATED_AT + " TIMESTAMP,"
                + Posts.UPDATED_AT + " TIMESTAMP,"
                + Posts.USER_ID + " INTEGER"
                + ");");
        
        db.execSQL("CREATE TABLE comments ("
                + Comments._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE,"
                + Comments.BODY + " TEXT,"
                + Comments.CREATED_AT + " TIMESTAMP,"
                + Comments.UPDATED_AT + " TIMESTAMP,"
                + Comments.POSITION_ID + " INTEGER,"
                + Comments.POSITION_TYPE + " VARCHAR(255),"
                + Comments.USER_ID + " INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS suggestions");
        onCreate(db);
    }
}