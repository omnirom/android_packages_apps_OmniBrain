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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;

import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;
import org.omnirom.omnilib.preference.AppMultiSelectListPreference;
import org.omnirom.omnilib.preference.ScrollAppsViewPreference;
import org.omnirom.omnilib.preference.SeekBarPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MediaPlayerSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {
    public static final String EVENTS_PREFERENCES_NAME = "event_service";

    public static final String EVENT_WIRED_HEADSET_CONNECT = "headset_connect";
    public static final String WIRED_EVENTS_THRESHOLD = "wired_events_threshold";
    public static final String MEDIA_APPS_LIST = "media_apps_list";
    public static final String MEDIA_APPS_LIST_VIEW = "media_apps_list_view";
    public static final String EVENT_AUTORUN_SINGLE = "autorun_single_app";
    public static final String EVENT_DISCONNECT_HEADSET_OR_A2DP = "event_disconnect_headset_or_a2dp";
    public static final String APP_CHOOSER_TIMEOUT = "app_chooser_timeout";
    public static final String APP_CHOOSER_POSITION = "app_chooser_position";
    public static final String EVENT_MEDIA_PLAYER_START = "media_player_autostart";
    public static final String EVENT_MUSIC_ACTIVE = "media_player_music_active";

    private SwitchPreference mAutoStart;
    private SwitchPreference mHeadsetEnable;
    private SwitchPreference mMusicActive;
    private SwitchPreference mAutorun;
    private SeekBarPreference mChooserTimeout;
    private SwitchPreference mDisconnectEvent;
    private ListPreference mChooserPosition;
    private AppMultiSelectListPreference mMediaAppsList;
    private ScrollAppsViewPreference mMediaAppsListView;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.media_player_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences mPreferences = getPrefs();

        mMediaAppsList = (AppMultiSelectListPreference) findPreference(MEDIA_APPS_LIST);
        String value = getPrefs().getString(MEDIA_APPS_LIST, null);
        List<String> valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mMediaAppsList.setValues(valueList);
        mMediaAppsList.setOnPreferenceChangeListener(this);

        mMediaAppsListView = (ScrollAppsViewPreference) findPreference(MEDIA_APPS_LIST_VIEW);
        if (TextUtils.isEmpty(value)) {
            mMediaAppsListView.setVisible(false);
        } else {
            mMediaAppsListView.setVisible(true);
            mMediaAppsListView.setValues(valueList);
        }

        mWiredThresholdTimeout = (SeekBarPreference) findPreference(WIRED_EVENTS_THRESHOLD);
        mWiredThresholdTimeout.setValue(getPrefs().getInt(EventServiceSettings.WIRED_EVENTS_THRESHOLD, 0));
        mWiredThresholdTimeout.setOnPreferenceChangeListener(this);

        mChooserPosition = (ListPreference) findPreference(APP_CHOOSER_POSITION);
        mChooserPosition.setOnPreferenceChangeListener(this);
        mChooserPosition.setValue(
                Integer.toString(mPreferences.getInt(APP_CHOOSER_POSITION, 0)));
        mChooserPosition.setSummary(mChooserPosition.getEntry());

        mAutoStart = (SwitchPreference) findPreference(EVENT_MEDIA_PLAYER_START);
        mAutoStart.setChecked(mPreferences.getBoolean(EVENT_MEDIA_PLAYER_START, false));
        mAutoStart.setOnPreferenceChangeListener(this);

        mMusicActive = (SwitchPreference) findPreference(EVENT_MUSIC_ACTIVE);
        mMusicActive.setChecked(mPreferences.getBoolean(EVENT_MUSIC_ACTIVE, true));
        mMusicActive.setOnPreferenceChangeListener(this);

        mDisconnectEvent = (SwitchPreference) findPreference(EVENT_DISCONNECT_HEADSET_OR_A2DP);
        mDisconnectEvent.setChecked(mPreferences.getBoolean(EVENT_DISCONNECT_HEADSET_OR_A2DP, false));
        mDisconnectEvent.setOnPreferenceChangeListener(this);

        mAutorun = (SwitchPreference) findPreference(EVENT_AUTORUN_SINGLE);
        mAutorun.setChecked(mPreferences.getBoolean(EVENT_AUTORUN_SINGLE, true));
        mAutorun.setOnPreferenceChangeListener(this);

        mChooserTimeout = (SeekBarPreference) findPreference(APP_CHOOSER_TIMEOUT);
        mChooserTimeout.setValue(mPreferences.getInt(APP_CHOOSER_TIMEOUT, 15));
        mChooserTimeout.setOnPreferenceChangeListener(this);

        mHeadsetEnable = (SwitchPreference) findPreference(EVENT_WIRED_HEADSET_CONNECT);
        mHeadsetEnable.setChecked(mPreferences.getBoolean(EVENT_WIRED_HEADSET_CONNECT, false));
        mHeadsetEnable.setOnPreferenceChangeListener(this)
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mMediaAppsList) {
            Collection<String> value = (Collection<String>) newValue;

            mMediaAppsListView.setVisible(false);
            if (value != null && !value.isEmpty()) {
                getPrefs().edit().putString(MEDIA_APPS_LIST, TextUtils.join(":", value)).commit();
                mMediaAppsListView.setValues(value);
                mMediaAppsListView.setVisible(true);
            } else {
                getPrefs().edit().putString(MEDIA_APPS_LIST, null).commit();
            }

            return true;
        } else if (preference == mAutoStart) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_MEDIA_PLAYER_START, value).commit();
            return true;
        } else if (preference == mMusicActive) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_MUSIC_ACTIVE, value).commit();
            return true;
        } else if (preference == mAutorun) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_AUTORUN_SINGLE, value).commit();
            return true;
        } else if (preference == mChooserTimeout) {
            int value = ((int) newValue);
            getPrefs().edit().putInt(APP_CHOOSER_TIMEOUT, value).commit();
            return true;
        } else if (preference == mChooserPosition) {
            int value = Integer.valueOf((String) newValue);
            getPrefs().edit().putInt(APP_CHOOSER_POSITION, value).commit();
            updateChooserPositionSummary(value);
            return true;
        } else if (preference == mWiredThresholdTimeout) {
            int value = ((int) newValue);
            getPrefs().edit().putInt(WIRED_EVENTS_THRESHOLD, value).commit();
            return true;
        } else if (preference == mDisconnectEvent) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_DISCONNECT_HEADSET_OR_A2DP, value).commit();
            return true;
        } else if (preference == mHeadsetEnable) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_WIRED_HEADSET_CONNECT, value).commit();
            return true;
        }
        return false;
    }

    private void updateChooserPositionSummary(int value) {
        Resources res = getResources();
        if (value == 0) {
            mChooserPosition.setSummary(res.getString(R.string.app_chooser_left));
        } else {
            mChooserPosition.setSummary(res.getString(R.string.app_chooser_right));
        }
    }
}
