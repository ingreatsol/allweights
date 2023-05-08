package com.ingreatsol.allweights.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.test.databinding.ListitemDeviceBinding;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {
    private final ArrayList<BluetoothDevice> mLeDevices;
    protected Activity activity;

    public LeDeviceListAdapter() {
        this.mLeDevices = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
        notifyDataSetChanged();
    }

    public void addDevices(@NonNull ArrayList<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            addDevice(device);
        }
        notifyDataSetChanged();
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
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

    @SuppressLint("SetTextI18n")
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();

            viewHolder.binding = ListitemDeviceBinding
                    .inflate(LayoutInflater.from(parent.getContext()),parent,false);

            view = viewHolder.binding.getRoot();

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(position);

        final String deviceName = device.getName();
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




