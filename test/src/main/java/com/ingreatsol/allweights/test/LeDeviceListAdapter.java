package com.ingreatsol.allweights.test;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import com.ingreatsol.allweights.test.databinding.ListitemDeviceBinding;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {
    private final ArrayList<BluetoothDevice> mLeDevices;

    public LeDeviceListAdapter() {
        this.mLeDevices = new ArrayList<>();
    }

    public synchronized void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
        notifyDataSetChanged();
    }

    public synchronized void addDevices(@NonNull ArrayList<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            addDevice(device);
        }
        notifyDataSetChanged();
    }

    public synchronized void remove(BluetoothDevice peer) {
        if (mLeDevices.remove(peer)) {
            notifyDataSetChanged();
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public synchronized void clear() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();

            viewHolder.binding = ListitemDeviceBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false);

            view = viewHolder.binding.getRoot();

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(position);

        @SuppressLint("MissingPermission") final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.binding.deviceName.setText(deviceName);
        else
            viewHolder.binding.deviceName.setText(R.string.unknown_service);
        viewHolder.binding.deviceAddress.setText(device.getAddress());

        return view;
    }

    static class ViewHolder {
        ListitemDeviceBinding binding;
    }

}




