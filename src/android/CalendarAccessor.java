package io.cozy.calendarsync;

import android.util.Log;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentResolver;

import android.provider.CalendarContract.Calendars;

import org.apache.cordova.CordovaInterface;


public class CalendarAccessor {
    static final String LOG_TAG = "CalendarAccessor";
    CordovaInterface app;
    Context context;

    private static final String[] CALENDAR_COLUMNS = new String[] {
        Calendars._ID,
        Calendars.NAME,
        Calendars.CALENDAR_DISPLAY_NAME,
        Calendars.VISIBLE,
        Calendars.SYNC_EVENTS,
        Calendars.ACCOUNT_NAME,
        Calendars.ACCOUNT_TYPE,
        Calendars.CALENDAR_COLOR,
        Calendars._SYNC_ID,
        Calendars.DIRTY,
        Calendars.MUTATORS,
        Calendars.OWNER_ACCOUNT,
        Calendars.MAX_REMINDERS,
        Calendars.ALLOWED_REMINDERS,
        Calendars.ALLOWED_AVAILABILITY,
        Calendars.ALLOWED_ATTENDEE_TYPES,
        Calendars.CAN_MODIFY_TIME_ZONE,
        Calendars.CAN_ORGANIZER_RESPOND,
        Calendars.CAN_PARTIALLY_UPDATE,
        Calendars.CALENDAR_LOCATION,
        Calendars.CALENDAR_TIME_ZONE,
        Calendars.CALENDAR_ACCESS_LEVEL,
        Calendars.DELETED,
        Calendars.CAL_SYNC1,
        Calendars.CAL_SYNC2,
        Calendars.CAL_SYNC3,
        Calendars.CAL_SYNC4,
        Calendars.CAL_SYNC5,
        Calendars.CAL_SYNC6,
        Calendars.CAL_SYNC7,
        Calendars.CAL_SYNC8,
        Calendars.CAL_SYNC9,
        Calendars.CAL_SYNC10
    };

    /* Singleton pattern */
    private static CalendarAccessor instance = null;

    public static void initInstance(CordovaInterface application) {
        if (instance == null) {
            instance = new CalendarAccessor();
            instance.app = application;
            instance.context = application.getActivity();
        }
    }

    public static CalendarAccessor getInstance() {
        return instance;
    }

    private CalendarAccessor() {}
    /* End singleton pattern. */


    /** Fetch all calendars of the specified account */
    public JSONArray getCalendars(String accountType, String accountName) {
        Cursor c = context.getContentResolver().query(
            Calendars.CONTENT_URI,
            CALENDAR_COLUMNS,
            "( " + Calendars.ACCOUNT_NAME + " = ? ) AND ( " +
            Calendars.ACCOUNT_TYPE + " = ? )",
            new String[] { accountName, accountType },
            null
        );
        Log.d(LOG_TAG, "GetCalendars, after cursor.");
        // Convert to JSON.
        return Tools.rows2JSONArray(c, CALENDAR_COLUMNS);
    }


    public String addCalendar(JSONObject calendar,
                    String accountType, String accountName) {

        Uri uri = context.getContentResolver().insert(
            Tools.asSyncAdapter(Calendars.CONTENT_URI, accountType, accountName),
            Tools.json2Row(calendar, CALENDAR_COLUMNS)
        );
        return uri.getLastPathSegment();
    }

    public void updateCalendar(JSONObject calendar,
                    String accountType, String accountName) {

        try {
            context.getContentResolver().update(
                Tools.asSyncAdapter(Calendars.CONTENT_URI,
                                    accountType, accountName),
                Tools.json2Row(calendar, CALENDAR_COLUMNS),
                Calendars._ID + " = ? ",
                new String[] { String.valueOf(calendar.get(Calendars._ID)) }
            );
        } catch (JSONException e) { Log.w(LOG_TAG, e); }
    }


    public int deleteCalendar(JSONObject calendar,
                    String accountType, String accountName) {

        try {
            ContentResolver cr = context.getContentResolver();
            return cr.delete(
                Tools.asSyncAdapter(Calendars.CONTENT_URI, accountType, accountName),
                Calendars._ID + " = ? ",
                new String[] {String.valueOf(calendar.getLong(Calendars._ID))}
            );
        } catch (JSONException e) {
            Log.w(LOG_TAG, e);
            return -1;
        }
    }

}
