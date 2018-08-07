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
import android.net.wifi.WifiSsid;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class WifiEvents {
    private static final String TAG = "WifiEvents";
    private static final boolean DEBUG = false;

    public static void runEvent(Context context, SharedPreferences prefs) {
        // TODO
    }

    private static void shouldDisableWIFI(Context context) {
        int timeout = getPrefs(context).getInt(EventServiceSettings.DISABLE_WIFI_THRESHOLD, 0);
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

    private static boolean isTaggedNetwork(String ssid, String network_list) {
        if (DEBUG) Log.d(TAG, "Is tagged?: " + ssid);
        if (DEBUG) Log.d(TAG, "Tagged list: " + network_list);
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(network_list)) return false;
        List<String> valueList = Arrays.asList(network_list.split(":"));
        return valueList.contains(ssid);
    }

    private static String getCurrentSSID() {
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
}