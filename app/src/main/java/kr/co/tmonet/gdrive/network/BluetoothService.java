package kr.co.tmonet.gdrive.network;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import kr.co.tmonet.gdrive.controller.activity.DeviceListActivity;
import kr.co.tmonet.gdrive.controller.activity.IntroActivity;

/**
 * Created by Jessehj on 14/06/2017.
 */

public class BluetoothService {

    private static final String LOG_TAG = BluetoothService.class.getSimpleName();
    private static final UUID BLUETOOTH_PROTOCOL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int STATE_NONE = 0;        // doing nothing
    private static final int STATE_LISTEN = 1;      // listening for incoming connections
    private static final int STATE_CONNECTING = 2;  // initiating an outgoing connection
    private static final int STATE_CONNECTED = 3;   // connected to a remote device

    private BluetoothAdapter mBtAdatper;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private Activity mActivity;
    private Handler mHandler;
    private int mState = 0;


    public BluetoothService(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;

        mBtAdatper = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Check the Bluetooth support
     *
     * @return boolean
     */
    public boolean getDeviceState() {
        Log.i(LOG_TAG, "Check the Bluetooth support");
        if (mBtAdatper == null) {
            Log.i(LOG_TAG, "Bluetooth is not available");
            return false;

        } else {
            Log.i(LOG_TAG, "Bluetooth is available");
            return true;
        }
    }

    /**
     * Check the enabled Bluetooth
     */
    public void enableBluetooth() {
        Log.i(LOG_TAG, "Check the enabled Bluetooth");

        if (mBtAdatper.isEnabled()) {
            // Bluetooth state is ON :
            Log.i(LOG_TAG, "Bluetooth Enable Now");

            scanDevice();

        } else {
            // Bluetooth state is OFF :
            Log.i(LOG_TAG, "Bluetooth Enable Request");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(intent, IntroActivity.REQ_ENABLE_BLUETOOTH);
        }
    }

    /**
     * Available device search
     */
    public void scanDevice() {
        Log.i(LOG_TAG, "Scan Device");

        Intent intent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(intent, IntroActivity.REQ_CONNECT_DEVICE);
    }

    /**
     * Request data for connect device
     */
    public void getDeviceInfo(Intent data) {
        // GET the device MAC Address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // GET the BluetoothDevice object
        BluetoothDevice device = mBtAdatper.getRemoteDevice(address);

        Log.i(LOG_TAG, "Get Device info(address): " + address);
        connect(device);
    }

    // Set Bluetooth state
    private synchronized void setState(int state) {
        Log.i(LOG_TAG, "setState() : " + mState + " -> " + state);
        mState = state;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.i(LOG_TAG, "Start: ");

        // Cancel any thread attempting to make a connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // ConnectThread 초기화 : Device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device) {
        Log.i(LOG_TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.i(LOG_TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    // 모든 thread stop
    public synchronized void stop() {
        Log.i(LOG_TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    // 값을 쓰는 부분(보내는부분)
    public void write(byte[] out) {         // Create temporary object
        ConnectedThread r;                  // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }                                   // Perfom the write unsynchronized r.write(out);
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
    }

    private void connectionLost() {
        setState(STATE_LISTEN);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket tmp = null;

            // 디바이스 정보를 얻어서 BluetoothSocket 생성
            try {
                tmp = device.createRfcommSocketToServiceRecord(BLUETOOTH_PROTOCOL_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        public void run() {
            Log.i(LOG_TAG, "BEGIN ConnectThread");
            setName("ConnectThread");

            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            mBtAdatper.cancelDiscovery();

            // BluetoothSocket 연결 시도
            try {
                // BluetoothSocket 연결 시도에 대한 return 값은 success 또는 exception 이다.
                mSocket.connect();
                Log.i(LOG_TAG, "Connect Success");
            } catch (IOException e) {
                connectionFailed();   // 연결 실패시 불러오는 메소드
                Log.i(LOG_TAG, "Connect Fail");

                // socket을 닫는다
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.i(LOG_TAG, "Unable to close() socket during connection failure");
                    e2.printStackTrace();
                }

                // 연결중 혹은 연결 대기상태인 메소드를 호출
                BluetoothService.this.start();
                return;
            }

            // ConnectThread 클래스를 reset 한다
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // ConnectThread를 시작한다.
            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.i(LOG_TAG, "close() of connect socket failed");
                e.printStackTrace();

            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.i(LOG_TAG, "Create ConnectedThread");
            mSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            // BluetoothSocket의 InputStream과 OutputStream을 얻는다.
            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(LOG_TAG, "temp sockets not created");
                e.printStackTrace();
            }
            mInputStream = tempIn;
            mOutputStream = tempOut;
        }

        public void run() {
            Log.i(LOG_TAG, "BEGIN ConnectetdThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // InputStream으로부터 값을 받아 읽는 부분(값을 받는다)
                    bytes = mInputStream.read(buffer);
                } catch (IOException e) {
                    Log.i(LOG_TAG, "disconnected");
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutputStream.
         *
         * @param buffer :The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                // 값을 쓰는 부분(값을 보낸다)
                mOutputStream.write(buffer);

            } catch (IOException e) {
                Log.i(LOG_TAG, "Exception during write");
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
