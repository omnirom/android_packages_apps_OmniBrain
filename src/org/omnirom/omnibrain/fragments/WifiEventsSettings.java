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
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import org.omnirom.omnibrain.R;
import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;
import org.omnirom.omnilib.preference.SeekBarPreference;

public class WifiEventsSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {

    public static final String DISABLE_WIFI_THRESHOLD = "disable_wifi_threshold";
    public static final String HOME_WIFI_PREFERENCE_SCREEN = "home_network_events";
    public static final String WORK_WIFI_PREFERENCE_SCREEN = "work_network_events";

    private SeekBarPreference mDisableWifi;
    private Preference homeWifi;
    private Preference workWifi;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EventCategoryFragment.EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.wifi_events_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        boolean locationDisabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.LOCATION_MODE, -1) == 0;

        mDisableWifi = (SeekBarPreference) findPreference(DISABLE_WIFI_THRESHOLD);
        mDisableWifi.setValue(getPrefs().getInt(DISABLE_WIFI_THRESHOLD, 0));
        mDisableWifi.setOnPreferenceChangeListener(this);
        mDisableWifi.setEnabled(!locationDisabled);

        homeWifi = findPreference(HOME_WIFI_PREFERENCE_SCREEN);
        homeWifi.setEnabled(!locationDisabled);

        workWifi = findPreference(WORK_WIFI_PREFERENCE_SCREEN);
        workWifi.setEnabled(!locationDisabled);

        if (locationDisabled) {
            mDisableWifi.setSummary(R.string.wifi_location_disabled);
            homeWifi.setSummary(R.string.wifi_location_disabled);
            workWifi.setSummary(R.string.wifi_location_disabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDisableWifi) {
            int value = ((int) newValue);
            getPrefs().edit().putInt(DISABLE_WIFI_THRESHOLD, value).commit();
            return true;
        }

        return false;
    }
}
