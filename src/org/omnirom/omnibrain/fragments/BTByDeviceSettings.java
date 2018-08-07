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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
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
import java.util.Set;

public class BTByDeviceSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {
    public static final String BT_DEVICE_CONNECT_ACTIONS = "bt_device_connect_actions";
    public static final String BT_DEVICE_DISCONNECT_ACTIONS = "bt_device_disconnect_actions";
    private static final String TAG = "BTByDeviceSettings";
    private static final String BT_DEVICES_LIST = "bt_devices_list";
    private ActionListPreference mDeviceConnectActions;
    private ActionListPreference mDeviceDisconnectActions;
    private ListPreference mSelectedDevice;
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

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        mDeviceConnectActions = (ActionListPreference) findPreference(BT_DEVICE_CONNECT_ACTIONS);
        mDeviceConnectActions.loadActions(R.xml.omni_actions);
        mDeviceConnectActions.setOnPreferenceChangeListener(this);

        mDeviceDisconnectActions = (ActionListPreference) findPreference(BT_DEVICE_DISCONNECT_ACTIONS);
        mDeviceDisconnectActions.loadActions(R.xml.omni_actions);
        mDeviceDisconnectActions.setOnPreferenceChangeListener(this);

        mSelectedDevice = (ListPreference) findPreference(BT_DEVICES_LIST);
        mSelectedDevice.setOnPreferenceChangeListener(this);

        if (devices.size() > 0) {
            int index = 0;
            String[] device_list = new String[devices.size()];

            for (BluetoothDevice device : devices) {
                device_list[index++] = device.getName();
            }

            mSelectedDevice.setEnabled(true);
            mSelectedDevice.setEntries(device_list);
            mSelectedDevice.setEntryValues(device_list);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSelectedDevice) {
            mDeviceName = (String) newValue;
            mSelectedDevice.setTitle(mDeviceName);
            mDeviceConnectActions.setEnabled(true);
            mDeviceDisconnectActions.setEnabled(true);
            setConnectionActions();
            setDisconnectionActions();
        } else if (preference == mDeviceConnectActions) {
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

    private void setConnectionActions() {
        List<String> actions;
        int size = 0;

        String json_actions = getPrefs().getString(BT_DEVICE_CONNECT_ACTIONS, null);

        if (!TextUtils.isEmpty(json_actions)) {
            try {
                mConnectSettings = new JSONObject(json_actions);
                if (mConnectSettings.has(mDeviceName)) {
                    actions = Arrays.asList(mConnectSettings.getString(mDeviceName).split(":"));
                    mDeviceConnectActions.setValues(actions);
                    size = actions.size();
                }
            } catch (JSONException e) {
                mConnectSettings = new JSONObject();
                Log.e(TAG, "Error loading settings for " + mDeviceName, e);
            }
        } else {
            mConnectSettings = new JSONObject();
        }

        mDeviceConnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                size
        ));
    }

    private void setDisconnectionActions() {
        List<String> actions;
        int size = 0;

        String json_actions = getPrefs().getString(BT_DEVICE_DISCONNECT_ACTIONS, null);

        if (!TextUtils.isEmpty(json_actions)) {
            try {
                mDisconnectSettings = new JSONObject(json_actions);
                if (mDisconnectSettings.has(mDeviceName)) {
                    actions = Arrays.asList(mDisconnectSettings.getString(mDeviceName).split(":"));
                    mDeviceDisconnectActions.setValues(actions);
                    size = actions.size();
                }
            } catch (JSONException e) {
                mConnectSettings = new JSONObject();
                Log.e(TAG, "Error loading settings for " + mDeviceName, e);
            }
        } else {
            mDisconnectSettings = new JSONObject();
        }

        mDeviceDisconnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                size
        ));
    }
}
