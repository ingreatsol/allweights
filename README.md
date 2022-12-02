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
In addition, the following service should be added.
```xml
<service
    android:name="com.ingreatsol.allweights.AllweightsBluetoothLeService"
    android:enabled="true"
    android:exported="false" />
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
In a fragment or activity, instantiate the `AllweightsScan` class and execute the `init` method in `onCreate` that receives the context of the activity and the layout we created earlier.
```xml
AllweightsScan bluetoothScan = new AllweightsScan();

bluetoothScan.init(this,
        R.layout.listitem_device,
        R.id.device_address,
        R.id.device_name);
```
To start the bluetooths scan, you have to use the `scan` method, and to stop it, with the `stopScan` method (you must have enabled all the permissions needed to perform the scan before).
```java
bluetoothScan.scan(this);
bluetoothScan.stopScan();
```


## Connect to allweights
To connect to allweights you have to instantiate the `AllweightsConnect` object and initialize the allweights service that receives data from the scale with the `init` method, sending as parameter the context of the activity and the `BluetoothDevice` to which it is going to connect.
```java
// In fragments paste this code in the `onCreateView` method
        
AllweightsConnect   allweightsConnect = new AllweightsConnect();

BluetoothDevice device = getArguments().getParcelable("device");

allweightsConnect.init(requireActivity(), device);
```
```java
//In activities paste this code in the `onCreate` method

AllweightsConnect allweightsConnect = new AllweightsConnect();

BluetoothDevice device = getIntent().getExtras().getParcelable("device");

allweightsConnect.init(this, device);
```
### Method `getData`
The `getData` method returns a `LiveData` object to which an observer must be assigned to update the weights of the scales.
```java
private final Observer<AllweightsData> dataObserver = new Observer<AllweightsData>() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onChanged(@NonNull AllweightsData allweightsData) {
            //Here are the weights of the allweights scale
            Toast.makeText(requireActivity(), allweightsData.toString(), Toast.LENGTH_LONG).show();
        }
    };
    
//in onResume
allweightsConnect.getData().observe(this, dataObserver);
//in onPause
allweightsConnect.getData().removeObserver(dataObserver);
```
The observer sends an `AllweightsData` object which contains:
 
1.  `weight`: A numeric value indicating the current weight of the scale.
2.  `isEnergyConnected`: A boolean value indicating whether the scale is connected to electricity.
3.  `bateryPercent`: A numeric value indicating the battery charge.

### Method `registerService`
The `registerService` method tells the previously started service to start receiving data and send it to the `getData` observer.

```java
// in onResume
allweightsConnect.registerService(this);
// in onPause
allweightsConnect.unRegisterService(this);
//in onDestroy
allweightsConnect.destroyService(this);
```
### Method `getConnectionStatus`
The `getConnectionStatus` method returns a `LiveData` object to which a observer must be assigned to receive the connection status between the allweights scale and the android device.
```java
private final Observer<ConnectionStatus> estadoConexionObserve = new Observer<ConnectionStatus>() {
        @Override
        public void onChanged(@NonNull ConnectionStatus estado) {
            Toast.makeText(requireActivity(), estado.toString(), Toast.LENGTH_LONG).show();
        }
    };
      
// in onResume
allweightsConnect.getConnectionStatus().observe(this, estadoConexionObserve);
// in onPause
allweightsConnect.getConnectionStatus().removeObserver(estadoConexionObserve);
```
The observer sends an `enum` object `ConnectionStatus` which can be:
1. `CONNECTED`: The device is connected to the scale and is receiving data.
2. `DISCONNECTED`: The device is not connected to the scale and has to be reconnected with the `registerService` method.
3. `CONNECTING`: The device is connecting to the scale but is not fully connected yet.
