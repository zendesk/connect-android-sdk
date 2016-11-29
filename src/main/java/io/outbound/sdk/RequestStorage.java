package io.outbound.sdk;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;

class RequestStorage extends SQLiteOpenHelper {

    public static String DATABASE_NAME = BuildConfig.APPLICATION_ID;
    public static int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "requests";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_REQUEST = "request";
    public static final String COLUMN_NAME_PAYLOAD = "payload";
    public static final String COLUMN_NAME_GUID = "guid";
    public static final String COLUMN_NAME_ATTEMPTS = "attempts";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME_REQUEST + " TEXT," +
                    COLUMN_NAME_PAYLOAD + " TEXT," +
                    COLUMN_NAME_GUID + " TEXT," +
                    COLUMN_NAME_ATTEMPTS + " INTEGER" +
                    " )";

    public RequestStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    synchronized long add(OutboundRequest request) {
        long result = -1;
        try {
            SQLiteDatabase db = getWritableDatabase();
            result = db.insert(TABLE_NAME, null, request.content());
        } catch (SQLiteException e) {
            // TODO how do we handle this?
        } finally {
            close();
        }
        return result;
    }

    synchronized JSONArray getRequests() {
        JSONArray requests = new JSONArray();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(
                    TABLE_NAME,
                    new String[]{
                            COLUMN_NAME_ID,
                            COLUMN_NAME_REQUEST,
                            COLUMN_NAME_PAYLOAD,
                            COLUMN_NAME_GUID,
                            COLUMN_NAME_ATTEMPTS
                    },
                    null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                OutboundRequest.Type type = OutboundRequest.Type.fromString(cursor.getString(1));
                String payload = cursor.getString(2);
                String guid = cursor.getString(3);
                int attempts = cursor.getInt(4);
                requests.put(new OutboundRequest(id, type, payload, guid, attempts));
            }
        } catch (SQLiteException e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return requests;
    }

    synchronized void remove(long id) throws SQLiteException {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME, COLUMN_NAME_ID + "=" + id, null);
        } catch (SQLiteException e) {
            throw e;
        } finally {
            close();
        }
    }
}