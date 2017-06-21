package kr.co.tmonet.gdrive.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Jessehj on 13/06/2017.
 */

public class UserInfo implements Parcelable {

    private static final String LOG_TAG = UserInfo.class.getSimpleName();

    private long mUseNum;       // 이용번호 20YYMMDDhhmmss (20170321000002)
    private Date mStartAt;      // 시작시간 YYMMDDhhmmss
    private Date mEndAt;        // 종료시간 YYMMDDhhmmss
    private double mReturnLat;  // 반납거점 위도 (mm.ssssssss)
    private double mReturnLng;  // 반납거점 경도 (mmm.ssssssss)

    public UserInfo() {
    }

    public long getUseNum() {
        return mUseNum;
    }

    public void setUseNum(long useNum) {
        mUseNum = useNum;
    }

    public Date getStartAt() {
        return mStartAt;
    }

    public void setStartAt(Date startAt) {
        mStartAt = startAt;
    }

    public Date getEndAt() {
        return mEndAt;
    }

    public void setEndAt(Date endAt) {
        mEndAt = endAt;
    }

    public double getReturnLat() {
        return mReturnLat;
    }

    public void setReturnLat(double returnLat) {
        mReturnLat = returnLat;
    }

    public double getReturnLng() {
        return mReturnLng;
    }

    public void setReturnLng(double returnLng) {
        mReturnLng = returnLng;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUseNum);
        dest.writeLong(this.mStartAt != null ? this.mStartAt.getTime() : -1);
        dest.writeLong(this.mEndAt != null ? this.mEndAt.getTime() : -1);
        dest.writeDouble(this.mReturnLat);
        dest.writeDouble(this.mReturnLng);
    }

    protected UserInfo(Parcel in) {
        this.mUseNum = in.readLong();
        long tmpMStartAt = in.readLong();
        this.mStartAt = tmpMStartAt == -1 ? null : new Date(tmpMStartAt);
        long tmpMEndAt = in.readLong();
        this.mEndAt = tmpMEndAt == -1 ? null : new Date(tmpMEndAt);
        this.mReturnLat = in.readDouble();
        this.mReturnLng = in.readDouble();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public String toString() {
        return "UserInfo{" +
                "mUseNum=" + mUseNum +
                ", mStartAt=" + mStartAt +
                ", mEndAt=" + mEndAt +
                ", mReturnLat=" + mReturnLat +
                ", mReturnLng=" + mReturnLng +
                '}';
    }
}
