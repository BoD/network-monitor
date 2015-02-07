/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Carmen Alvarez (c@rmen.ca)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.rmen.android.networkmonitor.app.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import ca.rmen.android.networkmonitor.Constants;
import ca.rmen.android.networkmonitor.app.prefs.NetMonPreferences;
import ca.rmen.android.networkmonitor.provider.NetMonColumns;
import ca.rmen.android.networkmonitor.provider.NetMonProvider;
import ca.rmen.android.networkmonitor.util.Log;

/**
 * Only keep the most recent X records: where X is determined by the
 * preference set by the user.
 */
public class DBPurge {
    private static final String TAG = Constants.TAG + "/" + DBPurge.class.getSimpleName();

    /**
     * Only keep the most recent X records: where X is determined by the
     * preference set by the user.
     *
     * @return the number of deleted rows.
     */
    public static int purgeDB(Context context) {
        Log.v(TAG, "purgeDB");
        int recordCount = NetMonPreferences.getInstance(context).getDBRecordCount();
        if (recordCount < 0) return 0;

        // Query the most recent X ids.
        // Then find the oldest id from this query.
        Uri uri = NetMonColumns.CONTENT_URI.buildUpon().appendQueryParameter(NetMonProvider.QUERY_PARAMETER_LIMIT, String.valueOf(recordCount)).build();
        int oldestIdToKeep = -1;
        Cursor cursor = context.getContentResolver().query(uri, new String[] { BaseColumns._ID }, null, null, BaseColumns._ID + " DESC");
        if (cursor != null) {
            try {
                if (cursor.moveToLast()) {
                    oldestIdToKeep = cursor.getInt(0);
                }
            } finally {}
            cursor.close();
        }

        if (oldestIdToKeep > 0) {
            Log.v(TAG, "Will delete rows before id=" + oldestIdToKeep);
            int result = context.getContentResolver().delete(NetMonColumns.CONTENT_URI, BaseColumns._ID + " < ?",
                    new String[] { String.valueOf(oldestIdToKeep) });
            Log.v(TAG, "Deleted " + result + " rows");
            return result;
        }
        return 0;
    }
}
