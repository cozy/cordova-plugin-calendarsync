/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.cozy.calendarsync.syncadapter;


// import com.example.android.samplesync.Constants;
// import com.example.android.samplesync.client.NetworkUtilities;
// import com.example.android.samplesync.client.RawContact;
// import com.example.android.samplesync.platform.ContactManager;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.List;


// import android.provider.ContactsContract.Groups;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.net.Uri;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    private static final String SYNC_MARKER_KEY = "com.example.android.samplesync.marker";
    private static final boolean NOTIFY_AUTH_FAILURE = true;

    private final AccountManager mAccountManager;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    // private static long ensureSampleGroupExists(Context context, Account account) {
    //         final ContentResolver resolver = context.getContentResolver();
    //         // Lookup the sample group
    //         long groupId = 0;
    //         final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups._ID },
    //             Groups.ACCOUNT_NAME + "=? AND " + Groups.ACCOUNT_TYPE + "=? AND " + Groups.TITLE + "=?",
    //             new String[] { account.name, account.type, "cozygroup"}, null);

    //         if (cursor != null) {
    //             try {
    //                 if (cursor.moveToFirst()) {
    //                     groupId = cursor.getLong(0);
    //                 }
    //             } finally {
    //                 cursor.close();
    //             }
    //         }
    //         if (groupId == 0) {
    //         // Sample group doesn't exist yet, so create it
    //         final ContentValues contentValues = new ContentValues();
    //         contentValues.put(Groups.ACCOUNT_NAME, account.name);
    //         contentValues.put(Groups.ACCOUNT_TYPE, account.type);
    //         contentValues.put(Groups.TITLE, "cozygroup");
    //         contentValues.put(Groups.GROUP_IS_READ_ONLY, true);
    //         final Uri newGroupUri = resolver.insert(Groups.CONTENT_URI, contentValues);
    //         groupId = ContentUris.parseId(newGroupUri);
    //         }
    //         return groupId;
    //     }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync: do nothing !");
        //final long groupId = ensureSampleGroupExists(mContext, account);
    }

    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark
     */
    private long getServerSyncMarker(Account account) {
        String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }

    /**
     * Save off the high-water-mark we receive back from the server.
     * @param account The account we're syncing
     * @param marker The high-water-mark we want to save.
     */
    private void setServerSyncMarker(Account account, long marker) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(marker));
    }
}

