package com.example.voro_app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BluetoothLEController {
    private static BluetoothLEController instance;
    private BluetoothManager bluetoothManager;
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();
    private BluetoothLeScanner scanner;
    private BluetoothDevice device;
    private ArrayList<BluetoothLEControllerListener> listeners = new ArrayList<>();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    public void init () {
        this.devices.clear();
        this.scanner = this.bluetoothManager.getAdapter().getBluetoothLeScanner();
        scanner.startScan(bleCallback);
    }

    private BluetoothLEController (Context context) {
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }
    public static BluetoothLEController getInstance (Context context) {
        if (null == instance) {
            instance = new BluetoothLEController(context);
        }
        return instance;
    }

    public void addBluetoothLEControllerListener (BluetoothLEControllerListener l) {
        if (!this.listeners.contains(l)) {
            this.listeners.add(l);
        }
    }

    public void removeBluetoothLEControllerListener (BluetoothLEControllerListener l) {
        this.listeners.remove(l);
    }

    private void fireDisconnected () {
        for (BluetoothLEControllerListener l : this.listeners) {
            l.BluetoothLEControllerDisconnected();
        }
        this.device = null;
    }
    private void fireConnected () {
        for (BluetoothLEControllerListener l : this.listeners) {
            l.BluetoothLEControllerConnected();
        }
    }

    private void fireDeviceFound (BluetoothDevice device) {
        for (BluetoothLEControllerListener l : this.listeners) {
            l.BluetoothLEDeviceFound(device.getName().trim(), device.getAddress());
        }
    }

    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult (int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                deviceFound(device);
            }
        }

        @Override
        public void onBatchScanResults (List<ScanResult> results) {
            for(ScanResult sr : results) {
                BluetoothDevice device = sr.getDevice();
                if(!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                    deviceFound(device);
                }
            }
        }

        @Override
        public void onScanFailed (int errorCode) {
            Log.i("[BLE]", "scan failed with errorcode: " + errorCode);
        }
    };

    private boolean isThisTheDevice (BluetoothDevice device) {
        return null != device.getName() && device.getName().startsWith("BT");
    }

    private void deviceFound (BluetoothDevice device) {
        this.devices.put(device.getAddress(), device);
        fireDeviceFound(device);
    }

    public void connectToDevice (String address) {
        this.device = this.devices.get(address);
        this.scanner.stopScan(this.bleCallback);
        this.bluetoothGatt = device.connectGatt(null, false, this.bleConnectCallback);
    }

    private final BluetoothGattCallback bleConnectCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange (BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothGattCharacteristic = null;
                fireDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered (BluetoothGatt gatt, int status) {
            if (null == bluetoothGattCharacteristic) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().toUpperCase().startsWith("0000FFE0")) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic bgc : gattCharacteristics) {
                            if (bgc.getUuid().toString().toUpperCase().startsWith("0000FFE1")) {
                                int chprop = bgc.getProperties();
                                if (((chprop & BluetoothGattCharacteristic.PROPERTY_WRITE) | (chprop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                                    bluetoothGattCharacteristic = bgc;
                                    fireConnected();
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    public void disconnect() {
        this.bluetoothGatt.disconnect();
    }

    public void sendData (byte [] data) {
        this.bluetoothGattCharacteristic.setValue(data);
        bluetoothGatt.writeCharacteristic(this.bluetoothGattCharacteristic);
    }

}
