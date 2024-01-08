package com.example.voro_app;

import android.text.Editable;
import android.widget.Toast;

public class RobotControl {
    private final static byte START = 0;
    private BluetoothLEController bluetoothLEController;

    public RobotControl (BluetoothLEController bluetoothLEController) {
        this.bluetoothLEController = bluetoothLEController;
    }

    private byte [] createRobotCommand (byte commandNumber, byte ... args) {
        byte [] command = new byte[args.length + 3];
        command[0] = START;
        command[1] = commandNumber;
        for (int i = 0; i < args.length; i++) {
            command[i+2] = args[i];
        }
        return command;
    }

    public void robotSend (int command, int value) {
        this.bluetoothLEController.sendData(createRobotCommand((byte)command, (byte)value));
    }
}
