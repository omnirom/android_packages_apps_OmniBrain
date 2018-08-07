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

package org.omnirom.omnibrain.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.omnirom.omnibrain.R;
import org.omnirom.omnibrain.actions.ActionListPreference;
import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BTByDeviceSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "BTByDeviceSettings";

    private static final String BT_DEVICE_CONNECT_ACTIONS = "bt_device_connect_actions";
    private static final String BT_DEVICE_DISCONNECT_ACTIONS = "bt_device_disconnect_actions";

    private ActionListPreference mDeviceConnectActions;
    private ActionListPreference mDeviceDisconnectActions;

    private JSONObject mConnectSettings;
    private JSONObject mDisconnectSettings;
    private String mDeviceName;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EventCategoryFragment.EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.bt_by_device_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        List<String> actions;
        int size = 0;

        mDeviceConnectActions = (ActionListPreference) findPreference(BT_DEVICE_CONNECT_ACTIONS);
        mDeviceConnectActions.loadActions(R.xml.omni_actions);
        mDeviceConnectActions.setOnPreferenceChangeListener(this);

        try {
            mConnectSettings = new JSONObject(getPrefs().getString(BT_DEVICE_CONNECT_ACTIONS, null));
            if (mConnectSettings.has(mDeviceName)) {
                try {
                    actions = Arrays.asList(mConnectSettings.getString(mDeviceName).split(":"));
                    mDeviceConnectActions.setValues(actions);
                    size = actions.size();
                } catch (JSONException e) {
                    Log.e(TAG, "No connect settings for " + mDeviceName, e);
                    size = 0;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "No connect settings", e);
            mConnectSettings = new JSONObject();
            size = 0;
        }

        mDeviceConnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                size
        ));


        mDeviceDisconnectActions = (ActionListPreference) findPreference(BT_DEVICE_DISCONNECT_ACTIONS);
        mDeviceDisconnectActions.loadActions(R.xml.omni_actions);
        mDeviceDisconnectActions.setOnPreferenceChangeListener(this);

        try {
            mDisconnectSettings = new JSONObject(getPrefs().getString(BT_DEVICE_DISCONNECT_ACTIONS, null));
            if (mDisconnectSettings.has(mDeviceName)) {
                try {
                    actions = Arrays.asList(mDisconnectSettings.getString(mDeviceName).split(":"));
                    mDeviceDisconnectActions.setValues(actions);
                    size = actions.size();
                } catch (JSONException e) {
                    Log.e(TAG, "No disconnect settings for " + mDeviceName, e);
                    size = 0;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "No disconnect settings", e);
            mDisconnectSettings = new JSONObject();
            size = 0;
        }

        mDeviceDisconnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                size
        ));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDeviceConnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                try {
                    mConnectSettings.put(mDeviceName, TextUtils.join(":", value));
                    getPrefs().edit().putString(BT_DEVICE_CONNECT_ACTIONS, mConnectSettings.toString()).commit();
                } catch (JSONException e) {
                    Log.e(TAG, "Unable to save settings", e);
                    getPrefs().edit().putString(BT_DEVICE_CONNECT_ACTIONS, null).commit();
                }
            } else {
                getPrefs().edit().putString(BT_DEVICE_CONNECT_ACTIONS, null).commit();
            }

            mDeviceConnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        } else if (preference == mDeviceDisconnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                try {
                    mDisconnectSettings.put(mDeviceName, TextUtils.join(":", value));
                    getPrefs().edit().putString(BT_DEVICE_DISCONNECT_ACTIONS, mDisconnectSettings.toString()).commit();
                } catch (JSONException e) {
                    Log.e(TAG, "Unable to save settings", e);
                    getPrefs().edit().putString(BT_DEVICE_DISCONNECT_ACTIONS, null).commit();
                }
            } else {
                getPrefs().edit().putString(BT_DEVICE_DISCONNECT_ACTIONS, null).commit();
            }

            mDeviceDisconnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        }

        return false;
    }

    public void setDeviceName(String name) {
        mDeviceName = name;
    }

}
