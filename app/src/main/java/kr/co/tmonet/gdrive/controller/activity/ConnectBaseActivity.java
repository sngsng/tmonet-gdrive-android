package kr.co.tmonet.gdrive.controller.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Set;

import kr.co.tmonet.gdrive.network.AppService;
import kr.co.tmonet.gdrive.network.UsbService;

/**
 * Created by Jessehj on 21/06/2017.
 */

public class ConnectBaseActivity extends BaseActivity {

    private static final String LOG_TAG = ConnectBaseActivity.class.getSimpleName();

    public static final int REQ_CONNECT_DEVICE = 1;

    private UsbService mUsbService;
    private MyHandler mHandler;
    private AppService mAppService = new AppService(this);

    public ResultActionListener mResultActionListener;

    public void setResultActionListener(ResultActionListener resultActionListener) {
        mResultActionListener = resultActionListener;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final ServiceConnection mUsbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUsbService = ((UsbService.UsbBinder) service).getService();
            mUsbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUsbService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new MyHandler(ConnectBaseActivity.this);

//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!editText.getText().toString().equals("")) {
//                    String data = editText.getText().toString();
//                    if (usbService != null) { // if UsbService was correctly binded, Send data
//                        usbService.write(data.getBytes());
//                    }
//                }
//            }
//        });

        setUpAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, mUsbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(mUsbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;
        private String tempFullText = "AT@CARINFO=2,44,28,20,0,0,234,0,24,45";
        private String receivedText = "";

        public MyHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;

                    receivedText = receivedText + data;
                    showToast(receivedText);
                    Log.i(LOG_TAG, "receivedData: " + data);
                    Log.i(LOG_TAG, "receivedText: " + receivedText);
                    if (data.equals("\r") || data.equals(0x0D) || data.equals(13) || data.contains("E")|| receivedText.equals(tempFullText)) {
                        Log.i("HANDLE", "equals!!");
                        Toast.makeText(mActivity.get(), "data: " + data, Toast.LENGTH_SHORT);

                        mAppService.setCallback(new AppService.ResponseCallback() {
                            @Override
                            public void onRequestNewCommand(String requestCmd) {
//                                byte[] requestData = DataConvertUtils.convertAsciiToBytes(requestCmd);
                                Log.i(LOG_TAG, "requestCmd: " + requestCmd);
                                mUsbService.write(requestCmd.getBytes());
                                mUsbService.write(mAppService.returnOK().getBytes());
                            }

                            @Override
                            public void onPowerOff() {
                                ActivityCompat.finishAffinity(ConnectBaseActivity.this);
                                System.runFinalizersOnExit(true);
                                System.exit(0);
                            }

                            @Override
                            public void onSynchronizeTime(Calendar calendar) {
                                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                am.setTime(calendar.getTimeInMillis());
                            }

                            @Override
                            public void returnOK(AppService.ActionType actionType) {
                                Log.i("HANDLE", "return ok!!");
                                mUsbService.write(mAppService.returnOK().getBytes());

                                if (mResultActionListener != null) {
                                    mResultActionListener.onResultAction(actionType);
                                }
                            }
                        });

                        mAppService.checkResponseCommand(receivedText);
                        receivedText = "";
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void setUpAction() {

        mAppService.setReqCallback(new AppService.RequestCallback() {
            @Override
            public void onRequestNewCommand(String requestCmd) {
                if (mUsbService != null) {
                    Log.i(LOG_TAG, "reqNewCmd: " + requestCmd);
                    mUsbService.write(requestCmd.getBytes());
//                    mUsbService.write(mAppService.returnOK().getBytes());
                }
            }
        });
    }

    public void sendEvent(int eventCode){
        AppService appService = new AppService();
        appService.requestEventCommand(eventCode, null, new AppService.RequestCallback() {
            @Override
            public void onRequestNewCommand(String requestCmd) {
                Log.i(LOG_TAG, "requestCmd: " + requestCmd);
                mUsbService.write(requestCmd.getBytes());
            }
        });


    }

    public interface ResultActionListener {
        void onResultAction(AppService.ActionType actionType);
    }
}
