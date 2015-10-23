package io.cozy.calendarsync;

import android.util.Log;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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


public class CalendarAccessor {
    protected final String LOG_TAG = "CalendarAccessor";
    protected CordovaInterface app;

    private static final String[] CALENDAR_ROWS = new String[] {
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

    private static final String[] EVENT_ROWS = new String[] {
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

    private static final String[] ATTENDEE_ROWS = new String[] {
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

    private static final String[] REMINDER_ROWS = new String[] {
        Reminders._ID,
        Reminders.EVENT_ID,
        Reminders.MINUTES,
        Reminders.METHOD
    };

    public CalendarAccessor(CordovaInterface context) {
        app = context;
    }


    ////// Generic Android cursor to JSON tools

    private JSONArray rows2JSONArray(Cursor c, String[] rowNames) {
        JSONArray result = new JSONArray();

        if (c.moveToFirst()) {
            do {
                result.put(row2JSON(c, ATTENDEE_ROWS));
            } while (c.moveToNext());
        }
        return result;
    }

    private JSONObject row2JSON(Cursor c, String[] rowNames) {
        JSONObject event = new JSONObject();
        for (String name : rowNames) {
            int index = c.getColumnIndex(name);
            if (index < 0) { // No such column in the row.
                continue;
            }
            try {
                switch(c.getType(index)) {
                    case Cursor.FIELD_TYPE_NULL: break; // pass
                    case Cursor.FIELD_TYPE_INTEGER:
                        event.put(name, c.getInt(index));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                            event.put(name, c.getFloat(index));
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


    ////// Convert events and theirs parts to JSON.

    /** Fetch all the attendees of the specified event,
       and put them in a JSONArray.
    */
    private JSONArray getAttendees(long eventId) {
        Cursor c = Attendees.query(
            app.getActivity().getContentResolver(),
            eventId,
            ATTENDEE_ROWS
        );

        JSONArray result = rows2JSONArray(c, ATTENDEE_ROWS);

        c.close();
        return result;
    }

    /** Fetch all the reminders of the specified event,
       and put them in a JSONArray.
    */
    private JSONArray getReminders(long eventId) {
        Cursor c = Reminders.query(
            app.getActivity().getContentResolver(),
            eventId,
            REMINDER_ROWS
        );

        JSONArray result = rows2JSONArray(c, REMINDER_ROWS);

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
        JSONObject event = row2JSON(c, EVENT_ROWS);
        try {
            event.put("attendees", getAttendees(event.getInt(Events._ID)));

            event.put("reminders", getReminders(event.getInt(Events._ID)));
        } catch (JSONException e) { Log.w(LOG_TAG, e); }

        return event;
    }


    /** Fetch all calendars of the specified account */
    private JSONArray getCalendars(String accountType, String accountName) {
        // Fetch all calendars with specified account type and name.
        Cursor c = app.getActivity().getContentResolver().query(
            asSyncAdapter(Calendars.CONTENT_URI, accountType, accountName),
            CALENDAR_ROWS,
            "( " + Calendars.ACCOUNT_NAME + " = ? ) AND ( " +
            Calendars.ACCOUNT_TYPE + " = ? )",
            new String[] { accountName, accountType },
            null
        );

        // Convert to JSON.
        return rows2JSONArray(c, CALENDAR_ROWS);
    }

    ////// Queries.

    static Uri asSyncAdapter(Uri uri, String accountType, String account) {
        return uri.buildUpon()
            .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
            .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
            .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }

    /** Fetch all event of the specified account, by calendars */
    public JSONArray all(String accountType, String accountName, boolean dirtiesOnly) {
        JSONArray calendars = getCalendars(accountType, accountName);

        for (int i = 0; i < calendars.length(); i++) {
            try {
                JSONObject calendar = calendars.getJSONObject(i);
                calendar.put("events", getEvents(calendar.getInt(Calendars._ID),    dirtiesOnly));
            } catch (JSONException e) { Log.w(LOG_TAG, e); }
        }

        return calendars;
    }


    /** Fetch all the events of the specified calendar */
    private JSONArray getEvents(int calendarId, boolean dirtiesOnly) {

        Cursor c = app.getActivity().getContentResolver().query(
            Events.CONTENT_URI,
            EVENT_ROWS,
            "( " + Events.CALENDAR_ID + " = ? )" +
            (dirtiesOnly? " AND ( "  + Events.DIRTY + " = 1 )" : "") ,
            new String[] { String.valueOf(calendarId) },
            null
        );

        JSONArray result = eventsRows2JSONArray(c);
        c.close();
        return result;
    }


    //////////////////////////// Add

    // Calendar : get / create / all .
    //
    // Add :
    // Nom Tag --> id calendrier --> dans la logique de l'app Cozy.
    //
    // Loop sur les nom des champs de la base, si donnée --> insert.
    // Vérif données minimales // placeholders ?
    // si reminders --> add reminders
    // si attendees --> add attendees.

    /** return id of the saved contact. */
    public String addEvent(JSONObject event, String accountType, String accountName) {
        ContentValues values = json2Row(event, EVENT_ROWS);
        Uri eventUri = app.getActivity().getContentResolver().insert(
            asSyncAdapter(Events.CONTENT_URI, accountType, accountName),
            values
        );

        // get the event ID that is the last element in the Uri
        String eventID = eventUri.getLastPathSegment();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        JSONArray attendees = event.optJSONArray("attendees");
        addRows(ops, attendees, eventID, ATTENDEE_ROWS, asSyncAdapter(Attendees.CONTENT_URI, accountType, accountName));

        JSONArray reminders = event.optJSONArray("reminders");
        addRows(ops, reminders, eventID, REMINDER_ROWS, asSyncAdapter(Reminders.CONTENT_URI, accountType, accountName));

        try {
            app.getActivity().getContentResolver().applyBatch(
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


    private void addRows(ArrayList<ContentProviderOperation> ops, JSONArray objects, String eventId, String[] rowNames, Uri contentUri
        ) {
        if (objects != null) {
            for (int i = 0; i < objects.length(); i++) {
                try {
                    JSONObject o = objects.getJSONObject(i);
                    ContentValues values = json2Row(o, rowNames);
                    values.put("event_id", eventId);
                    ops.add(ContentProviderOperation.newInsert(contentUri)
                        .withValues(values)
                        .build());
                } catch (JSONException e) { Log.w(LOG_TAG, e); }

            }
        }
    }

    private ContentValues json2Row(JSONObject o, String[] rowNames) {
        ContentValues result = new ContentValues();
        for (String name: rowNames) {
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

    /** return id of the saved contact. */
    public void updateEvent(JSONObject event, String accountType, String accountName) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String eventID = null;
        try {
            eventID = String.valueOf(event.getInt(Events._ID));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "while updating event", e);
            return;
        }

        ContentResolver cr = app.getActivity().getContentResolver();
        ContentValues values = json2Row(event, EVENT_ROWS);
        ops.add(ContentProviderOperation.newUpdate(
            asSyncAdapter(Events.CONTENT_URI, accountType, accountName))
            .withSelection(Events._ID + " = ? ", new String[] { eventID })
            .withValues(values)
            .build());

        Uri attendeesUri = asSyncAdapter(Attendees.CONTENT_URI, accountType, accountName);
        ops.add(ContentProviderOperation.newDelete(attendeesUri)
            .withSelection(Attendees.EVENT_ID + " = ? ", new String[] { eventID })
            .build());
        JSONArray attendees = event.optJSONArray("attendees");
        addRows(ops, attendees, eventID, ATTENDEE_ROWS, attendeesUri);


        Uri remindersUri = asSyncAdapter(Reminders.CONTENT_URI, accountType, accountName);
        ops.add(ContentProviderOperation.newDelete(remindersUri)
            .withSelection(Attendees.EVENT_ID + " = ? ", new String[] { eventID })
            .build());
        JSONArray reminders = event.optJSONArray("reminders");
        addRows(ops, reminders, eventID, REMINDER_ROWS, remindersUri);

        try {
            app.getActivity().getContentResolver().applyBatch(
                CalendarContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "while updating event", e);
        } catch (OperationApplicationException e) {
            Log.e(LOG_TAG, "while updating event", e);
        }

    }


    public void undirty(JSONObject event, String accountType, String accountName) {

        try {
            eventID = String.valueOf(event.getInt(Events._ID));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "while updating event", e);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Events.DIRTY, 0);

        app.getActivity().getContentResolver().update(
            asSyncAdapter(Events.CONTENT_URI, accountType, accountName),
            values,
            Events._ID + " = ? ",
            new String[] { eventID },
        );
    }
    // Update : update row.
    // delete reminders; delete attendees,
    // add reminders, add attendees if necessary.

    // Delete : relationnal auto delete attendee ?
}


