package org.omnirom.omnibrain.fragments;

import android.os.Bundle;

import org.omnirom.omnilib.fragments.OmniLibPreferenceFragment;

public class EventCategoryFragment extends OmniLibPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.omnibrain_event_category);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
