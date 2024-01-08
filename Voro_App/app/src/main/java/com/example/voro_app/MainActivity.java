package com.example.voro_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


public class MainActivity extends AppCompatActivity implements BluetoothLEControllerListener {

    TextView console;
    int g_gait = 0;
    BluetoothLEController bluetoothLEController;
    Button connectButton;
    ImageButton disconnectButton;
    Button gaitButton;
    String deviceAddress;
    RobotControl robotControl;
    EditText stepsInput;
    Button walkButton;
    EditText walkText;
    private final static int WALK = 5;
    private final static int POSE = 4;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        console = findViewById(R.id.text);
        this.bluetoothLEController = BluetoothLEController.getInstance(this);
        checkBLESupport();
        checkPermissions();
        initConnectButton();
        initDisconnectButton();
        initGaitButton();
        initStepInput();
        connectButton.setEnabled(false);
        this.robotControl = new RobotControl(this.bluetoothLEController);
    }

    @Override
    protected void onStart () {
        super.onStart();
        showMenu(false);
        console.setText("ieškoma įrenginio");
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, 1);
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        this.bluetoothLEController.addBluetoothLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.bluetoothLEController.init();
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        this.bluetoothLEController.removeBluetoothLEControllerListener(this);
    }

    private void checkBLESupport () {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkPermissions () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "\"Access Fine Location\" permission missing", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 42);
        }
    }

    @Override
    public void BluetoothLEControllerConnected () {
        Toast.makeText(this, "Prisijungta prie roboto", Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void BluetoothLEControllerDisconnected () {
        Toast.makeText(this, "Atsijungta nuo roboto", Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
            }
        });
    }

    @Override
    public void BluetoothLEDeviceFound (String name, String address) {
        console.setText("Rastas irenginys " + name + " su adresu " + address);
        connectButton.setEnabled(true);
        this.deviceAddress = address;
    }

    private void initConnectButton () {
        this.connectButton = findViewById(R.id.connect);
        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("Prisijungta");
                bluetoothLEController.connectToDevice(deviceAddress);
                showStart(false);
                showMenu(true);
                robotControl.robotSend(POSE, 0);
            }
        });
    }

    private void initDisconnectButton () {
        this.disconnectButton = findViewById(R.id.disconnect);
        this.disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotControl.robotSend(POSE, 0);
                g_gait = 0;
                gaitButton.setText("Eisena: nepasirinkta");
                console.setText("Atsijungta");
                bluetoothLEController.disconnect();
                showStart(true);
                showMenu(false);
            }
        });
    }

    private void initGaitButton () {
        this.gaitButton = findViewById(R.id.gait);
        gaitButton.setText("Eisena: nepasirinkta");
        this.gaitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pop = new PopupMenu(MainActivity.this, v);
                pop.getMenuInflater().inflate(R.menu.gait_menu, pop.getMenu());
                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.gait1:
                                g_gait = 0;
                                gaitButton.setText("Eisena: nepasirinkta");
                                robotControl.robotSend(POSE, g_gait);
                                return true;
                            case R.id.gait2:
                                g_gait = 1;
                                gaitButton.setText("Eisena: Pirma");
                                robotControl.robotSend(POSE, g_gait);
                                return true;
                            case R.id.gait3:
                                g_gait = 2;
                                gaitButton.setText("Eisena: Antra");
                                robotControl.robotSend(POSE, g_gait);
                                return true;
                            case R.id.gait4:
                                g_gait = 3;
                                gaitButton.setText("Eisena: Trečia");
                                robotControl.robotSend(POSE, g_gait);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                pop.show();
            }
        });
    }

    private void initStepInput () {
        walkText = findViewById(R.id.editTextTextWalk);
        stepsInput = findViewById(R.id.editTextNumber);
        walkButton = findViewById(R.id.walk);
        this.walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (g_gait == 0) {
                    Toast.makeText(MainActivity.this, "Pasirinkite eiseną", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int stepNumber = Integer.parseInt(stepsInput.getText().toString());
                    robotControl.robotSend(WALK, stepNumber);
                    stepsInput.getText().clear();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "turite įvesti žingsnių skaičių", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void showMenu (boolean show) {
        int view = show == true ? View.VISIBLE : View.GONE;
        disconnectButton.setVisibility(view);
        gaitButton.setVisibility(view);
        walkText.setVisibility(view);
        stepsInput.setVisibility(view);
        walkButton.setVisibility(view);
    }

    private void showStart (boolean show) {
        int view = show == true ? View.VISIBLE : View.GONE;
        connectButton.setVisibility(view);
        console.setVisibility(view);
    }
}