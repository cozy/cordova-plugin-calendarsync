package io.cozy.calendarsync;

import android.util.Log;
import android.content.Context;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.text.TextUtils;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentProviderOperation;
import android.os.RemoteException;
import android.content.OperationApplicationException;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Reminders;

import org.apache.cordova.CordovaInterface;


public class EventAccessor {
    static final String LOG_TAG = "EventAccessor";
    CordovaInterface app;
    Context context;
    CalendarAccessor calendarAccessor;


    private static final String[] EVENT_COLUMNS = new String[] {
        Events._ID,
        Events.CALENDAR_ID,
        Events.ORGANIZER,
        Events.TITLE,
        Events.EVENT_LOCATION,
        Events.DESCRIPTION,
        Events.EVENT_COLOR,
        Events.DTSTART,
        Events.DTEND,
        Events.EVENT_TIMEZONE,
        Events.EVENT_END_TIMEZONE,
        Events.DURATION,
        Events.ALL_DAY,
        Events.RRULE,
        Events.RDATE,
        Events.EXRULE,
        Events.EXDATE,
        Events.ORIGINAL_ID,
        Events.ORIGINAL_SYNC_ID,
        Events.ORIGINAL_INSTANCE_TIME,
        Events.ORIGINAL_ALL_DAY,
        Events.ACCESS_LEVEL,
        Events.AVAILABILITY,
        Events.GUESTS_CAN_MODIFY,
        Events.GUESTS_CAN_INVITE_OTHERS,
        Events.GUESTS_CAN_SEE_GUESTS,
        Events.CUSTOM_APP_PACKAGE,
        Events.CUSTOM_APP_URI,
        Events.UID_2445,
        Events.DIRTY,
        Events.DELETED,
        Events.MUTATORS,
        Events._SYNC_ID,
        Events.SYNC_DATA1,
        Events.SYNC_DATA2,
        Events.SYNC_DATA3,
        Events.SYNC_DATA4,
        Events.SYNC_DATA5,
        Events.SYNC_DATA6,
        Events.SYNC_DATA7,
        Events.SYNC_DATA8,
        Events.SYNC_DATA9,
        Events.SYNC_DATA10
    };

    private static final String[] ATTENDEE_COLUMNS = new String[] {
        Attendees._ID,
        Attendees.EVENT_ID,
        Attendees.ATTENDEE_NAME,
        Attendees.ATTENDEE_EMAIL,
        Attendees.ATTENDEE_RELATIONSHIP,
        Attendees.ATTENDEE_TYPE,
        Attendees.ATTENDEE_STATUS,
        Attendees.ATTENDEE_IDENTITY,
        Attendees.ATTENDEE_ID_NAMESPACE
    };

    private static final String[] REMINDER_COLUMNS = new String[] {
        Reminders._ID,
        Reminders.EVENT_ID,
        Reminders.MINUTES,
        Reminders.METHOD
    };


    /* Singleton pattern */
    private static EventAccessor instance = null;

    public static void initInstance(CordovaInterface application) {
        if (instance == null) {
            instance = new EventAccessor();
            instance.app = application;
            instance.context = application.getActivity();
            instance.calendarAccessor = CalendarAccessor.getInstance();
        }
    }

    public static EventAccessor getInstance() {
        return instance;
    }

    private EventAccessor() {}
    /* End singleton pattern. */


    ////// Fetching
    public JSONArray allEvents(String accountType, String accountName) {
        return allEvents(accountType, accountName, false);
    }

    /** Fetch all dirty events of the specified account */
    public JSONArray dirtyEvents(String accountType, String accountName) {
        return allEvents(accountType, accountName, true);
    }

    private JSONArray allEvents(String accountType, String accountName,
                                boolean dirtiesOnly) {
        JSONArray calendars = calendarAccessor.getCalendars(
                                accountType, accountName);

        String[] calendarIds = new String[calendars.length()];
        for (int i = 0; i < calendars.length(); i++) {
            try {
                calendarIds[i] = String.valueOf(
                    calendars.getJSONObject(i).getLong(Calendars._ID));
            } catch (JSONException e) {
                Log.w(LOG_TAG, e);
                return null;
            }
        }

        String query = "( " + Events.CALENDAR_ID + " in ( "
        // no android way to use 'in' operator with parameters.
            + TextUtils.join(",", calendarIds) +" ))" ;

        if (dirtiesOnly) {
            query += " AND ( "  + Events.DIRTY + " = 1 )" ;
        }

        Cursor c = context.getContentResolver().query(
            Events.CONTENT_URI,
            EVENT_COLUMNS,
            query,
            null,
            null
        );

        JSONArray events = eventsRows2JSONArray(c);
        c.close();
        return events;
    }


