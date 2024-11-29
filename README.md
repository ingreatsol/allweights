# allweights
Library for connecting Allweights scales to Android applications

## Using
### Gradle file
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
        repositories {
                ...
                maven { url 'https://jitpack.io' }
        }
}
```
In the `gradle` file of the `app` module add the `allweights` library
```gradle
implementation 'com.github.ingreatsol:allweights:version'
```
`version` refers to the latest version of the library, for example.
```gradle
implementation 'com.github.ingreatsol:allweights:2.0.0'
```
### AndroidManifestFile
The library requires location and blueooth permissions to work. In the manifest add the following permissions.
```xml
<uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
<uses-permission
    android:name="android.permission.BLUETOOTH_ADMIN"
    android:maxSdkVersion="30" />
<uses-permission
    android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<uses-feature
    android:name="android.hardware.bluetooth"
    android:required="false" />
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="false" />

<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
## Scan Allweights
To connect to Allweights we need a bluetooth object `BluetoothDevice`, for this we must implement a bluetooth device search to find the bluetooth of the scale.

To do this, the first thing to do is to create a `layout` file (name listitem_device.xml) that will contain each of the devices that the library finds.
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="2dp"
    android:orientation="vertical"
    android:padding="10dp">

    <TextView
        android:id="@+id/device_name"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Device name"
        android:textAlignment="center"
        android:textSize="28sp" />

    <TextView
        android:id="@+id/device_address"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Device address"
        android:textAlignment="center"
        android:textSize="16sp" />
</LinearLayout>
```
In a fragment or activity, instantiate the `AllweightsScan` class in `onCreate` that receives the context of the activity and the layout we created earlier.

`AllweightsScan` is an abstract class that can be implemented with `AllweightsBluetoothScan` for classic bluetooth or `AllweightsBleScan` for low energy bluetooth (ble).
```xml
AllweightsScan bluetoothScan = new AllweightsBluetoothScan(requireActivity());

LeDeviceListAdapter mLeDeviceListAdapter = new LeDeviceListAdapter();

bluetoothScan.addOnScanDeviceListener(device -> mLeDeviceListAdapter.addDevice(device));
```
To start the bluetooths scan, you have to use the `scan` method, and to stop it, with the `stopScan` method (you must have enabled all the permissions needed to perform the scan before).
```java
bluetoothScan.scan(this);
bluetoothScan.stopScan();
```
To know the scan status, the `addOnScanStatusChangeListener` method is used, which receives a `ScanStatusChangeListener` to get the changes in the scan status.
```java
bluetoothScan.addOnScanStatusChangeListener(status -> {
    binding.progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    binding.button.setVisibility(status ? View.GONE : View.VISIBLE);
});
```
In the xml file of the fragment or activity you have to add a list where the bluetooth devices will be represented.
```xml
<ListView
        android:id="@+id/dispositivos"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_gravity="center_horizontal"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="8dp"
        android:choiceMode="singleChoice"
        android:textAlignment="center"
        app:layout_constraintBottom_toTopOf="@+id/progressBar"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.0"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

<ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_gravity="center"
        app:layout_constraintBottom_toTopOf="@+id/button"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Escanear"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
```
And assign the `Adapter` object of the library to that list
```java
binding.dispositivos.setOnItemClickListener((parent, _view, position, id) -> {
    try {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

        if (device == null) return;

        bluetoothScan.stopScan();

        Bundle b = new Bundle();
        b.putString("deviceAddress", device.getAddress());
        b.putInt("deviceType", device.getType());

        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment, b);
    } catch (Exception e) {
        e.printStackTrace();
    }
});

binding.dispositivos.setAdapter(mLeDeviceListAdapter);

binding.button.setOnClickListener(l -> scanear());
```
Override methods 
```java
@Override
    public void onResume() {
        super.onResume();
        bluetoothScan.scan();
    }
    
    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        bluetoothScan.stopScan();
    }
```

## Connect to allweights
To connect to allweights you have to instantiate the `AllweightsConnect` object and assign the mac and device type previously passed with the `setDevice` method.

AllweightsConnect is an abstract class that can be implemented with `AllweightsBluetoothConnect` for classic bluetooth or `AllweightsBleightsConnect` for low energy (ble) bluetooth.
```java
// In fragments paste this code in the `onCreateView` method
        
String deviceAddres = getArguments().getString("deviceAddress");
        int deviceType = getArguments().getInt("deviceType");

if (deviceType == 1) {
    allweightsConnect = new AllweightsBluetoothConnect(requireActivity());
} else {
    allweightsConnect = new AllweightsBleConnect(requireActivity());
}

allweightsConnect.setDevice(deviceAddres, deviceType);
```
```java
//In activities paste this code in the `onCreate` method

String deviceAddres = getIntent().getExtras().getString("deviceAddress");
int deviceType = getIntent().getExtras().getInt("deviceType");
AllweightsConnect allweightsConnect = null;
if (deviceType == 1) {
    allweightsConnect = new AllweightsBluetoothConnect(this);
} else {
    allweightsConnect = new AllweightsBleConnect(this);
}

allweightsConnect.setDevice(deviceAddres, deviceType);
```
To initiate the connection, use the `connect` method in `onResume` and the `disconnect` method in `onPause`.
```
//in onResume
allweightsConnect.connect();
//in onPause
allweightsConnect.disconnect();
//in onDestroy
allweightsConnect.destroy();
```
### Method `addOnDataChangeListener`
The `addOnDataChangeListener` method receives a `DataChangeListener` that receives the updates of the balance weights.
```java
private final AllweightsConnect.DataChangeListener dataChangeListener = data -> {
        binding.textviewPeso.setText(data.weight.toString());
        if (Boolean.TRUE.equals(data.isEnergyConnected)) {
            binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            if (data.bateryPercent != null && RANGO_MINIMO_BATERIA < data.bateryPercent) {
                binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else {
                binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
            }
        }
        if (data.bateryPercent != null) {
            binding.progressbar.setProgress((int) (((data.bateryPercent - RANGO_MINIMO_BATERIA) / LIMITE_BATERIA) * 100));
        }
    };
    
//in onResume
allweightsConnect.addOnDataChangeListener(dataChangeListener);
//in onPause
allweightsConnect.removeOnDataChangeListener(dataChangeListener);
```
The `DataChangeListener` uses a method that returns an `AllweightsData` object containing the information obtained from the bullet:
 
1.  `weight`: A numeric value indicating the current weight of the scale.
2.  `isEnergyConnected`: A boolean value indicating whether the scale is connected to electricity.
3.  `bateryPercent`: A numeric value indicating the battery charge.

### Method `getConnectionStatus`
The `getConnectionStatus` method returns a `LiveData` object to which a observer must be assigned to receive the connection status between the allweights scale and the android device.
```java
    private final AllweightsConnect.ConnectionStatusChangeListener statusChangeListener =
            status -> binding.textViewEstado.setText(status.toString());
      
// in onResume
allweightsConnect.addOnConnectionStatusChangeListener(statusChangeListener);
// in onPause
allweightsConnect.removeOnConnectionStatusChangeListener(statusChangeListener);
```
The observer sends an `enum` object `ConnectionStatus` which can be:
1. `CONNECTED`: The device is connected to the scale and is receiving data.
2. `DISCONNECTED`: The device is not connected to the scale and has to be reconnected with the `registerService` method.
3. `CONNECTING`: The device is connecting to the scale but is not fully connected yet.
