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

package com.ingreatsol.allweights;

import android.content.IntentFilter;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.UUID;

class GattAttributes {
    private static final HashMap<UUID, String> attributes = new HashMap<>();
    public static UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static UUID HEART_RATE_MEASUREMENT2 = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID INFORMACION_DISPOSITIVO = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_GATT_CONNECTED = "com.ingreatsol.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "com.ingreatsol.bluetooth.le.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "com.ingreatsol.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.ingreatsol.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.ingreatsol.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.ingreatsol.bluetooth.le.EXTRA_DATA";

    static {
        // Sample Services.
        attributes.put(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"), "Heart Rate Service");
        attributes.put(INFORMACION_DISPOSITIVO, "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"), "Manufacturer Name String");
        attributes.put(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), "DATOS DEL DISPOSTIVO");
        attributes.put(HEART_RATE_MEASUREMENT2, "MOSTAR DATOS");
        attributes.put(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"), "NOMBRE DEL DISPOSTIVO");
    }

    public static String lookup(UUID uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    @NonNull
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_CONNECTING);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
