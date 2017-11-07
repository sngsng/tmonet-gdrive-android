package kr.co.tmonet.gdrive.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import kr.co.tmonet.gdrive.R;

/**
 * Created by jessehj on 11/6/17.
 */

public class Charger implements Parcelable {

    private String mName;
    private double mLat;
    private double mLng;
    private int mChargeable;
    private int mOptInfo;       // default 0

    private String mDistance;

    public Charger() {
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        mLng = lng;
    }

    public int getChargeable() {
        return mChargeable;
    }

    public void setChargeable(int chargeable) {
        mChargeable = chargeable;
    }

    public int getOptInfo() {
        return mOptInfo;
    }

    public void setOptInfo(int optInfo) {
        mOptInfo = optInfo;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public String getChargeableText(Context context) {
        switch (mChargeable) {
            case 0:
                return context.getString(R.string.title_chargeable);
            case 1:
                return context.getString(R.string.title_charging_other);
            case 2:
                return context.getString(R.string.title_parking_other);
            case 3:
                return context.getString(R.string.title_charge_unusable);
            default:
                return "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeDouble(this.mLat);
        dest.writeDouble(this.mLng);
        dest.writeInt(this.mChargeable);
        dest.writeInt(this.mOptInfo);
        dest.writeString(this.mDistance);
    }

    protected Charger(Parcel in) {
        this.mName = in.readString();
        this.mLat = in.readDouble();
        this.mLng = in.readDouble();
        this.mChargeable = in.readInt();
        this.mOptInfo = in.readInt();
        this.mDistance = in.readString();
    }

    public static final Creator<Charger> CREATOR = new Creator<Charger>() {
        @Override
        public Charger createFromParcel(Parcel source) {
            return new Charger(source);
        }

        @Override
        public Charger[] newArray(int size) {
            return new Charger[size];
        }
    };
}
