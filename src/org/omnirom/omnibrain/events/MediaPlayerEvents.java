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

package org.omnirom.omnibrain.events;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.util.Log;

import org.omnirom.omnibrain.fragments.MediaPlayerSettings;
import org.omnirom.omnilib.ui.AppChooser;
import org.omnirom.omnilib.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerEvents {
    private static final String TAG = "MediaPlayerEvents";
    private static final boolean DEBUG = true;

    public void runEvent(Context context, SharedPreferences prefs, String mode, int overlayWidth) {
        // Get app list a clean uninstalled apps
        List<String> mAppList = getAvailableAppsList(context, prefs.getString(MediaPlayerSettings.MEDIA_APPS_LIST, null));
        if (mAppList == null) {
            if (DEBUG) Log.d(TAG, "App list is null");
            return;
        }
        // Save filtered list
        prefs.edit().putString(MediaPlayerSettings.MEDIA_APPS_LIST, TextUtils.join(":", mAppList)).commit();

        switch (mode) {
            case "headset":
                if (!prefs.getBoolean(MediaPlayerSettings.EVENT_WIRED_HEADSET_CONNECT, false)) {
                    return;
                }
                break;
        }

        if (prefs.getBoolean(MediaPlayerSettings.EVENT_MUSIC_ACTIVE, true) && isMusicActive()) {
            if (DEBUG) Log.d(TAG, "EVENT_MUSIC_ACTIVE : abort");
            return;
        }

        // boolean sendStart = prefs.getBoolean(MediaPlayerSettings.EVENT_MEDIA_PLAYER_START, false);

        if (prefs.getBoolean(MediaPlayerSettings.EVENT_AUTORUN_SINGLE, true) && mAppList.size() == 1) {
            context.startActivityAsUser(AppUtils.createIntent(mAppList.iterator().next()), UserHandle.CURRENT);
            // if (sendStart) sendStartEvent(context);
        } else {
            AppChooser chooser = new AppChooser(context,
                    prefs.getInt(MediaPlayerSettings.APP_CHOOSER_POSITION, AppChooser.LEFT),
                    prefs.getInt(MediaPlayerSettings.APP_CHOOSER_TIMEOUT, 15),
                    overlayWidth);
            chooser.openAppChooserDialog(mAppList);
        }
    }

    // TODO: NOT WORKING
    public void sendStartEvent(final Context context) {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchMediaKeyToAudioService(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            }
        }, 1000); // TODO: SeekBar for that
    }

    private boolean isMusicActive() {
        return AudioSystem.isStreamActive(AudioSystem.STREAM_MUSIC, 0)
                || AudioSystem.isStreamActiveRemotely(AudioSystem.STREAM_MUSIC, 0);
    }

    // filter out unresolvable (uninstalled) intents
    private List<String> getAvailableAppsList(Context context, String app_list) {
        PackageManager mPm = context.getPackageManager();
        List<String> valueList = new ArrayList<String>();
        if (!TextUtils.isEmpty(app_list)) return null;
        for (String intentUri : app_list.split(":")) {
            Intent intent = AppUtils.createIntent(intentUri);
            if (mPm.resolveActivity(intent, 0) != null) {
                valueList.add(intentUri);
            }
        }

        return valueList;
    }

    private void dispatchMediaKeyToAudioService(Context context, int keycode) {
        if (ActivityManagerNative.isSystemReady()) {
            IAudioService audioService = IAudioService.Stub
                    .asInterface(ServiceManager.checkService(Context.AUDIO_SERVICE));
            if (audioService != null) {
                KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
                        keycode, 0);
                MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(event, true);
                event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
                MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(event, true);
            }
        }
    }
}
