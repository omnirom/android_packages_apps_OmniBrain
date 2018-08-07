/*
 *  Copyright (C) 2018 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omnibrain.service;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.bluetooth.BluetoothProfile;

import org.omnirom.omnibrain.events.MediaPlayerEvents;
import org.omnirom.omnibrain.events.WifiEvents;


public class EventService extends Service {
    public static final String EVENTS_PREFERENCES_NAME = "event_service";

    private static final String TAG = "OmniEventService";
    private static final boolean DEBUG = false;
    private static boolean mIsRunning;
    private final LocalBinder mBinder = new LocalBinder();
    private PowerManager.WakeLock mWakeLock;
    private int mOverlayWidth;

    private MediaPlayerEvents mMediaPlayerEvents;
    private WifiEvents mWifiEvents;

    private BroadcastReceiver mStateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mWakeLock.acquire();

            try {
                if (DEBUG) Log.d(TAG, "onReceive " + action);
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        // TODO
                        break;
                    case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                        boolean connect = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                                BluetoothProfile.STATE_CONNECTED) == BluetoothProfile.STATE_CONNECTED;
                        if (connect) {
                            mMediaPlayerEvents.runEvent(context, getPrefs(context), "a2dp", mOverlayWidth);
                        }
                        break;
                    case AudioManager.ACTION_HEADSET_PLUG:
                        if (intent.getIntExtra("state", 0) == 1) {
                            mMediaPlayerEvents.runEvent(context, getPrefs(context), "headset", mOverlayWidth);
                        }
                        break;
                    case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                        mWifiEvents.runEvent(context, getPrefs(context));
                        break;
                }
            } finally {
                mWakeLock.release();
            }
        }
    };

    public static boolean isRunning() {
        return mIsRunning;
    }

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate");
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(true);
        mIsRunning = true;
        registerListener();
        mOverlayWidth = getOverlayWidth(this);
        mMediaPlayerEvents = new MediaPlayerEvents();
        mWifiEvents = new WifiEvents();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mOverlayWidth = getOverlayWidth(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy");
        unregisterListener();
        mIsRunning = false;
    }

    private void registerListener() {
        if (DEBUG) Log.d(TAG, "registerListener");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(mStateListener, filter);
    }

    private void unregisterListener() {
        if (DEBUG) Log.d(TAG, "unregisterListener");
        try {
            this.unregisterReceiver(mStateListener);
        } catch (Exception e) {
            Log.e(TAG, "unregisterListener", e);
        }
    }

    private int getOverlayWidth(Context context) {
        return (context.getResources().getDimensionPixelSize(R.dimen.floating_widget_view_padding) +
                context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size)) / 2;
    }

    public class LocalBinder extends Binder {
        public EventService getService() {
            return EventService.this;
        }
    }

}

