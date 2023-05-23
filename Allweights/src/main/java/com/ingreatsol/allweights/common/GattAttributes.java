/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ingreatsol.allweights.common;

import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.UUID;

public class GattAttributes {
    private static final HashMap<UUID, String> attributes = new HashMap<>();
    public static UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static UUID SHOW_DATA = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_INFORMATION_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID MANUFACTURER_NAME = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");

    static {
        // Sample Services.
        attributes.put(HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(MANUFACTURER_NAME, "Manufacturer Name String");
        attributes.put(SERVICE_UUID, "Device Data");
        attributes.put(SHOW_DATA, "Show data");
        attributes.put(DEVICE_NAME, "Device Name");
    }

    public static String lookup(UUID uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    @NonNull
    public static IntentFilter makeBluetoothUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        return intentFilter;
    }
}
