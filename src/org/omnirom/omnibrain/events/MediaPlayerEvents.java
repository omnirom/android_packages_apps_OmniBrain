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

import java.util.Arrays;
import java.util.List;

public class MediaPlayerEvents implements AppChooser.AppChooserCallback {
    private static final String TAG = "MediaPlayerEvents";
    private static final boolean DEBUG = false;

    public static long mLastUnplugEventTimestamp;

    private Context mContext;
    private boolean mSendStart;

    public MediaPlayerEvents(Context context) {
        mContext = context;
    }

    public void runEvent(SharedPreferences prefs, String mode, int overlayWidth, String device) {
        // Get app list
        String list = prefs.getString(MediaPlayerSettings.MEDIA_APPS_LIST, null);
        if (DEBUG) Log.d(TAG, "App list:" + list);
        if (TextUtils.isEmpty(list)) {
            return;
        }

        // Filter uninstalled apps
        final List<String> mAppList = AppUtils.cleanUninstalledApps(mContext, list);
        prefs.edit().putString(
                MediaPlayerSettings.MEDIA_APPS_LIST,
                mAppList.size() == 0 ? null : TextUtils.join(":", mAppList)
        ).commit();

        if (mAppList.size() == 0) {
            if (DEBUG) Log.d(TAG, "Empty list after filter");
            return;
        }

        switch (mode) {
            case "headset":
                if (!prefs.getBoolean(MediaPlayerSettings.EVENT_WIRED_HEADSET_CONNECT, false)) {
                    return;
                }

                final int threshold = prefs.getInt(MediaPlayerSettings.WIRED_EVENTS_THRESHOLD, 0);
                if (threshold > 0 && mLastUnplugEventTimestamp != 0) {
                    final long eventDelta = System.currentTimeMillis() - mLastUnplugEventTimestamp;
                    if (eventDelta < threshold * 1000) {
                        if (DEBUG)
                            Log.d(TAG, "Ignore AudioManager.ACTION_HEADSET_PLUG");
                        return;
                    }
                }

                break;
            case "a2dp":
                String device_list = prefs.getString(MediaPlayerSettings.EVENT_MEDIA_A2DP_CONNECT, null);
                if (DEBUG) Log.d(TAG, "Device list:" + device_list);
                if (TextUtils.isEmpty(device_list)) {
                    return;
                }

                final List<String> mBTValues = Arrays.asList(device_list.split(":"));
                if (!mBTValues.contains(device)) {
                    if (DEBUG) Log.d(TAG, "Device not in list:" + device);
                    return;
                }
                break;
        }

        if (prefs.getBoolean(MediaPlayerSettings.EVENT_MUSIC_ACTIVE, true) && isMusicActive()) {
            if (DEBUG) Log.d(TAG, "EVENT_MUSIC_ACTIVE : abort");
            return;
        }

        mSendStart = prefs.getBoolean(MediaPlayerSettings.EVENT_MEDIA_PLAYER_START, false);

        if (prefs.getBoolean(MediaPlayerSettings.EVENT_AUTORUN_SINGLE, true) && mAppList.size() == 1) {
            mContext.startActivityAsUser(AppUtils.createIntent(mAppList.iterator().next()), UserHandle.CURRENT);
            if (mSendStart) sendStartEvent();
        } else {
            AppChooser chooser = new AppChooser(mContext,
                    prefs.getInt(MediaPlayerSettings.APP_CHOOSER_POSITION, AppChooser.LEFT),
                    prefs.getInt(MediaPlayerSettings.APP_CHOOSER_TIMEOUT, 15),
                    overlayWidth,
                    mAppList);
            chooser.setCallBack(this);
            chooser.openDialog();
        }
    }

    public void sendStartEvent() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchMediaKeyToAudioService(KeyEvent.KEYCODE_MEDIA_PLAY);
            }
        }, 1000); // TODO: SeekBar for that
    }

    private boolean isMusicActive() {
        return AudioSystem.isStreamActive(AudioSystem.STREAM_MUSIC, 0)
                || AudioSystem.isStreamActiveRemotely(AudioSystem.STREAM_MUSIC, 0);
    }

    private void dispatchMediaKeyToAudioService(int keycode) {
        if (ActivityManagerNative.isSystemReady()) {
            IAudioService audioService = IAudioService.Stub
                    .asInterface(ServiceManager.checkService(Context.AUDIO_SERVICE));
            if (audioService != null) {
                KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
                        keycode, 0);
                MediaSessionLegacyHelper.getHelper(mContext).sendMediaButtonEvent(event, true);
                event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
                MediaSessionLegacyHelper.getHelper(mContext).sendMediaButtonEvent(event, true);
            }
        }
    }

    @Override
    public void onAppClickCallback() {
        if (mSendStart) sendStartEvent();
    }
}
