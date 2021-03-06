package io.cozy.calendarsync;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;


public class CalendarSyncManager extends CordovaPlugin {

    private CalendarAccessor calendarAccessor;
    private EventAccessor eventAccessor;

    private CallbackContext callbackContext; // The callback context from which we were invoked.
    private JSONArray executeArgs;

    private static final String LOG_TAG = "Calendar Query";

    public static final int UNKNOWN_ERROR = 0;
    public static final int INVALID_ARGUMENT_ERROR = 1;
    public static final int TIMEOUT_ERROR = 2;
    public static final int PENDING_OPERATION_ERROR = 3;
    public static final int IO_ERROR = 4;
    public static final int NOT_SUPPORTED_ERROR = 5;
    public static final int PERMISSION_DENIED_ERROR = 20;

    /**
     * Constructor.
     */
    public CalendarSyncManager() {
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        this.executeArgs = args;

        // Create calendarAccessor as a singleton.
        if (calendarAccessor == null) {
            CalendarAccessor.initInstance(cordova);
            calendarAccessor = CalendarAccessor.getInstance();
            EventAccessor.initInstance(cordova);
            eventAccessor = EventAccessor.getInstance();
        }

        // Events methods.
        if (action.equals("all")) {
            final String accountType = args.getString(0);
            final String accountName = args.getString(1);
            JSONArray calendars = eventAccessor.all(accountType, accountName);

            callbackContext.success(calendars);

        } else if (action.equals("allEvents")) {
            final String accountType = args.getString(0);
            final String accountName = args.getString(1);
            JSONArray events = eventAccessor.allEvents(accountType, accountName);

            callbackContext.success(events);

        } else if (action.equals("dirtyEvents")) {
            final String accountType = args.getString(0);
            final String accountName = args.getString(1);
            JSONArray events = eventAccessor.dirtyEvents(
                accountType, accountName);

            callbackContext.success(events);

        } else if (action.equals("eventBySyncId")) {
            final String syncId = args.getString(0);
            JSONArray events = eventAccessor.eventBySyncId(syncId);
            callbackContext.success(events);

        } else if (action.equals("addEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            String eventId = eventAccessor.addEvent(event, accountType, accountName);
            callbackContext.success(eventId);

        } else if (action.equals("updateEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            eventAccessor.updateEvent(event, accountType, accountName);
            callbackContext.success();

        } else if (action.equals("undirtyEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            eventAccessor.undirtyEvent(event, accountType, accountName);
            callbackContext.success();


        } else if (action.equals("deleteEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            int deletedCount = eventAccessor.deleteEvent(event, accountType, accountName);
            callbackContext.success(deletedCount);

        // Calendars methods.
        } else if (action.equals("allCalendars")) {
            final String accountType = args.getString(0);
            final String accountName = args.getString(1);
            JSONArray calendars = calendarAccessor.getCalendars(accountType, accountName);
            callbackContext.success(calendars);

        } else if (action.equals("addCalendar")) {
            final JSONObject calendar = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            String calendarId = calendarAccessor.addCalendar(calendar, accountType, accountName);
            callbackContext.success(calendarId);

        } else if (action.equals("updateCalendar")) {
            final JSONObject calendar = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            calendarAccessor.updateCalendar(calendar,
                                    accountType, accountName);
            callbackContext.success();

        } else if (action.equals("deleteCalendar")) {
            final JSONObject calendar = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            int deletedCount = calendarAccessor.deleteCalendar(calendar, accountType, accountName);
            callbackContext.success(deletedCount);

        } else {
            return false;
        }
        return true;
    }
}
