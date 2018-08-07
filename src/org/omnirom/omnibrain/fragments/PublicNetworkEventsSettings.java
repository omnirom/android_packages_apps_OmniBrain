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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PublicNetworkEventsSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {

    public static final String PUBLIC_CONNECT_ACTIONS = "public_connect_actions";
    public static final String PUBLIC_DISCONNECT_ACTIONS = "public_disconnect_actions";

    private OmniActionsListPreference mPublicConnectActions;
    private OmniActionsListPreference mPublicDisconnectActions;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EventCategoryFragment.EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.public_network_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mPublicConnectActions = (OmniActionsListPreference) findPreference(PUBLIC_CONNECT_ACTIONS);
        mPublicConnectActions.loadActions(R.xml.omni_actions);
        String value = getPrefs().getString(PUBLIC_CONNECT_ACTIONS, null);
        List<String> valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mPublicConnectActions.setValues(valueList);
        mPublicConnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                valueList.size()
        ));
        mPublicConnectActions.setOnPreferenceChangeListener(this);

        mPublicDisconnectActions = (OmniActionsListPreference) findPreference(PUBLIC_DISCONNECT_ACTIONS);
        mPublicDisconnectActions.loadActions(R.xml.omni_actions);
        value = getPrefs().getString(PUBLIC_DISCONNECT_ACTIONS, null);
        valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mPublicDisconnectActions.setValues(valueList);
        mPublicDisconnectActions.setSummary(String.format(
                getResources().getString(R.string.omni_actions_summary),
                valueList.size()
        ));
        mPublicDisconnectActions.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPublicConnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                getPrefs().edit().putString(PUBLIC_CONNECT_ACTIONS, TextUtils.join(":", value)).commit();
            } else {
                getPrefs().edit().putString(PUBLIC_CONNECT_ACTIONS, null).commit();
            }

            mPublicConnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        } else if (preference == mPublicDisconnectActions) {
            Collection<String> value = (Collection<String>) newValue;
            if (value != null) {
                getPrefs().edit().putString(PUBLIC_DISCONNECT_ACTIONS, TextUtils.join(":", value)).commit();
            } else {
                getPrefs().edit().putString(PUBLIC_DISCONNECT_ACTIONS, null).commit();
            }

            mPublicDisconnectActions.setSummary(String.format(
                    getResources().getString(R.string.omni_actions_summary),
                    value != null ? value.size() : 0
            ));

            return true;
        }
        return false;
    }
}

