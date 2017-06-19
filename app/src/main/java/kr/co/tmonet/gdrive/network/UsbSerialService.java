package kr.co.tmonet.gdrive.network;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jessehj on 19/06/2017.
 */

public class UsbSerialService extends Service {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public static final String ACTION_USB_READY = "kr.co.tmonet.gdrive.action.USB_READY";
    public static final String ACTION_USB_NOT_SUPPORTED = "kr.co.tmonet.gdrive.action.USB_NOT_SUPPORTED";
    public static final String ACTION_USB_NONE = "kr.co.tmonet.gdrive.action.USB_NONE";
    public static final String ACTION_USB_DISCONNECTED = "kr.co.tmonet.gdrive.action.USB_DISCONNECTED";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "kr.co.tmonet.gdrive.action.USB_DEVICE_NOT_WORKING";

    public static final String ACTION_USB_PERMISSION_GRANTED = "kr.co.tmonet.gdrive.action.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "kr.co.tmonet.gdrive.action.USB_PERMISSION_NOT_GRANTED";

    public static final String ACTION_USB_ATTACHED = UsbManager.ACTION_USB_DEVICE_ATTACHED;
    public static final String ACTION_USB_DETACHED = UsbManager.ACTION_USB_DEVICE_DETACHED;

    public static final int MESSAGE_FROM_SERIAL_PORT = 0;

    private static final int BAUD_RATE = 14400;
    public static boolean SERVICE_CONNECTED = false;

    private IBinder mBinder = new UsbBinder();

    private Context mContext;
    private Handler mHandler;
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mConnection;
    private UsbSerialDevice mSerialPort;

    private boolean mSerialPortConnected = false;

    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            try {
                String data = new String(bytes, "UTF-8");
                if (mHandler != null) {
                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, data).sendToTarget();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    // User accepted our USB connection. Try to open the device as a serial port
                    Intent i = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    context.sendBroadcast(i);
                    mConnection = mUsbManager.openDevice(mUsbDevice);
                    new ConnectionThread().start();
                } else {
                    // User not accepted our USB connection. Send an Intent to the Main Activity
                    Intent i = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    context.sendBroadcast(i);
                }
            } else if (intent.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!mSerialPortConnected) {
                    // A USB device has been attached. Try to open it as a Serial Port
                    findSerialPortDevice();
                }
            } else if (intent.getAction().equals(ACTION_USB_DETACHED)) {
                // USB device was disconnected. Send an intent to the Main Activity
                Intent i = new Intent(ACTION_USB_DISCONNECTED);
                context.sendBroadcast(i);
                if (mSerialPortConnected) {
                    mSerialPort.close();
                }
                mSerialPortConnected = false;
            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */

    @Override
    public void onCreate() {
        mContext = this;
        mSerialPortConnected = false;
        UsbSerialService.SERVICE_CONNECTED = true;
        setFilter();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsbSerialService.SERVICE_CONNECTED = false;
    }

    /*
     * This function will be called from MainActivity to write data through Serial Port
     */
    public void write(byte[] data) {
        if (mSerialPort != null) {
            mSerialPort.write(data);
        }
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                mUsbDevice = entry.getValue();
                int deviceVID = mUsbDevice.getVendorId();
                int devicePID = mUsbDevice.getProductId();

                if (deviceVID == 0x2A03) {
                    requestUserPermission();
                    keep = false;
                } else {
                    mConnection = null;
                    mUsbDevice = null;
                }

                if (!keep) {
                    break;
                }
            }
            if (!keep) {
                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_USB_NONE);
                sendBroadcast(intent);
            }
        } else {
            // There is no USB devices connected. Send an intent to MainActivity.
            Intent intent = new Intent(ACTION_USB_NONE);
            sendBroadcast(intent);
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbManager.requestPermission(mUsbDevice, pi);
    }

    public class UsbBinder extends Binder {
        public UsbSerialService getService() {
            return UsbSerialService.this;
        }
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            mSerialPort = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, mConnection);
            if (mSerialPort != null) {
                if (mSerialPort.open()) {
                    mSerialPortConnected = true;
                    mSerialPort.setBaudRate(BAUD_RATE);
                    mSerialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    mSerialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    mSerialPort.setParity(UsbSerialInterface.PARITY_NONE);

                    /**
                     * Current flow control Options:
                     * UsbSerialInterface.FLOW_CONTROL_OFF
                     * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
                     * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
                     */
                    mSerialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    mSerialPort.read(mCallback);

                    // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
                    // to be uploaded or not
                    //Thread.sleep(2000); // sleep some. YMMV with different chips.


                    // Everything went as expected. Send an intent to MainActivity
                    Intent intent = new Intent(ACTION_USB_READY);
                    mContext.sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (mSerialPort instanceof CDCSerialDevice) {

                        // CDC DRIVER NOT WORKING
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        mContext.sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                mContext.sendBroadcast(intent);
            }
        }
    }
}
