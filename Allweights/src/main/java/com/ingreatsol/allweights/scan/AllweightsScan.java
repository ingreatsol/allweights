package com.ingreatsol.allweights.scan;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ingreatsol.allweights.common.AllweightsBase;
import com.ingreatsol.allweights.common.AllweightsException;

import java.util.ArrayList;

public abstract class AllweightsScan extends AllweightsBase {
    protected Boolean mScanning = false;
    protected final ArrayList<AllweightsScanCallback> mOnAllweightsScanCallback;
    public static final long SCAN_PERIOD = 10000;

    public AllweightsScan(@NonNull final Context context, String feature) {
        super(context,feature);
        mOnAllweightsScanCallback = new ArrayList<>();
    }

    protected void newScanStatus(Boolean status) {
        mScanning = status;
        for (AllweightsScanCallback listener : mOnAllweightsScanCallback) {
            listener.onAllweightsScanStatusChange(mScanning);
        }
    }

    public Boolean getScanStatus() {
        return mScanning;
    }

    public void addOnAllweightsScanCallback(AllweightsScanCallback listener) {
        mOnAllweightsScanCallback.add(listener);
    }

    public void removeOnAllweightsScanCallback(AllweightsScanCallback listener) {
        mOnAllweightsScanCallback.remove(listener);
    }

    public void clearOnAllweightsScanCallback() {
        mOnAllweightsScanCallback.clear();
    }

    public void stopScan() {
    }

    public void scan() throws AllweightsException {
        checkBluetooth();

        stopScan();

        mMainHandler.postDelayed(this::stopScan, SCAN_PERIOD);
    }

    @Override
    public void destroy() {
        super.destroy();
        mOnAllweightsScanCallback.clear();
    }
}
