package kr.co.tmonet.gdrive.network;

import android.app.Activity;
import android.util.Log;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.Charger;
import kr.co.tmonet.gdrive.model.UserInfo;
import kr.co.tmonet.gdrive.network.APIConstants.Command;

import static kr.co.tmonet.gdrive.utils.DataConvertUtils.calcCheckSum;
import static kr.co.tmonet.gdrive.utils.DataConvertUtils.convertAsciiToBytes;

/**
 * Created by Jessehj on 20/06/2017.
 */

public class AppService {

    private static final String LOG_TAG = AppService.class.getSimpleName();

    private Activity mActivity;
    private ResponseCallback mResCallback;
    private RequestCallback mReqCallback;
    private ActionType mActionType;

    public enum ActionType {
        CarInfo,
        UsrInfo,
        Charger
    }

    public AppService(Activity activity) {
        mActivity = activity;
    }

    public void setResCallback(ResponseCallback resCallback) {
        mResCallback = resCallback;
    }

    public void setReqCallback(RequestCallback reqCallback) {
        mReqCallback = reqCallback;
    }

    public void checkResponseCommand(String cmd) {
        Log.i(LOG_TAG, "cmd : + " + cmd);
        Log.i(LOG_TAG, "cmd length: " + cmd.length());

        if (cmd.contains(Command.SIGN_READ)) {
            // READ ?
            Log.i(LOG_TAG, "read!!!");
            checkReadResponse(cmd);

        } else if (cmd.contains(Command.SIGN_WRITE)) {
            // WRITE =
            Log.i(LOG_TAG, "write!!!");
            checkWriteCommand(cmd);
        }


    }

    public void requestEventCommand(int eventCode, JSONObject params, RequestCallback reqCallback) {
        Log.i(LOG_TAG, "reqEvent: " + eventCode);
        String requestCmd = "";
        StringBuilder stringBuilder;
        switch (eventCode) {
            case 1:     // 이용정보 조회
                requestCmd = Command.EVENT1;
                stringBuilder = new StringBuilder(requestCmd).append(Command.CR);

                if (mReqCallback != null) {
                    mReqCallback.onRequestNewCommand(stringBuilder.toString());
                }
                break;
            case 2:     // 충전소 조회
                double lat = SettingManager.getInstance().getCurrentLatitude();
                double lng = SettingManager.getInstance().getCurrentLongitude();
                requestCmd = Command.EVENT2 + "," + lat + "," + lng;
                Log.i(LOG_TAG, "reqCmd: " + requestCmd);

                stringBuilder = new StringBuilder(requestCmd).append(Command.CR);

                if (mReqCallback != null) {
                    reqCallback.onRequestNewCommand(stringBuilder.toString());
                }

                break;
            case 3:     // 현재시간 요청
                requestCmd = Command.EVENT3 + Command.CR;
                if (mReqCallback != null) {
                    mReqCallback.onRequestNewCommand(requestCmd);
                }

                break;
            case 4:     // GPS 좌표 조회
                requestCmd = Command.EVENT4 + Command.CR;

                if (mReqCallback != null) {
                    mReqCallback.onRequestNewCommand(requestCmd);
                }
                break;
            case 5:     // 차량정보 요청
                requestCmd = Command.EVENT5 + Command.CR;
                if (mReqCallback != null) {
                    mReqCallback.onRequestNewCommand(requestCmd);
                }
                break;
            default:
                break;
        }
    }

    public void requestNotify(int notiCode) {
        String requestCmd = "";

        switch (notiCode) {
            case 0:
                requestCmd = Command.NOTI0 + Command.CR;
                break;
            case 1:
                requestCmd = Command.NOTI1 + Command.CR;
                break;
            default:
                break;
        }

        if (mResCallback != null) {
            mResCallback.onRequestNewCommand(requestCmd);
        }
    }

    private void checkReadResponse(String responseCmd) {       // ?
        String requestCmd = "";
        if (responseCmd.contains(Command.USRINFO)) {
            UserInfo userInfo = ModelManager.getInstance().getGlobalInfo().getUserInfo();

            if (userInfo != null) {
                requestCmd = Command.USRINFO_READ + userInfo.getUseNum() + "," + userInfo.getStartAt() + "," + userInfo.getEndAt();
            } else {
                requestCmd = Command.USRINFO_READ + "0,0,0";
            }

            requestCmd += Command.SIGN_CHECKSUM_START + calcCheckSum(requestCmd) + Command.CR;

            if (mResCallback != null) {
                mResCallback.onRequestNewCommand(requestCmd);
            }

        } else if (responseCmd.contains(Command.PWOFF)) {

            if (mResCallback != null) {
                mResCallback.onPowerOff();
            }

        } else if (responseCmd.contains(Command.CHARGER)) {


        } else if (responseCmd.contains(Command.TIME)) {
            SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDhhmmss", Locale.KOREA);
            Date curTime = new Date();
            String formattedTime = formatter.format(curTime);

            requestCmd = Command.TIME_READ + formattedTime;
            requestCmd += calcCheckSum(requestCmd) + Command.CR;


            if (mResCallback != null) {
                mResCallback.onRequestNewCommand(requestCmd);
            }

        } else if (responseCmd.contains(Command.GPS)) {
            Log.i(LOG_TAG, " READ GPS");
            double lat = SettingManager.getInstance().getCurrentLatitude();
            double lng = SettingManager.getInstance().getCurrentLongitude();

            requestCmd = Command.GPS_READ + lat + "," + lng;
//            requestCmd += calcCheckSum(requestCmd) + Command.CR;
            Log.i(LOG_TAG, "requestCmd(GPS) : " + requestCmd);
            StringBuilder stringBuilder = new StringBuilder(requestCmd).append(Command.CR);

            if (mResCallback != null) {
                mResCallback.onRequestNewCommand(stringBuilder.toString());
            }

        } else if (responseCmd.contains(Command.CARINFO)) {

        }
    }

