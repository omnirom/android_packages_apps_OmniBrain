/*
 *  Copyright (C) 2013 The OmniROM Project
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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.view.MenuItem;

import org.omnirom.omnibrain.fragments.EventCategoryFragment;
import org.omnirom.omnibrain.fragments.MediaPlayerSettings;
import org.omnirom.omnibrain.fragments.WifiEventsSettings;
import org.omnirom.omnibrain.fragments.HomeNetworkEventsSettings;
import org.omnirom.omnibrain.fragments.WorkNetworkEventsSettings;
import org.omnirom.omnibrain.fragments.PublicNetworkEventsSettings;

public class OmniBrainActivity extends Activity implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.omnibrainactivity_layout);

        mFragmentManager = getFragmentManager();

        // Do not overlapping fragments.
        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new EventCategoryFragment())
                    .commit();
        }
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.addToBackStack(null);

        switch (pref.getFragment()) {
            case "org.omnirom.omnibrain.fragments.MediaPlayerSettings":
                transaction.replace(R.id.content_frame, new MediaPlayerSettings());
                break;
            case "org.omnirom.omnibrain.fragments.WifiEventsSettings":
                transaction.replace(R.id.content_frame, new WifiEventsSettings());
                break;
            case "org.omnirom.omnibrain.fragments.HomeNetworkEventsSettings":
                transaction.replace(R.id.content_frame, new HomeNetworkEventsSettings());
                break;
            case "org.omnirom.omnibrain.fragments.WorkNetworkEventsSettings":
                transaction.replace(R.id.content_frame, new WorkNetworkEventsSettings());
                break;
            case "org.omnirom.omnibrain.fragments.PublicNetworkEventsSettings":
                transaction.replace(R.id.content_frame, new PublicNetworkEventsSettings());
                break;
            default:
                return false;
        }

        transaction.commit();
        setTitle(pref.getTitle());

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStack();
            setTitle(getResources().getString(R.string.event_category_title));
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
