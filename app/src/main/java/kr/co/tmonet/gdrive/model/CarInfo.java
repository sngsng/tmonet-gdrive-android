package kr.co.tmonet.gdrive.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jessehj on 13/06/2017.
 */

public class CarInfo implements Parcelable {

    private static final String LOG_TAG = CarInfo.class.getSimpleName();

    private int mCarType;               // 차종
    private double mFuelEfficiency;     // 연비 (km/kW)
    private double mCarBettery;         // 차량 배터리 용량 (kWh)
    private double mRemainBettery;      // 현재 배터리 잔량 (%)
    private int mChargeState;           // 차량 충전상태 (연결 1, 충전중 2, 오류 3, 미충전 0)
    private int mCO;                    // CO (ppm)
    private int mCO2;                   // CO2 (ppm)
    private int mVolatility;            // 휘발성 (ppm)
    private double mTemperature;        // 온도 ('C)
    private double mHumidity;           // 습도 (%)

    public CarInfo() {
    }

    public int getCarType() {
        return mCarType;
    }

    public void setCarType(int carType) {
        mCarType = carType;
    }

    public double getFuelEfficiency() {
        return mFuelEfficiency;
    }

    public void setFuelEfficiency(double fuelEfficiency) {
        mFuelEfficiency = fuelEfficiency;
    }

    public double getCarBettery() {
        return mCarBettery;
    }

    public void setCarBettery(double carBettery) {
        mCarBettery = carBettery;
    }

    public double getRemainBettery() {
        return mRemainBettery;
    }

    public void setRemainBettery(double remainBettery) {
        mRemainBettery = remainBettery;
    }

    public int getChargeState() {
        return mChargeState;
    }

    public void setChargeState(int chargeState) {
        mChargeState = chargeState;
    }

    public int getCO() {
        return mCO;
    }

    public void setCO(int CO) {
        mCO = CO;
    }

    public int getCO2() {
        return mCO2;
    }

    public void setCO2(int CO2) {
        mCO2 = CO2;
    }

    public int getVolatility() {
        return mVolatility;
    }

    public void setVolatility(int volatility) {
        mVolatility = volatility;
    }

    public double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCarType);
        dest.writeDouble(this.mFuelEfficiency);
        dest.writeDouble(this.mCarBettery);
        dest.writeDouble(this.mRemainBettery);
        dest.writeInt(this.mChargeState);
        dest.writeInt(this.mCO);
        dest.writeInt(this.mCO2);
        dest.writeInt(this.mVolatility);
        dest.writeDouble(this.mTemperature);
        dest.writeDouble(this.mHumidity);
    }

    protected CarInfo(Parcel in) {
        this.mCarType = in.readInt();
        this.mFuelEfficiency = in.readDouble();
        this.mCarBettery = in.readDouble();
        this.mRemainBettery = in.readDouble();
        this.mChargeState = in.readInt();
        this.mCO = in.readInt();
        this.mCO2 = in.readInt();
        this.mVolatility = in.readInt();
        this.mTemperature = in.readDouble();
        this.mHumidity = in.readDouble();
    }

    public static final Creator<CarInfo> CREATOR = new Creator<CarInfo>() {
        @Override
        public CarInfo createFromParcel(Parcel source) {
            return new CarInfo(source);
        }

        @Override
        public CarInfo[] newArray(int size) {
            return new CarInfo[size];
        }
    };


}
