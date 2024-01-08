package com.example.voro_app;

public interface BluetoothLEControllerListener {
    public void BluetoothLEControllerConnected();
    public void BluetoothLEControllerDisconnected();
    public void BluetoothLEDeviceFound(String name, String address);
}