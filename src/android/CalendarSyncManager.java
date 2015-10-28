/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package io.cozy.calendarsync;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
//import android.provider.ContactsContract.Contacts;
//import android.provider.ContactsContract.RawContacts;
import android.util.Log;

// account
import android.accounts.Account;
import android.accounts.AccountManager;
import java.lang.Runnable;
import android.provider.ContactsContract;
import android.content.ContentResolver;

import android.content.ContentValues;
// Calendars
import android.provider.CalendarContract.Calendars;
import android.net.Uri;


import android.provider.CalendarContract.Events;

public class CalendarSyncManager extends CordovaPlugin {

    private CalendarAccessor calendarAccessor;
    private CallbackContext callbackContext;        // The callback context from which we were invoked.
    private JSONArray executeArgs;

    private static final String LOG_TAG = "Calendar Query";

    public static final int UNKNOWN_ERROR = 0;
    public static final int INVALID_ARGUMENT_ERROR = 1;
    public static final int TIMEOUT_ERROR = 2;
    public static final int PENDING_OPERATION_ERROR = 3;
    public static final int IO_ERROR = 4;
    public static final int NOT_SUPPORTED_ERROR = 5;
    public static final int PERMISSION_DENIED_ERROR = 20;
    private static final int CONTACT_PICKER_RESULT = 1000;

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

        // /**
        //  * Check to see if we are on an Android 1.X device.  If we are return an error as we
        //  * do not support this as of Cordova 1.0.
        //  */
        // if (android.os.Build.VERSION.RELEASE.startsWith("1.")) {
        //     callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, CalendarSyncManager.NOT_SUPPORTED_ERROR));
        //     return true;
        // }

        // /**
        //  * Only create the contactAccessor after we check the Android version or the program will crash
        //  * older phones.
        //  */
        if (calendarAccessor == null) {
            calendarAccessor = new CalendarAccessor(cordova);
        }

        // if (action.equals("dirties")) {
        // }

        // if (action.equals("search")) {
        //     final JSONArray filter = args.getJSONArray(0);
        //     final JSONObject options = args.get(1) == null ? null : args.getJSONObject(1);
        //     this.cordova.getThreadPool().execute(new Runnable() {
        //         public void run() {
        //             JSONArray res = contactAccessor.search(filter, options);
        //             callbackContext.success(res);
        //         }
        //     });
        // }
        // else if (action.equals("save")) {
        //     final JSONObject contact = args.getJSONObject(0);
        //     final String accountType = args.optString(1, null);
        //     final String accountName = args.optString(2, null);
        //     final Boolean callerIsSyncAdapter = args.optBoolean(3, false);
        //     final Boolean resetFields = args.optBoolean(4, false);
        //     this.cordova.getThreadPool().execute(new Runnable(){
        //         public void run() {
        //             JSONObject res = null;
        //             String id = contactAccessor.save(contact, accountType,
        //                 accountName, callerIsSyncAdapter, resetFields);
        //             Log.d(LOG_TAG, "Saved id: " + id);
        //             if (id != null) {
        //                 try {
        //                     res = contactAccessor.getContactById(id);
        //                 } catch (JSONException e) {
        //                     Log.e(LOG_TAG, "JSON fail.", e);
        //                 }
        //             }
        //             Log.d(LOG_TAG, "getContact success: " + String.valueOf(res != null));
        //             if (res != null) {
        //                 callbackContext.success(res);
        //             } else {
        //                 callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
        //             }
        //         }
        //     });
        // }
        // else if (action.equals("remove")) {
        //     final String rawContactId = args.getString(0);
        //     final Boolean callerIsSyncAdapter = args.optBoolean(1, false);

        //     this.cordova.getThreadPool().execute(new Runnable() {
        //         public void run() {
        //             if (contactAccessor.remove(rawContactId, callerIsSyncAdapter)) {
        //                 callbackContext.success();
        //             } else {
        //                 callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
        //             }
        //         }
        //     });
        // }
        // else if (action.equals("pickContact")) {
        //     pickContactAsync();
        // }
        // else

