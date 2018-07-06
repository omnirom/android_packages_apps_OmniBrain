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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

public class EventServiceSettings extends OmniLibPreferenceFragment implements OnPreferenceChangeListener {
    public static final String EVENTS_PREFERENCES_NAME = "event_service";

    public static final String EVENT_A2DP_CONNECT = "bt_a2dp_connect_app_string";
    public static final String EVENT_WIRED_HEADSET_CONNECT = "headset_connect_app_string";
    public static final String EVENT_SERVICE_ENABLED = "event_service_enabled";
    public static final String EVENT_MEDIA_PLAYER_START = "media_player_autostart";
    public static final String EVENT_MUSIC_ACTIVE = "media_player_music_active";
    public static final String EVENT_AUTORUN_SINGLE = "autorun_single_app";
    public static final String A2DP_APP_LIST = "a2dp_app_list";
    public static final String HEADSET_APP_LIST = "headset_app_list";
    public static final String APP_CHOOSER_TIMEOUT = "app_chooser_timeout";
    public static final String APP_CHOOSER_POSITION = "app_chooser_position";
    public static final String WIRED_EVENTS_THRESHOLD = "wired_events_threshold";
    public static final String DISABLE_WIFI_THRESHOLD = "disable_wifi_threshold";
    public static final String HOME_WIFI_PREFERENCE_SCREEN = "home_network_events";
    public static final String WORK_WIFI_PREFERENCE_SCREEN = "work_network_events";
    public static final String EVENT_DISCONNECT_HEADSET_OR_A2DP = "event_disconnect_headset_or_a2dp";

