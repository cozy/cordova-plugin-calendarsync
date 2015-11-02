package io.cozy.calendarsync;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentValues;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;


public class Tools {
    private static final String LOG_TAG = "Tools";

    // Make the class static.
    private Tools() {}

    /**
     * Append parameters to the specified content URI to act as a sync adapter.
     *
     * @param uri the content uri
     * @param accountType account type of the sync adapter
     * @param accountName account name of the sync adapter
     * @return the Content URI with sync adapters parameters.
     */
    static Uri asSyncAdapter(Uri uri, String accountType, String accountName) {
        return uri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true")
            .appendQueryParameter(Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }


    /**
     * Convert rows (in a cursor) to a JSONArray.
     *
     * @param c a brand new cursor, result of a query.
     * @param columnNames the columns names to extract data from the cursor,
     *        and build JSONObjects.
     * @return a JSONArray, (empty if c is empty).
     */
    public static JSONArray rows2JSONArray(Cursor c, String[] columnNames) {
        JSONArray result = new JSONArray();

        if (c.moveToFirst()) {
            do {
                result.put(row2JSON(c, columnNames));
            } while (c.moveToNext());
        }
        return result;
    }


    /**
     * Convert a row (in a cursor) to a JSONObject.
     *
     * @param c a cursor, moved on the row to extract.
     * @param columnNames the columns names to extract data from the cursor,
     *        and build JSONObject properties.
     * @return a JSONObject, where properties name are the rowNames.
     */
    public static JSONObject row2JSON(Cursor c, String[] columnNames) {
        JSONObject event = new JSONObject();
        for (String name : columnNames) {
            int index = c.getColumnIndex(name);
            if (index < 0) { // No such column in the row.
                continue;
            }
            try {
                switch(c.getType(index)) {
                    case Cursor.FIELD_TYPE_NULL: break; // pass
                    case Cursor.FIELD_TYPE_INTEGER:
                        event.put(name, c.getLong(index));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                            event.put(name, c.getDouble(index));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                            event.put(name, c.getString(index));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        Log.e(LOG_TAG, "Unhandled Blob field.");
                        break; // pass
                }
            } catch (JSONException e) { Log.w(LOG_TAG, e); }
        }
        return event;
    }


    /**
     * Convert a JSONObject to databses row to insert (as a ContentValues).
     *
     * @param o the json object to convert.
     * @param columnNames the list of properties (which are columns names) to
     *        extract from object and put in the row.
     * @return the content values ready to use with insert or update.
     */
    public static ContentValues json2Row(JSONObject o, String[] columnNames) {
        ContentValues result = new ContentValues();
        for (String name: columnNames) {

            Object v = o.opt(name);
            if (v == null) {
                // result.putNull(name);
                continue;
            } else if (v instanceof String) {
                result.put(name, (String)v);
            } else if (v instanceof Boolean) {
                result.put(name, ((Boolean)v).booleanValue());
            } else if (v instanceof Integer || v instanceof Long) {
                result.put(name, ((Number)v).longValue());
            } else if (v instanceof Float || v instanceof Double) {
                result.put(name, ((Number)v).doubleValue());
            } else if (JSONObject.NULL.equals(v)) {
                // result.putNull(name);
                continue;

            } else { // JSONArray, JSONObject ...
                Log.w(LOG_TAG, "Unexpected JSONArray, JSONObject, ...");
            }
        }
        return result;
    }
}
