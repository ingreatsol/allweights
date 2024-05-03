package com.ingreatsol.allweights.scan;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.annotation.NonNull;

import com.ingreatsol.allweights.common.AllweightsBase;
import com.ingreatsol.allweights.common.AllweightsException;

import java.util.LinkedHashSet;

public abstract class AllweightsScan extends AllweightsBase {
    private Boolean mScanning = false;
    private final LinkedHashSet<ScanDeviceListener> scanDeviceListeners;
    private final LinkedHashSet<ScanStatusChangeListener> scanStatusChangeListeners;
    private static final long SCAN_PERIOD = 10000;
    private final Object statusLock = new Object();
    private final Object deviceLock = new Object();

    public AllweightsScan(@NonNull final Context context, String feature) {
        super(context,feature);
        scanDeviceListeners = new LinkedHashSet<>();
        scanStatusChangeListeners = new LinkedHashSet<>();
    }

    protected void newScanStatus(Boolean status) {
        mMainHandler.post(() -> {
            synchronized (statusLock){
                this.mScanning = status;
                for (ScanStatusChangeListener listener : scanStatusChangeListeners) {
                    listener.onScanStatusChange(mScanning);
                }
            }
        });
    }

    protected void newDevice(BluetoothDevice device) {
        mMainHandler.post(() -> {
            synchronized (deviceLock){
                for (ScanDeviceListener listener : scanDeviceListeners) {
                    listener.onScanDevice(device);
                }
            }
        });
    }

    public Boolean getScanStatus() {
        return mScanning;
    }

    public void addOnScanDeviceListener(ScanDeviceListener listener) {
        scanDeviceListeners.add(listener);
    }

    public void removeOnScanDeviceListener(ScanDeviceListener listener) {
        scanDeviceListeners.remove(listener);
    }

    public void clearOnScanDeviceListener() {
        scanDeviceListeners.clear();
    }

    public void addOnScanStatusChangeListener(ScanStatusChangeListener listener) {
        scanStatusChangeListeners.add(listener);
    }

    public void removeOnScanStatusChangeListener(ScanStatusChangeListener listener) {
        scanStatusChangeListeners.remove(listener);
    }

    public void clearOnScanStatusChangeListener() {
        scanStatusChangeListeners.clear();
    }

    public void stopScan() {
    }

    public void scan() throws AllweightsException {
        checkBluetooth();
        checkLocation();

        stopScan();

        mMainHandler.postDelayed(this::stopScan, SCAN_PERIOD);
    }

    @Override
    public void destroy() {
        super.destroy();
        scanDeviceListeners.clear();
        scanStatusChangeListeners.clear();
    }

    public interface ScanDeviceListener {
        void onScanDevice(BluetoothDevice device);
    }

    public interface ScanStatusChangeListener {
        void onScanStatusChange(Boolean status);
    }
}
