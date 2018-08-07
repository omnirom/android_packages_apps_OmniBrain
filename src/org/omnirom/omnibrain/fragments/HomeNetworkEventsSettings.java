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

import org.omnirom.omnibrain.R;
import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;
import org.omnirom.omnilib.preference.OmniActionsListPreference;
import org.omnirom.omnilib.preference.WifiSelectListPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HomeNetworkEventsSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {

    public static final String HOME_TAGGED_NETWORKS = "home_tagged_networks";
    public static final String HOME_CONNECT_ACTIONS = "home_connect_actions";
    public static final String HOME_DISCONNECT_ACTIONS = "home_disconnect_actions";

    private WifiSelectListPreference mHomeNetworks;
    private OmniActionsListPreference mHomeConnectActions;
    private OmniActionsListPreference mHomeDisconnectActions;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EventCategoryFragment.EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.home_network_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        List<String> valueList = new ArrayList<String>();
        mHomeNetworks = (WifiSelectListPreference) findPreference(HOME_TAGGED_NETWORKS);
        String value = getPrefs().getString(HOME_TAGGED_NETWORKS, null);
        valueList = new ArrayList<String>();
        boolean is_set = !TextUtils.isEmpty(value);
        if (is_set) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mHomeNetworks.setValues(valueList);
        mHomeNetworks.setSummary(String.format(
                getResources().getString(R.string.tagged_networks_summary),
                valueList.size()
        ));
        mHomeNetworks.setOnPreferenceChangeListener(this);

        mHomeConnectActions = (OmniActionsListPreference) findPreference(HOME_CONNECT_ACTIONS);
        mHomeConnectActions.loadActions(R.xml.omni_actions);
        mHomeConnectActions.setEnabled(is_set);
        value = getPrefs().getString(HOME_CONNECT_ACTIONS, null);
        valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mHomeConnectActions.setValues(valueList);
        mHomeConnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                valueList.size()
        ));
        mHomeConnectActions.setOnPreferenceChangeListener(this);

        mHomeDisconnectActions = (OmniActionsListPreference) findPreference(HOME_DISCONNECT_ACTIONS);
        mHomeDisconnectActions.loadActions(R.xml.omni_actions);
        mHomeDisconnectActions.setEnabled(is_set);
        value = getPrefs().getString(HOME_DISCONNECT_ACTIONS, null);
        valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mHomeDisconnectActions.setValues(valueList);
        mHomeDisconnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                valueList.size()
        ));
        mHomeDisconnectActions.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHomeNetworks) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null && !value.isEmpty()) {
                mHomeConnectActions.setEnabled(true);
                mHomeDisconnectActions.setEnabled(true);
                getPrefs().edit().putString(HOME_TAGGED_NETWORKS, TextUtils.join(":", value)).commit();
            } else {
                mHomeConnectActions.setEnabled(false);
                mHomeDisconnectActions.setEnabled(false);
                getPrefs().edit().putString(HOME_TAGGED_NETWORKS, null).commit();
            }

            mHomeNetworks.setSummary(String.format(
                    getResources().getString(R.string.tagged_networks_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        } else if (preference == mHomeConnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                getPrefs().edit().putString(HOME_CONNECT_ACTIONS, TextUtils.join(":", value)).commit();
            } else {
                getPrefs().edit().putString(HOME_CONNECT_ACTIONS, null).commit();
            }

            mHomeConnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        } else if (preference == mHomeDisconnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                getPrefs().edit().putString(HOME_DISCONNECT_ACTIONS, TextUtils.join(":", value)).commit();
            } else {
                getPrefs().edit().putString(HOME_DISCONNECT_ACTIONS, null).commit();
            }

            mHomeDisconnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        }
        return false;
    }
}

