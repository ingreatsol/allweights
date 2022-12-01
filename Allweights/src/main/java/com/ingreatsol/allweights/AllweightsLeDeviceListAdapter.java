package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresPermission;

import java.util.ArrayList;

public class AllweightsLeDeviceListAdapter extends BaseAdapter {
    private final ArrayList<BluetoothDevice> mLeDevices;
    protected Activity activity;
    private final @LayoutRes int mResource;
    private final @IdRes int device_address;
    private final @IdRes int device_name;

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public AllweightsLeDeviceListAdapter(Activity activity, @LayoutRes int layout,
                                         @IdRes int device_address, @IdRes int device_name) {
        this.activity = activity;
        this.mLeDevices = new ArrayList<>();
        this.mResource = layout;
        this.device_address = device_address;
        this.device_name = device_name;
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
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
            LayoutInflater mInflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflator.inflate(mResource, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = view.findViewById(device_address);
            viewHolder.deviceName = view.findViewById(device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText("Unknown service");
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}




