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
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.omnirom.omnibrain.actions.ActionUtils;
import org.omnirom.omnibrain.fragments.BTByDeviceSettings;

public class ByBTDeviceActions {
    private static final String TAG = "ByBTDeviceActions";

    private JSONObject mConnectSettings;
    private JSONObject mDisconnectSettings;

    public void runConnectActions(Context context, SharedPreferences prefs, String mDeviceName) {
        String json_actions = prefs.getString(BTByDeviceSettings.BT_DEVICE_CONNECT_ACTIONS, null);
        if (!TextUtils.isEmpty(json_actions)) {
            try {
                mConnectSettings = new JSONObject(json_actions);
                if (mConnectSettings.has(mDeviceName)) {
                    ActionUtils.execOmniActions(context, mConnectSettings.getString(mDeviceName));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error loading settings for " + mDeviceName, e);
            }
        }
    }

    public void runDisconnectActions(Context context, SharedPreferences prefs, String mDeviceName) {
        String json_actions = prefs.getString(BTByDeviceSettings.BT_DEVICE_DISCONNECT_ACTIONS, null);
        if (!TextUtils.isEmpty(json_actions)) {
            try {
                mDisconnectSettings = new JSONObject(json_actions);
                if (mDisconnectSettings.has(mDeviceName)) {
                    ActionUtils.execOmniActions(context, mDisconnectSettings.getString(mDeviceName));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error loading settings for " + mDeviceName, e);
            }
        }

    }
}