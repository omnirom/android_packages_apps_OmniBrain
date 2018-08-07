package org.omnirom.omnibrain.fragments;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;

import org.omnirom.omnibrain.service.EventService;
import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;

public class EventCategoryFragment extends OmniLibPreferenceFragment {
    public static final String EVENTS_PREFERENCES_NAME = "event_service";

    public static final String EVENT_SERVICE_ENABLED = "event_service_enabled";

    private SwitchPreference mEnable;
    private String mServiceRunning;
    private String mServiceStopped;
    private Handler mHandler = new Handler();

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.omnibrain_event_category);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mEnable = (SwitchPreference) findPreference(EVENT_SERVICE_ENABLED);
        mEnable.setChecked(getPrefs().getBoolean(EVENT_SERVICE_ENABLED, false));
        mEnable.setOnPreferenceChangeListener(this);
        mServiceRunning = getResources().getString(R.string.event_service_running);
        mServiceStopped = getResources().getString(R.string.event_service_stopped);
        mEnable.setSummary(isServiceRunning() ? mServiceRunning : mServiceStopped);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEnable) {
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
        }
    }

    private boolean isServiceRunning() {
        // return EventService.isRunning();
        return false;
    }
}
