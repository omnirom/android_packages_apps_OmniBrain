/*
 *  Copyright (C) 2016 The OmniROM Project
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
package org.omnirom.omnibrain.actions;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settingslib.CustomDialogPreference;

import org.omnirom.omnilib.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActionListPreference extends CustomDialogPreference {
    private static final String TAG = "OmniActionsList";
    private static final boolean DEBUG = true;

    private ArrayList<OmniAction> oActionInfoList;
    private ActionListAdapter mAdapter;
    private List<String> mValues = new ArrayList<String>();
    private Context mContext;

    public ActionListPreference(Context context) {
        this(context, null);
    }

    public ActionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_action_list);
        mContext = context;
        setPositiveButtonText(R.string.action_save);
        setNegativeButtonText(android.R.string.cancel);
    }

    public Collection<String> getValues() {
        return mValues;
    }

    public void setValues(Collection<String> values) {
        mValues.clear();
        mValues.addAll(values);
    }

    public boolean loadActions(int xml_id) {
        try {
            oActionInfoList = ActionUtils.inflate(mContext, xml_id);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Load omni actions ", e);
            return false;
        }
        return true;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mAdapter = new ActionListAdapter(getContext());
        final ListView listView = (ListView) view.findViewById(R.id.action_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AppViewHolder holder = (AppViewHolder) view.getTag();
                final boolean isChecked = !holder.checkBox.isChecked();

                holder.checkBox.setChecked(isChecked);
                OmniAction info = mAdapter.getItem(position);

                if (isChecked) {
                    mValues.add(info.key);
                } else {
                    mValues.remove(info.key);
                }
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            callChangeListener(mValues.size() > 0 ? mValues : null);
        }
    }

    public static class AppViewHolder {
        public View rootView;
        public TextView title;
        public CheckBox checkBox;

        public static AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.action_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                AppViewHolder holder = new AppViewHolder();
                holder.rootView = convertView;
                holder.title = (TextView) convertView.findViewById(R.id.action_title);
                holder.checkBox = (CheckBox) convertView.findViewById(android.R.id.checkbox);
                convertView.setTag(holder);
                return holder;
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                return (AppViewHolder) convertView.getTag();
            }
        }
    }

    public class ActionListAdapter extends ArrayAdapter<OmniAction> {
        private final LayoutInflater mInflater;

        public ActionListAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(oActionInfoList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder = AppViewHolder.createOrRecycle(mInflater, convertView);
            convertView = holder.rootView;
            OmniAction info = getItem(position);
            holder.title.setText(info.title);
            holder.checkBox.setChecked(mValues.contains(info.key));
            return convertView;
        }

        @Override
        public OmniAction getItem(int position) {
            return oActionInfoList.get(position);
        }
    }
}