    private AppMultiSelectListPreference mA2DPappSelect;
    private AppMultiSelectListPreference mWiredHeadsetAppSelect;
    private ScrollAppsViewPreference mA2DPApps;
    private ScrollAppsViewPreference mHeadsetApps;
    private SwitchPreference mEnable;
    private SwitchPreference mAutoStart;
    private SwitchPreference mMusicActive;
    private SwitchPreference mAutorun;
    private SeekBarPreference mChooserTimeout;
    private SeekBarPreference mDisableWifi;
    private SwitchPreference mDisconnectEvent;
    private ListPreference mChooserPosition;
    private Handler mHandler = new Handler();
    private String mServiceRunning;
    private String mServiceStopped;
    private SeekBarPreference mWiredThresholdTimeout;
    private Preference homeWifi;
    private Preference workWifi;

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.event_service_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mEnable = (SwitchPreference) findPreference(EVENT_SERVICE_ENABLED);
        mEnable.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_SERVICE_ENABLED, false));
        mEnable.setOnPreferenceChangeListener(this);
        mServiceRunning = getResources().getString(R.string.event_service_running);
        mServiceStopped = getResources().getString(R.string.event_service_stopped);
        mEnable.setSummary(isServiceRunning() ? mServiceRunning : mServiceStopped);

        mChooserPosition = (ListPreference) findPreference(APP_CHOOSER_POSITION);
        mChooserPosition.setOnPreferenceChangeListener(this);
        mChooserPosition.setValue(
                Integer.toString(getPrefs().getInt(EventServiceSettings.APP_CHOOSER_POSITION, 0)));
        mChooserPosition.setSummary(mChooserPosition.getEntry());

        mAutoStart = (SwitchPreference) findPreference(EVENT_MEDIA_PLAYER_START);
        mAutoStart.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_MEDIA_PLAYER_START, false));
        mAutoStart.setOnPreferenceChangeListener(this);

        mMusicActive = (SwitchPreference) findPreference(EVENT_MUSIC_ACTIVE);
        mMusicActive.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_MUSIC_ACTIVE, false));
        mMusicActive.setOnPreferenceChangeListener(this);

        mDisconnectEvent = (SwitchPreference) findPreference(EVENT_DISCONNECT_HEADSET_OR_A2DP);
        mDisconnectEvent.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_DISCONNECT_HEADSET_OR_A2DP, false));
        mDisconnectEvent.setOnPreferenceChangeListener(this);

        mAutorun = (SwitchPreference) findPreference(EVENT_AUTORUN_SINGLE);
        mAutorun.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_AUTORUN_SINGLE, true));
        mAutorun.setOnPreferenceChangeListener(this);

        mChooserTimeout = (SeekBarPreference) findPreference(APP_CHOOSER_TIMEOUT);
        mChooserTimeout.setValue(getPrefs().getInt(EventServiceSettings.APP_CHOOSER_TIMEOUT, 15));
        mChooserTimeout.setOnPreferenceChangeListener(this);

        boolean locationDisabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                            Settings.Secure.LOCATION_MODE, -1) == 0;

        mDisableWifi = (SeekBarPreference) findPreference(DISABLE_WIFI_THRESHOLD);
        mDisableWifi.setValue(getPrefs().getInt(EventServiceSettings.DISABLE_WIFI_THRESHOLD, 0));
        mDisableWifi.setOnPreferenceChangeListener(this);
        mDisableWifi.setEnabled(!locationDisabled);

        homeWifi = findPreference(HOME_WIFI_PREFERENCE_SCREEN);
        homeWifi.setEnabled(!locationDisabled);

        workWifi = findPreference(WORK_WIFI_PREFERENCE_SCREEN);
        workWifi.setEnabled(!locationDisabled);

        if (locationDisabled){
            mDisableWifi.setSummary(R.string.wifi_location_disabled);
            homeWifi.setSummary(R.string.wifi_location_disabled);
            workWifi.setSummary(R.string.wifi_location_disabled);
        }

        mWiredThresholdTimeout = (SeekBarPreference) findPreference(WIRED_EVENTS_THRESHOLD);
        mWiredThresholdTimeout.setValue(getPrefs().getInt(EventServiceSettings.WIRED_EVENTS_THRESHOLD, 0));
        mWiredThresholdTimeout.setOnPreferenceChangeListener(this);

        mA2DPappSelect = (AppMultiSelectListPreference) findPreference(EVENT_A2DP_CONNECT);
        String value = getPrefs().getString(EVENT_A2DP_CONNECT, null);
        List<String> valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mA2DPappSelect.setValues(valueList);
        mA2DPappSelect.setOnPreferenceChangeListener(this);

        mA2DPApps = (ScrollAppsViewPreference) findPreference(A2DP_APP_LIST);
        if (TextUtils.isEmpty(value)) {
            mA2DPApps.setVisible(false);
        } else {
            mA2DPApps.setVisible(true);
            mA2DPApps.setValues(valueList);
        }

        mWiredHeadsetAppSelect = (AppMultiSelectListPreference) findPreference(EVENT_WIRED_HEADSET_CONNECT);
        value = getPrefs().getString(EVENT_WIRED_HEADSET_CONNECT, null);
        valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(value)) {
            valueList.addAll(Arrays.asList(value.split(":")));
        }
        mWiredHeadsetAppSelect.setValues(valueList);
        mWiredHeadsetAppSelect.setOnPreferenceChangeListener(this);

        mHeadsetApps = (ScrollAppsViewPreference) findPreference(HEADSET_APP_LIST);
        if (TextUtils.isEmpty(value)) {
            mHeadsetApps.setVisible(false);
        } else {
            mHeadsetApps.setValues(valueList);
            mHeadsetApps.setVisible(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mA2DPappSelect) {
            Collection<String> value = (Collection<String>) newValue;

            mA2DPApps.setVisible(false);
            if (value != null && !value.isEmpty()) {
                getPrefs().edit().putString(EVENT_A2DP_CONNECT, TextUtils.join(":", value)).commit();
                mA2DPApps.setValues(value);
                mA2DPApps.setVisible(true);
            } else {
                getPrefs().edit().putString(EVENT_A2DP_CONNECT, null).commit();
            }

            return true;
        } else if (preference == mWiredHeadsetAppSelect) {
            Collection<String> value = (Collection<String>) newValue;

            mHeadsetApps.setVisible(false);
            if (value != null && !value.isEmpty()) {
                getPrefs().edit().putString(EVENT_WIRED_HEADSET_CONNECT, TextUtils.join(":", value)).commit();
                mHeadsetApps.setValues(value);
                mHeadsetApps.setVisible(true);
            } else {
                getPrefs().edit().putString(EVENT_WIRED_HEADSET_CONNECT, null).commit();
            }

            return true;
        } else if (preference == mEnable) {
            boolean value = ((Boolean) newValue).booleanValue();
            if (value) {
                getActivity().startService(new Intent(getActivity(), EventService.class));
            } else {
                getActivity().stopService(new Intent(getActivity(), EventService.class));
            }
            getPrefs().edit().putBoolean(EVENT_SERVICE_ENABLED, value).commit();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mEnable.setSummary(isServiceRunning() ? mServiceRunning : mServiceStopped);
                    } catch (Exception e) {
                    }
                }
            }, 1000);
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
        } else if (preference == mDisableWifi) {
            int value = ((int) newValue);
            getPrefs().edit().putInt(DISABLE_WIFI_THRESHOLD, value).commit();
            return true;
        } else if (preference == mDisconnectEvent) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_DISCONNECT_HEADSET_OR_A2DP, value).commit();
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

    private boolean isServiceRunning() {
        return EventService.isRunning();
    }
}
