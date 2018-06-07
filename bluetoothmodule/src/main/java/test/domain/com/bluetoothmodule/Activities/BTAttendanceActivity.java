package test.domain.com.bluetoothmodule.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import test.domain.com.bluetoothmodule.Services.BluetoothService;
import test.domain.com.bluetoothmodule.R;

public class BTAttendanceActivity extends Activity{

    private static final int REQUEST_ENABLE_BT = 0;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_TOAST = 3;
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_ADDRESS = "deviceAddress";
    public static final String TOAST = "toast";

    private TextView tvStatus;
    private Button btnSwitch;
    private BluetoothService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btattendance);
        tvStatus = findViewById(R.id.tvStatus);
        btnSwitch = findViewById(R.id.btnSwitch);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(btnSwitch.getText().toString().equalsIgnoreCase("Stop Attendance Registration")){
                    if(service != null){
                        service.stop();
                        btnSwitch.setText("Start Attendance Registration");
                        tvStatus.setText("");
                    }
                }else {
                    checkBluetooth();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void checkBluetooth(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(BTAttendanceActivity.this, "No bluetooth adapter found.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!bluetoothAdapter.isEnabled()){
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, REQUEST_ENABLE_BT);
        }
        else {
            startService();
        }
    }

    @SuppressLint("SetTextI18n")
    private void startService(){
        if(service == null){
            service = new BluetoothService(btHandler);
        }
        if (service.getState() == BluetoothService.STATE_NONE) {
            service.start();
        }
        btnSwitch.setText("Stop Attendance Registration");
    }

    private Handler btHandler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:

                            break;
                        case BluetoothService.STATE_LISTEN:
                            tvStatus.setText("listening....");
                            break;
                    }
                    break;

                case MESSAGE_DEVICE_NAME:
                    Toast.makeText(BTAttendanceActivity.this,"Attendance Registered for "+ msg.getData().getString(DEVICE_ADDRESS), Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    btnSwitch.setText("Start Attendance Registration");
                    service.stop();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startService();
            } else {
                Toast.makeText(BTAttendanceActivity.this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
