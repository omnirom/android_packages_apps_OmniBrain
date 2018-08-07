/*
 *  Copyright (C) 2014 The OmniROM Project
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
 */

package org.omnirom.omnibrain.actions;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import org.omnirom.omnibrain.R;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionUtils {
    private static final String TAG = "ActionUtils";
    private static final boolean DEBUG = false;

    public static void execOmniActions(Context context, String actions_list) {
        if (!TextUtils.isEmpty(actions_list)) {
            List<String> valueList = Arrays.asList(actions_list.split(":"));
            try {
                ArrayList<OmniAction> actions = inflate(context, R.xml.omni_actions);
                for (final OmniAction action : actions) {
                    if (DEBUG) Log.d(TAG, "Action key: " + action.key);
                    if (valueList.contains(action.key)) {
                        if (DEBUG) Log.d(TAG, "Action executed: " + action.key);
                        action.execute();
                        continue;
                    }
                }
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "Load omni actions ", e);
            }
        }
    }

    public static ArrayList<OmniAction> inflate(Context context, int xmlFileResId) throws Exception {
        int token;
        ArrayList<OmniAction> actions = new ArrayList<OmniAction>();
        XmlResourceParser parser = context.getResources().getXml(xmlFileResId);

        while ((token = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (token == XmlPullParser.START_TAG) {
                if ("action".equals(parser.getName())) {
                    actions.add(new OmniAction(context, parser));
                }
            }
        }
        return actions;
    }
}