    public JSONArray eventBySyncId(String syncId) {
        Cursor c = context.getContentResolver().query(
            Events.CONTENT_URI,
            EVENT_COLUMNS,
            "( " + Events._SYNC_ID + " = ? )",
            new String[] { syncId },
            null
        );

        JSONArray result = eventsRows2JSONArray(c);
        c.close();
        return result;
    }


    /** Fetch all event of the specified account, by calendars */
    public JSONArray all(String accountType, String accountName) {
        JSONArray calendars = calendarAccessor.getCalendars(
                                accountType, accountName);

        for (int i = 0; i < calendars.length(); i++) {
            try {
                JSONObject calendar = calendars.getJSONObject(i);
                calendar.put("events",
                    getEvents(calendar.getInt(Calendars._ID)));
            } catch (JSONException e) { Log.w(LOG_TAG, e); }
        }

        return calendars;
    }

    /** Fetch all the events of the specified calendar */
    private JSONArray getEvents(int calendarId) {

        Cursor c = context.getContentResolver().query(
            Events.CONTENT_URI,
            EVENT_COLUMNS,
            "( " + Events.CALENDAR_ID + " = ? )",
            new String[] { String.valueOf(calendarId) },
            null
        );

        JSONArray result = eventsRows2JSONArray(c);
        c.close();
        return result;
    }

    /** Fetch all the attendees of the specified event,
       and put them in a JSONArray.
    */
    private JSONArray getAttendees(long eventId) {
        Cursor c = Attendees.query(
            context.getContentResolver(),
            eventId,
            ATTENDEE_COLUMNS
        );

        JSONArray result = Tools.rows2JSONArray(c, ATTENDEE_COLUMNS);
        c.close();
        return result;
    }

    /** Fetch all the reminders of the specified event,
       and put them in a JSONArray.
    */
    private JSONArray getReminders(long eventId) {
        Cursor c = Reminders.query(
            context.getContentResolver(),
            eventId,
            REMINDER_COLUMNS
        );

        JSONArray result = Tools.rows2JSONArray(c, REMINDER_COLUMNS);
        c.close();
        return result;
    }

    private JSONArray eventsRows2JSONArray(Cursor c) {
        JSONArray result = new JSONArray();

        if (c.moveToFirst()) {
            do {
                result.put(eventRow2JSON(c));
            } while (c.moveToNext());
        }
        return result;
    }

    /** Convert Event row to JSON with its dependencies tables.
    */
    private JSONObject eventRow2JSON(Cursor c) {
        // Convert cursor to JSON.
        JSONObject event = Tools.row2JSON(c, EVENT_COLUMNS);
        try {
            event.put("attendees", getAttendees(event.getInt(Events._ID)));
            event.put("reminders", getReminders(event.getInt(Events._ID)));
        } catch (JSONException e) { Log.w(LOG_TAG, e); }

        return event;
    }


    ////// CUD

