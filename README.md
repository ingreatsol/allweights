# allweights
Library for connecting Allweights scales to Android applications

## Using
### Gradle file
In the `gradle` file of the `app` module add the `allweights` library
```gradle
implementation 'com.github.ingreatsol:allweights:version'
```
`version` refers to the latest version of the library, for example.
```gradle
implementation 'com.github.ingreatsol:allweights:1.0.0'
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