        if (action.equals("createAccount")) {
            final String accountName = args.getString(0);
            final String accountType = args.getString(1);
            createAccount(accountName, accountType);
            callbackContext.success();
        }
        else if (action.equals("listAccounts")) {
            JSONArray accounts = listAccounts();
            callbackContext.success(accounts);
        }
        else if (action.equals("createCalendar")) {
            Log.d(LOG_TAG, "create calendar call");

            createCalendar();
            Log.d(LOG_TAG, "created calendar");

            callbackContext.success();
        }
        else if (action.equals("listDirty")) {
            JSONArray events = dirtyEvents();
            callbackContext.success(events);
        }
        else if (action.equals("all")) {
            final String accountName = args.getString(0);
            final String accountType = args.getString(1);
            JSONArray calendars = calendarAccessor.all(accountType, accountName);

            callbackContext.success(calendars);
        }
        else if (action.equals("dirtyEvents")) {
            final String accountType = args.getString(0);
            final String accountName = args.getString(1);
            JSONArray events = calendarAccessor.dirtyEvents(
                accountType, accountName);

            callbackContext.success(events);

        } else if (action.equals("eventBySyncId")) {
            final String syncId = args.getString(0);
            JSONArray events = calendarAccessor.eventBySyncId(syncId);
            callbackContext.success(events);

        } else if (action.equals("addEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            String eventId = calendarAccessor.addEvent(event, accountType, accountName);
            callbackContext.success(eventId);

        } else if (action.equals("updateEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            calendarAccessor.updateEvent(event, accountType, accountName);
            callbackContext.success();

        } else if (action.equals("undirtyEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            calendarAccessor.undirtyEvent(event, accountType, accountName);
            callbackContext.success();


        } else if (action.equals("deleteEvent")) {
            final JSONObject event = args.getJSONObject(0);
            final String accountType = args.getString(1);
            final String accountName = args.getString(2);
            int deletedCount = calendarAccessor.deleteEvent(event, accountType, accountName);
            callbackContext.success(deletedCount);


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


    private void createAccount(String accountName, String accountType) {
        AccountManager accountManager = AccountManager.get(cordova.getActivity());

        // Create account if not exist.
        for (Account c: accountManager.getAccounts()) {
            if (accountType.equals(c.type) && accountName.equals(c.name)) {
                return; // skip creation.
            }
        }

        Account account = new Account(accountName, accountType);
        accountManager.addAccountExplicitly(account, null, null);


    }

    private JSONArray listAccounts() {
        AccountManager accountManager = AccountManager.get(cordova.getActivity());
        JSONArray accounts = new JSONArray();
        try {
            for (Account c: accountManager.getAccounts()) {
                JSONObject account = new JSONObject();
                account.put("type", c.type);
                account.put("name", c.name);

                accounts.put(account);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON fail.", e);
        }
        return accounts;
    }

    static Uri asSyncAdapter(Uri uri, String accountType, String account) {
        return uri.buildUpon()
            .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
            .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
            .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }

    private void createCalendar() {
        Log.d(LOG_TAG, "create calendar");

        ContentResolver cr = cordova.getActivity().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(Calendars.ACCOUNT_NAME, "myCozy");
        values.put(Calendars.ACCOUNT_TYPE, "io.cozy");
        values.put(Calendars.NAME, "testcalendar");
        values.put(Calendars.CALENDAR_DISPLAY_NAME, "test calendar");
        values.put(Calendars.CALENDAR_COLOR, 0); // as integer
        // android.provider.CalendarContract.CalendarColumns is a protected interface (!?)
        // so use direct value of CalendarColumns.CAL_ACCESS_OWNER : 0x000002bc
        values.put(Calendars.CALENDAR_ACCESS_LEVEL, 0x2bc);//http://developer.android.com/reference/android/provider/CalendarContract.CalendarColumns.html#CALENDAR_ACCESS_LEVEL
        values.put(Calendars.OWNER_ACCOUNT, "myCozy");

        //Optionnal
        // SYNC_EVENTS set to 1
        // CALENDAR_TIME_ZONE
        // ALLOWED_REMINDERS
        // ALLOWED_AVAILABILITY
        // ALLOWED_ATTENDEE_TYPES

        // values.put(Events.DTSTART, startMillis);
        // values.put(Events.DTEND, endMillis);
        // values.put(Events.TITLE, "Jazzercise");
        // values.put(Events.DESCRIPTION, "Group workout");
        // values.put(Events.CALENDAR_ID, calID);
        // values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
        Uri uri = cr.insert(asSyncAdapter(Calendars.CONTENT_URI, "io.cozy", "myCozy"), values);
        Log.d(LOG_TAG, "calendar created");
        Log.d(LOG_TAG, uri.toString());

    }



    private JSONArray dirtyEvents () {
        Log.d(LOG_TAG, "dirtyEvents");

        Cursor cur = null;
        ContentResolver cr = cordova.getActivity().getContentResolver();
        String[] projection = new String[] { Events.TITLE, Events.SYNC_DATA1, Events.SYNC_DATA2 };
        //Events.DTSTART, Events.CALENDAR_ID,

        cur = cr.query(
            asSyncAdapter(Events.CONTENT_URI, "io.cozy", "myCozy"),
            projection,
            "( " + Events.CALENDAR_ID + " = ? ) AND (" +
                Events.DIRTY + " = ? )",
            new String[] { "53", "1" },
            null);
        JSONArray result = new JSONArray();

        if (cur.moveToFirst()) {
            do {
                String eventStr = "";
                    for (String columnName : projection) {
                        eventStr += cur.getString(cur.getColumnIndex(columnName));
                        eventStr += " ; ";
                    }
                result.put(eventStr);

            } while (cur.moveToNext());
        }
        cur.close();
        return result;
    }







    // /**
    //  * Launches the Contact Picker to select a single contact.
    //  */
    // private void pickContactAsync() {
    //     final CordovaPlugin plugin = (CordovaPlugin) this;
    //     Runnable worker = new Runnable() {
    //         public void run() {
    //             Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    //             plugin.cordova.startActivityForResult(plugin, contactPickerIntent, CONTACT_PICKER_RESULT);
    //         }
    //     };
    //     this.cordova.getThreadPool().execute(worker);
    // }

    /**
     * Called when user picks contact.
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     * @throws JSONException
     */
    // public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
    //     if (requestCode == CONTACT_PICKER_RESULT) {
    //         if (resultCode == Activity.RESULT_OK) {
    //             String contactId = intent.getData().getLastPathSegment();
    //             // to populate contact data we require  Raw Contact ID
    //             // so we do look up for contact raw id first
    //             Cursor c =  this.cordova.getActivity().getContentResolver().query(RawContacts.CONTENT_URI,
    //                         new String[] {RawContacts._ID}, RawContacts.CONTACT_ID + " = " + contactId, null, null);
    //             if (!c.moveToFirst()) {
    //                 this.callbackContext.error("Error occured while retrieving contact raw id");
    //                 return;
    //             }
    //             String id = c.getString(c.getColumnIndex(RawContacts._ID));
    //             c.close();

    //             try {
    //                 JSONObject contact = contactAccessor.getContactById(id);
    //                 this.callbackContext.success(contact);
    //                 return;
    //             } catch (JSONException e) {
    //                 Log.e(LOG_TAG, "JSON fail.", e);
    //             }
    //         } else if (resultCode == Activity.RESULT_CANCELED){
    //             this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.NO_RESULT, UNKNOWN_ERROR));
    //             return;
    //         }
    //         this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, UNKNOWN_ERROR));
    //     }
    // }
}