    /** return id of the saved contact. */
    public String addEvent(JSONObject event,
                    String accountType, String accountName) {
        ContentValues values = Tools.json2Row(event, EVENT_COLUMNS);
        Uri eventUri = context.getContentResolver().insert(
            Tools.asSyncAdapter(Events.CONTENT_URI, accountType, accountName),
            values
        );

        // get the event ID that is the last element in the Uri
        String eventID = eventUri.getLastPathSegment();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        JSONArray attendees = event.optJSONArray("attendees");
        addRowsOnEvent(ops, attendees, eventID, ATTENDEE_COLUMNS,
            Tools.asSyncAdapter(Attendees.CONTENT_URI,
                accountType, accountName));

        JSONArray reminders = event.optJSONArray("reminders");
        addRowsOnEvent(ops, reminders, eventID, REMINDER_COLUMNS,
            Tools.asSyncAdapter(Reminders.CONTENT_URI,
                accountType, accountName));

        try {
            context.getContentResolver().applyBatch(
                CalendarContract.AUTHORITY, ops);
            // TODO: error handling !
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "while adding event", e);
            return "";
        } catch (OperationApplicationException e) {
            Log.e(LOG_TAG, "while adding event", e);
            return "";
        }
        return eventID;
    }


    private void addRowsOnEvent(ArrayList<ContentProviderOperation> ops,
                    JSONArray objects, String eventId, String[] columnNames,
                    Uri contentUri) {
        if (objects != null) {
            for (int i = 0; i < objects.length(); i++) {
                try {
                    JSONObject o = objects.getJSONObject(i);
                    ContentValues values = Tools.json2Row(o, columnNames);
                    values.put("event_id", eventId);
                    ops.add(ContentProviderOperation.newInsert(contentUri)
                        .withValues(values)
                        .build());
                } catch (JSONException e) { Log.w(LOG_TAG, e); }
            }
        }
    }


    public void updateEvent(JSONObject event,
                    String accountType, String accountName) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String eventID = null;
        try {
            eventID = String.valueOf(event.getInt(Events._ID));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "while updating event", e);
            return;
        }

        ContentResolver cr = context.getContentResolver();
        // Update data on the event's row.
        ContentValues values = Tools.json2Row(event, EVENT_COLUMNS);
        ops.add(ContentProviderOperation.newUpdate(
            Tools.asSyncAdapter(Events.CONTENT_URI, accountType, accountName))
            .withSelection(Events._ID + " = ? ", new String[] { eventID })
            .withValues(values)
            .build());

        // Update attendees data.
        Uri attendeesUri = Tools.asSyncAdapter(Attendees.CONTENT_URI,
                            accountType, accountName);
        // Deletes them
        ops.add(ContentProviderOperation.newDelete(attendeesUri)
            .withSelection(
                Attendees.EVENT_ID + " = ? ",
                new String[] { eventID })
            .build());
        JSONArray attendees = event.optJSONArray("attendees");
        // And them re-add them
        addRowsOnEvent(ops, attendees, eventID, ATTENDEE_COLUMNS,attendeesUri);

        // Update reminders data.
        Uri remindersUri = Tools.asSyncAdapter(Reminders.CONTENT_URI,
                            accountType, accountName);

        // Deletes them
        ops.add(ContentProviderOperation.newDelete(remindersUri)
            .withSelection(
                Attendees.EVENT_ID + " = ? ",
                new String[] { eventID })
            .build());

        // And them re-add them
        JSONArray reminders = event.optJSONArray("reminders");
        addRowsOnEvent(ops, reminders, eventID, REMINDER_COLUMNS, remindersUri);

        try {
            context.getContentResolver().applyBatch(
                CalendarContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "while updating event", e);
        } catch (OperationApplicationException e) {
            Log.e(LOG_TAG, "while updating event", e);
        }
    }


    /** Update sync fields on the specified event, setting dirty to 0
    */
    public void undirtyEvent(JSONObject event, String accountType, String accountName) {
        String eventID = null;
        try {
            eventID = String.valueOf(event.getInt(Events._ID));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "while updating event", e);
            return;
        }

        // Update only these fields.
        String[] syncRows = new String[] {
            Events.DELETED,
            Events.MUTATORS,
            Events._SYNC_ID,
            Events.SYNC_DATA1,
            Events.SYNC_DATA2,
            Events.SYNC_DATA3,
            Events.SYNC_DATA4,
            Events.SYNC_DATA5,
            Events.SYNC_DATA6,
            Events.SYNC_DATA7,
            Events.SYNC_DATA8,
            Events.SYNC_DATA9,
            Events.SYNC_DATA10
        };

        ContentValues values = Tools.json2Row(event, syncRows);
        values.put(Events.DIRTY, 0);

        context.getContentResolver().update(
            Tools.asSyncAdapter(Events.CONTENT_URI, accountType, accountName),
            values,
            Events._ID + " = ? ",
            new String[] { eventID }
        );
    }

    // Update : update row.
    // delete reminders; delete attendees,
    // add reminders, add attendees if necessary.
    // Delete : relationnal auto delete attendee ?
    public int deleteEvent(JSONObject event,
                String accountType, String accountName) {
        try {
            ContentResolver cr = context.getContentResolver();
            return cr.delete(
                Tools.asSyncAdapter(Events.CONTENT_URI,
                    accountType, accountName),
                Events._ID + " = ? ",
                new String[] { String.valueOf(event.getLong(Events._ID)) }
            );
        } catch (JSONException e) {
            Log.w(LOG_TAG, e);
            return -1;
        }
    }
}