    private void checkWriteCommand(String cmd) {      // =
        if (cmd.contains(Command.USRINFO)) {
            Log.i(LOG_TAG, "if write user??!!!");
            UserInfo userInfo = new UserInfo();

            String response = cmd.substring(11, cmd.length() - 4);
            if (response.contains(",")) {
                String[] responseDatas = response.split(",");

                userInfo.setUseNum(Long.parseLong(responseDatas[0]));

                String startAt = responseDatas[1];
                String endAt = responseDatas[2];

                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDhhmmss", Locale.KOREA);
                    Date startDate = formatter.parse(startAt);
                    Date endDate = formatter.parse(endAt);

                    userInfo.setStartAt(startDate);
                    userInfo.setEndAt(endDate);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            ModelManager.getInstance().updateGlobalInfo(userInfo);
            mActionType = ActionType.UsrInfo;

        } else if (cmd.contains(Command.PWOFF)) {

        } else if (cmd.contains(Command.CHARGER)) {

            final ArrayList<Charger> chargers = new ArrayList<>();

            String response;

            response = cmd.substring(cmd.indexOf(Command.SIGN_WRITE) + 1);

            Log.i(LOG_TAG, "responseStr : " + response);
            double curLatitude = SettingManager.getInstance().getCurrentLatitude();
            double curLongitude = SettingManager.getInstance().getCurrentLongitude();

            if (response.contains(",")) {
                final String[] responseDatas = response.split(",");
                int totalCnt = Integer.parseInt(responseDatas[0]);

                for (int i = 0; i < totalCnt; i++) {

                    try {

                        Charger charger = new Charger();

                        String name = responseDatas[(i * 5) + 3].trim();
                        charger.setName(name);
                        charger.setLat(Double.parseDouble(responseDatas[(i * 5) + 4].trim()));
                        charger.setLng(Double.parseDouble(responseDatas[(i * 5) + 5].trim()));
                        charger.setChargeable(Integer.parseInt(responseDatas[(i * 5) + 6]));
                        charger.setOptInfo(Integer.parseInt(responseDatas[(i * 5) + 7]));
                        chargers.add(charger);

                        Log.i(LOG_TAG, "charger: " + charger.toString());
                        ModelManager.getInstance().setChargers(chargers);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
            mActionType = ActionType.Charger;

        } else if (cmd.contains(Command.TIME)) {
            String response = cmd.substring(8, cmd.length() - 4);
            int year = Integer.parseInt(response.substring(0, 4));
            int month = Integer.parseInt(response.substring(4, 6));
            int date = Integer.parseInt(response.substring(6, 8));
            int hour = Integer.parseInt(response.substring(8, 10));
            int min = Integer.parseInt(response.substring(10, 12));
            int sec = Integer.parseInt(response.substring(12, 14));

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, date, hour, min, sec);

            if (mResCallback != null) {
                mResCallback.onSynchronizeTime(calendar);
            }

        } else if (cmd.contains(Command.GPS)) {

        } else if (cmd.contains(Command.CARINFO)) {
            Log.i(LOG_TAG, "if write carinfo??!!!");
            CarInfo carInfo = new CarInfo();


            String response = cmd.substring(cmd.indexOf(Command.SIGN_WRITE) + 1);

//            if (cmd.contains("\r"))
//
//            else if (cmd.contains("E")) {
//                int index = cmd.indexOf("E");
//                Log.i(LOG_TAG, "index: " + index);
//                response = cmd.substring(11, cmd.indexOf("E"));
//            } else {
//                response = cmd.substring(11);
//            }

            Log.i(LOG_TAG, "responseStr : " + response);


            if (response.contains(",")) {
                String[] responseDatas = response.split(",");

                try {
                    carInfo.setCarType(Integer.parseInt(responseDatas[0]));
                    carInfo.setFuelEfficiency(Double.parseDouble(responseDatas[1]));
                    carInfo.setCarBettery(Double.parseDouble(responseDatas[2]));
                    carInfo.setRemainBettery(Double.parseDouble(responseDatas[3]));
                    carInfo.setChargeState(Integer.parseInt(responseDatas[4]));
                    carInfo.setCO(Integer.parseInt(responseDatas[5]));
                    carInfo.setCO2(Integer.parseInt(responseDatas[6]));
                    carInfo.setVolatility(Integer.parseInt(responseDatas[7]));
                    carInfo.setTemperature(Double.parseDouble(responseDatas[8]));
                    carInfo.setHumidity(Double.parseDouble(responseDatas[9]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ModelManager.getInstance().updateGlobalInfo(carInfo);
            mActionType = ActionType.CarInfo;
        }

        if (mResCallback != null) {
            Log.i(LOG_TAG, "returnok " + mActionType);
            mResCallback.returnOK(mActionType);
        }
    }

    private byte[] carriageReturn() {
        return convertAsciiToBytes(Command.CR);
    }

    public String returnOK() {
        StringBuilder stringBuilder = new StringBuilder(Command.OK).append(Command.CR);
        return stringBuilder.toString();
//        return convertAsciiToBytes(Command.OK + Command.CR);
    }

    public interface ResponseCallback {
        void onRequestNewCommand(String requestCmd);

        void onPowerOff();

        void onSynchronizeTime(Calendar calendar);

        void returnOK(ActionType actionType);
    }

    public interface RequestCallback {
        void onRequestNewCommand(String requestCmd);
    }
}
