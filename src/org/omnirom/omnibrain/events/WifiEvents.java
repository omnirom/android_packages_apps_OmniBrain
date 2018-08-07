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

package org.omnirom.omnibrain.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.omnirom.omnibrain.actions.ActionUtils;
import org.omnirom.omnibrain.fragments.HomeNetworkEventsSettings;
import org.omnirom.omnibrain.fragments.PublicNetworkEventsSettings;
import org.omnirom.omnibrain.fragments.WifiEventsSettings;
import org.omnirom.omnibrain.fragments.WorkNetworkEventsSettings;

import java.util.Arrays;
import java.util.List;

public class WifiEvents {
    private static final String TAG = "WifiEvents";
    private static final boolean DEBUG = false;

    private WifiManager mWifiManager;
    private boolean mDisableWifiIsRunning;
    private String lastSSID = null;
    private SharedPreferences mPrefs;

    public void runEvent(Context context, SharedPreferences prefs) {
        boolean locationDisabled = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.LOCATION_MODE, -1) == 0;

        mPrefs = prefs;

        if (locationDisabled) {
            if (DEBUG) Log.d(TAG, "Location disabled");
            return;
        }

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (DEBUG) Log.d(TAG, "WifiManager.NETWORK_STATE_CHANGED_ACTION = true");

        if (mWifiManager.isWifiEnabled()) {
            String ssid = getCurrentSSID();
            if (ssid == null) {
                if (DEBUG) Log.d(TAG, "LASTSSID: " + lastSSID);
                execOnDisconnectActions(context);
                shouldDisableWIFI(mPrefs.getInt(WifiEventsSettings.DISABLE_WIFI_THRESHOLD, 0));
            } else {
                if (DEBUG) Log.d(TAG, "SSID: " + ssid);
                execOnConnectActions(context, ssid);
            }
            lastSSID = ssid;
        } else {
            if (lastSSID != null) {
                if (DEBUG) Log.d(TAG, "LASTSSID: " + lastSSID);
                execOnDisconnectActions(context);
                lastSSID = null;
            }
        }
    }

    private void shouldDisableWIFI(int timeout) {
        if ((timeout > 0) && !mDisableWifiIsRunning) {
            if (DEBUG) Log.d(TAG, "DISABLE_WIFI_THRESHOLD true");
            mDisableWifiIsRunning = true;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisableWifiIsRunning = false;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... args) {
                            if (mWifiManager.isWifiEnabled() && getCurrentSSID() == null) {
                                if (DEBUG) Log.d(TAG, "DISABLE WIFI now");
                                mWifiManager.setWifiEnabled(false);
                            }
                            return null;
                        }
                    }.execute();
                }
            }, timeout * 60000);
        }
    }

    private boolean isTaggedNetwork(String ssid, String network_list) {
        if (DEBUG) Log.d(TAG, "Is tagged?: " + ssid);
        if (DEBUG) Log.d(TAG, "Tagged list: " + network_list);
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(network_list)) return false;
        List<String> valueList = Arrays.asList(network_list.split(":"));
        return valueList.contains(ssid);
    }

    private String getCurrentSSID() {
        /*
         * WifiInfo.getSSID retunrs a strings like "<name>"
         * Two options, trim " from returned string or
         *  do that 'override'
         */
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo.getNetworkId() == -1) return null;
        WifiSsid ssid = wifiInfo.getWifiSsid();
        if (ssid != null) {
            String unicode = ssid.toString();
            return !TextUtils.isEmpty(unicode) ? unicode : ssid.getHexString();
        } else {
            return "<unknown ssid>";
        }
    }

    private void execOnConnectActions(Context context, String ssid) {
        if (isTaggedNetwork(ssid, mPrefs.getString(HomeNetworkEventsSettings.HOME_TAGGED_NETWORKS, null))) {
            if (DEBUG) Log.d(TAG, "Is HOME");
            ActionUtils.execOmniActions(context, mPrefs.getString(HomeNetworkEventsSettings.HOME_CONNECT_ACTIONS, null));
        } else if (isTaggedNetwork(ssid, mPrefs.getString(WorkNetworkEventsSettings.WORK_TAGGED_NETWORKS, null))) {
            if (DEBUG) Log.d(TAG, "Is WORK");
            ActionUtils.execOmniActions(context, mPrefs.getString(WorkNetworkEventsSettings.WORK_CONNECT_ACTIONS, null));
        } else {
            if (DEBUG) Log.d(TAG, "Is PUBLIC");
            ActionUtils.execOmniActions(context, mPrefs.getString(PublicNetworkEventsSettings.PUBLIC_CONNECT_ACTIONS, null));
        }
    }

    private void execOnDisconnectActions(Context context) {
        if (isTaggedNetwork(lastSSID, mPrefs.getString(HomeNetworkEventsSettings.HOME_TAGGED_NETWORKS, null))) {
            if (DEBUG) Log.d(TAG, "Is HOME");
            ActionUtils.execOmniActions(context, mPrefs.getString(HomeNetworkEventsSettings.HOME_DISCONNECT_ACTIONS, null));
        } else if (isTaggedNetwork(lastSSID, mPrefs.getString(WorkNetworkEventsSettings.WORK_TAGGED_NETWORKS, null))) {
            if (DEBUG) Log.d(TAG, "Is WORK");
            ActionUtils.execOmniActions(context, mPrefs.getString(WorkNetworkEventsSettings.WORK_DISCONNECT_ACTIONS, null));
        } else {
            if (DEBUG) Log.d(TAG, "Is PUBLIC");
            ActionUtils.execOmniActions(context, mPrefs.getString(PublicNetworkEventsSettings.PUBLIC_DISCONNECT_ACTIONS, null));
        }
    }
}