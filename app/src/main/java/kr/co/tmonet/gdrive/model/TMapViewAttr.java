package kr.co.tmonet.gdrive.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jessehj on 12/06/2017.
 */

public class TMapViewAttr implements Parcelable {

    private int mZoomLevel;
    private double mLocateLat;
    private double mLocateLng;

    public TMapViewAttr() {
    }

    public int getZoomLevel() {
        return mZoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        mZoomLevel = zoomLevel;
    }

    public double getLocateLat() {
        return mLocateLat;
    }

    public void setLocateLat(double locateLat) {
        mLocateLat = locateLat;
    }

    public double getLocateLng() {
        return mLocateLng;
    }

    public void setLocateLng(double locateLng) {
        mLocateLng = locateLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mZoomLevel);
        dest.writeDouble(this.mLocateLat);
        dest.writeDouble(this.mLocateLng);
    }

    protected TMapViewAttr(Parcel in) {
        this.mZoomLevel = in.readInt();
        this.mLocateLat = in.readDouble();
        this.mLocateLng = in.readDouble();
    }

    public static final Creator<TMapViewAttr> CREATOR = new Creator<TMapViewAttr>() {
        @Override
        public TMapViewAttr createFromParcel(Parcel source) {
            return new TMapViewAttr(source);
        }

        @Override
        public TMapViewAttr[] newArray(int size) {
            return new TMapViewAttr[size];
        }
    };

    @Override
    public String toString() {
        return "TMapViewAttr{" +
                "mZoomLevel=" + mZoomLevel +
                ", mLocateLat=" + mLocateLat +
                ", mLocateLng=" + mLocateLng +
                '}';
    }
}
