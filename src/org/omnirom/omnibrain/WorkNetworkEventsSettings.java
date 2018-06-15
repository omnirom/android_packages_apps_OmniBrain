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

package org.omnirom.omnibrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;

import org.omnirom.omnibrain.R;

import org.omnirom.omnilib.preference.OmniActionsListPreference;
import org.omnirom.omnilib.preference.WifiSelectListPreference;
import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;

import android.support.v14.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WorkNetworkEventsSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {
    public static final String EVENTS_PREFERENCES_NAME = "event_service";


    public static final String WORK_TAGGED_NETWORKS = "work_tagged_networks";
    public static final String WORK_CONNECT_ACTIONS = "work_connect_actions";
    public static final String WORK_DISCONNECT_ACTIONS = "work_disconnect_actions";


    private WifiSelectListPreference mWorkNetworks;
    private OmniActionsListPreference mWorkConnectActions;
    private OmniActionsListPreference mWorkDisconnectActions;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.work_network_settings);

        List<String> valueList = new ArrayList<String>();
        mWorkNetworks = (WifiSelectListPreference) findPreference(WORK_TAGGED_NETWORKS);
        String value = getPrefs().getString(WORK_TAGGED_NETWORKS, null);
        valueList = new ArrayList<String>();
        boolean is_set = !TextUtils.isEmpty(value);
        if (is_set) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mWorkNetworks.setValues(valueList);
        mWorkNetworks.setSummary(String.format(
                getResources().getString(R.string.tagged_networks_summary),
                valueList.size()
        ));
        mWorkNetworks.setOnPreferenceChangeListener(this);

        mWorkConnectActions = (OmniActionsListPreference) findPreference(WORK_CONNECT_ACTIONS);
        mWorkConnectActions.loadActions(R.xml.omni_actions);
        mWorkConnectActions.setEnabled(is_set);
        value = getPrefs().getString(WORK_CONNECT_ACTIONS, null);
        valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mWorkConnectActions.setValues(valueList);
        mWorkConnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                valueList.size()
        ));
        mWorkConnectActions.setOnPreferenceChangeListener(this);

        mWorkDisconnectActions = (OmniActionsListPreference) findPreference(WORK_DISCONNECT_ACTIONS);
        mWorkDisconnectActions.loadActions(R.xml.omni_actions);
        mWorkDisconnectActions.setEnabled(is_set);
        value = getPrefs().getString(WORK_DISCONNECT_ACTIONS, null);
        valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mWorkDisconnectActions.setValues(valueList);
        mWorkDisconnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                valueList.size()
        ));
        mWorkDisconnectActions.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWorkNetworks) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                mWorkConnectActions.setEnabled(true);
                mWorkDisconnectActions.setEnabled(true);
                getPrefs().edit().putString(WORK_TAGGED_NETWORKS, TextUtils.join(":", value)).commit();
            } else {
                mWorkConnectActions.setEnabled(false);
                mWorkDisconnectActions.setEnabled(false);
                getPrefs().edit().putString(WORK_TAGGED_NETWORKS, null).commit();
            }

            mWorkNetworks.setSummary(String.format(
                    getResources().getString(R.string.tagged_networks_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        } else if (preference == mWorkConnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                getPrefs().edit().putString(WORK_CONNECT_ACTIONS, TextUtils.join(":", value)).commit();
            } else {
                getPrefs().edit().putString(WORK_CONNECT_ACTIONS, null).commit();
            }

            mWorkConnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        } else if (preference == mWorkDisconnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                getPrefs().edit().putString(WORK_DISCONNECT_ACTIONS, TextUtils.join(":", value)).commit();
            } else {
                getPrefs().edit().putString(WORK_DISCONNECT_ACTIONS, null).commit();
            }
            mWorkDisconnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        }
        return false;
    }
}

