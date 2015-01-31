/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2014 Carmen Alvarez (c@rmen.ca)
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
package ca.rmen.android.networkmonitor.app.service.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;

import ca.rmen.android.networkmonitor.util.Log;

/**
 * Execute the runnable when the network changes.
 */
public class NetworkChangeScheduler implements Scheduler {

    private static final String TAG = NetworkChangeScheduler.class.getSimpleName();
    private Context mContext;
    private Runnable mRunnableImpl;
    private HandlerThread mHandlerThread;

    @Override
    public void onCreate(Context context) {
        Log.v(TAG, "onCreate");
        mContext = context;
        // Register the broadcast receiver in a background thread
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        Handler handler = new Handler(mHandlerThread.getLooper());
        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), null, handler);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        mContext.unregisterReceiver(mBroadcastReceiver);
        mHandlerThread.quit();
    }

    @Override
    public void schedule(Runnable runnable, int interval) {
        Log.v(TAG, "schedule at interval " + interval);
        mRunnableImpl = runnable;
    }

    @Override
    public void setInterval(int interval) {}

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive: intent = " + intent);
            try {
                Log.v(TAG, "Executing task");
                mRunnableImpl.run();
            } catch (Throwable t) {
                Log.v(TAG, "Error executing task: " + t.getMessage(), t);
            }
        }
    };

}
