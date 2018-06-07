package test.domain.com.bluetoothmodule.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

import test.domain.com.bluetoothmodule.Activities.BTAttendanceActivity;

public class BluetoothService {
    private static final String CONNECTION_NAME = "BT_CONNECTION";

    private static final UUID BT_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");

    private AcceptThread secureAcceptThread;
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private int state;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTED = 2;

    public BluetoothService(Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        this.handler = handler;
    }

    private synchronized void setState(int state) {
        this.state = state;
        handler.obtainMessage(BTAttendanceActivity.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void start() {
        setState(STATE_LISTEN);
        if (secureAcceptThread == null) {
            secureAcceptThread = new AcceptThread();
            secureAcceptThread.start();
        }
    }

    private synchronized void connected(BluetoothDevice device) {
        Message msg = handler.obtainMessage(BTAttendanceActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BTAttendanceActivity.DEVICE_NAME, device.getName());
        bundle.putString(BTAttendanceActivity.DEVICE_ADDRESS, device.getAddress());
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public synchronized void stop() {
        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        CONNECTION_NAME, BT_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (state != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (state) {
                            case STATE_LISTEN:
                                // start the connected thread.
                                connected(socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Not ready or already connected.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
