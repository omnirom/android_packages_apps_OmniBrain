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

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.omnirom.omnibrain.ui.SimpleDividerItemDecoration;

import java.util.List;
import java.util.Set;

public class BTDeviceEventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private BTDevicesAdapter mAdapter;
    private Context mContext;

    public BTDeviceEventsFragment() {
        // Empty constructor
    }

    public BTDeviceEventsFragment(Context context) {
        mContext = context;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter = new BTDevicesAdapter(mBluetoothAdapter.getBondedDevices());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bt_device_config_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.device_list);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        recyclerView.setHasFixedSize(true); // does not change, except in onResume()
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    class BTDevicesAdapter extends RecyclerView.Adapter<BTDevicesAdapter.ViewHolder> {
        private List<String> mDevices;

        public BTDevicesAdapter(Set<BluetoothDevice> devices) {
            for (BluetoothDevice device : devices) {
                mDevices.add(device.getName());
            }
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }

        @Override
        public BTDevicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bt_device_item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final BTDevicesAdapter.ViewHolder holder, int position) {
            holder.name.setText(mDevices.get(position));
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO
                }
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView name;

            ViewHolder(final View row) {
                super(row);
                name = (TextView) row.findViewById(R.id.device_name);
            }
        }
    }
}
