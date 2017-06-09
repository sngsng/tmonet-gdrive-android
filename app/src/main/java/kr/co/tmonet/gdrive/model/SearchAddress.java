package kr.co.tmonet.gdrive.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.network.RestClient;
import kr.co.tmonet.gdrive.utils.ModelUtils;

/**
 * Created by Jessehj on 08/06/2017.
 */

public class SearchAddress implements Parcelable {

    private static final String LOG_TAG = SearchAddress.class.getSimpleName();

    private String mRoadAddress;    // 서울특별시 마포구 잔다리로3안길 51 (서교동, 서교 프라임빌)
    private String mRoadAddrPart1;  // 서울특별시 마포구 잔다리로3안길 51
    private String mRoadAddrPart2;  // (서교동, 서교 프라임빌)
    private String mJibunAddress;   // 서울특별시 마포구 서교동  395-133 서교 프라임빌
    private String mPostCode;       // 04043
    private String mSiNm;           // 서울특별시
    private String mSggNm;          // 마포구
    private String mEmdNm;          // 서교동

    public SearchAddress() {
    }

    public static SearchAddress getSearchAddressFromJson(JSONObject jsonObject) {
        SearchAddress address = new SearchAddress();

        try {
            if (jsonObject.has(APIConstants.AddressSearch.ROAD_ADDR) && !jsonObject.isNull(APIConstants.AddressSearch.ROAD_ADDR)) {
                String roadAddress = jsonObject.getString(APIConstants.AddressSearch.ROAD_ADDR);
                address.setRoadAddress(roadAddress);
            }
            if (jsonObject.has(APIConstants.AddressSearch.ROAD_ADDR_PART1) && !jsonObject.isNull(APIConstants.AddressSearch.ROAD_ADDR_PART1)) {
                String roadAddrPart1 = jsonObject.getString(APIConstants.AddressSearch.ROAD_ADDR_PART1);
                address.setRoadAddrPart1(roadAddrPart1);
            }
            if (jsonObject.has(APIConstants.AddressSearch.ROAD_ADDR_PART2) && !jsonObject.isNull(APIConstants.AddressSearch.ROAD_ADDR_PART2)) {
                String roadAddrPart2 = jsonObject.getString(APIConstants.AddressSearch.ROAD_ADDR_PART2);
                address.setRoadAddrPart2(roadAddrPart2);
            }
            if (jsonObject.has(APIConstants.AddressSearch.JIBUN_ADDR) && !jsonObject.isNull(APIConstants.AddressSearch.JIBUN_ADDR)) {
                String jibunAddress = jsonObject.getString(APIConstants.AddressSearch.JIBUN_ADDR);
                address.setJibunAddress(jibunAddress);
            }
            if (jsonObject.has(APIConstants.AddressSearch.ZIP_NO) && !jsonObject.isNull(APIConstants.AddressSearch.ZIP_NO)) {
                String postCode = jsonObject.getString(APIConstants.AddressSearch.ZIP_NO);
                address.setPostCode(postCode);
            }
            if (jsonObject.has(APIConstants.AddressSearch.SI_NM) && !jsonObject.isNull(APIConstants.AddressSearch.SI_NM)) {
                String siNm = jsonObject.getString(APIConstants.AddressSearch.SI_NM);
                address.setSiNm(siNm);
            }
            if (jsonObject.has(APIConstants.AddressSearch.SGG_NM) && !jsonObject.isNull(APIConstants.AddressSearch.SGG_NM)) {
                String sggNm = jsonObject.getString(APIConstants.AddressSearch.SGG_NM);
                address.setSggNm(sggNm);
            }
            if (jsonObject.has(APIConstants.AddressSearch.EMD_NM) && !jsonObject.isNull(APIConstants.AddressSearch.EMD_NM)) {
                String emdNm = jsonObject.getString(APIConstants.AddressSearch.EMD_NM);
                address.setEmdNm(emdNm);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return address;
    }

    public static void getSearchResults(Context context, String keyword, int page, final RestClient.RestListener listener) {
        final int COUNT_PER_PAGE = 20;
        JSONObject params = new JSONObject();

        try {
            params.put(APIConstants.AddressSearch.CONFM_KEY, context.getString(R.string.juso_api_key));
            params.put(APIConstants.AddressSearch.KEY_WORD, keyword);
            params.put(APIConstants.AddressSearch.CURRENT_PAGE, page);
            params.put(APIConstants.AddressSearch.COUNT_PER_PAGE, COUNT_PER_PAGE);
            params.put(APIConstants.AddressSearch.RESULT_TYPE, APIConstants.AddressSearch.RESULT_TYPE_JSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ModelUtils.getQueryAppendedUrlFromJson(APIConstants.AddressSearch.URL, params);

        Log.i(LOG_TAG, "search address url: " + url);
        RestClient restClient = new RestClient(context);
        restClient.strRequestGet(url, new RestClient.RestListener() {
            @Override
            public void onBefore() {
                listener.onBefore();
            }

            @Override
            public void onSuccess(Object response) {
                JSONObject responseJson = (JSONObject) response;
                Log.i(LOG_TAG, "response result: " + response.toString());

                try {
                    JSONObject result = responseJson.getJSONObject(APIConstants.AddressSearch.RESULTS);
                    JSONObject common = result.getJSONObject(APIConstants.AddressSearch.COMMON);
                    String errorCode = common.getString(APIConstants.AddressSearch.ERROR_CODE);

                    if (errorCode.equals("0")) {
                        listener.onSuccess(result);
                    } else {
                        listener.onSuccess(null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Error error = new Error("JSON Parsing Error");
                    listener.onFail(error);
                }
            }

            @Override
            public void onFail(Error error) {
                listener.onFail(error);
            }

            @Override
            public void onError(Error error) {
                listener.onError(error);
            }
        });
    }

    public String getRoadAddress() {
        return mRoadAddress;
    }

    public void setRoadAddress(String roadAddress) {
        mRoadAddress = roadAddress;
    }

    public String getRoadAddrPart1() {
        return mRoadAddrPart1;
    }

    public void setRoadAddrPart1(String roadAddrPart1) {
        mRoadAddrPart1 = roadAddrPart1;
    }

    public String getRoadAddrPart2() {
        return mRoadAddrPart2;
    }

    public void setRoadAddrPart2(String roadAddrPart2) {
        mRoadAddrPart2 = roadAddrPart2;
    }

    public String getJibunAddress() {
        return mJibunAddress;
    }

    public void setJibunAddress(String jibunAddress) {
        mJibunAddress = jibunAddress;
    }

    public String getPostCode() {
        return mPostCode;
    }

    public void setPostCode(String postCode) {
        mPostCode = postCode;
    }

    public String getSiNm() {
        return mSiNm;
    }

    public void setSiNm(String siNm) {
        mSiNm = siNm;
    }

    public String getSggNm() {
        return mSggNm;
    }

    public void setSggNm(String sggNm) {
        mSggNm = sggNm;
    }

    public String getEmdNm() {
        return mEmdNm;
    }

    public void setEmdNm(String emdNm) {
        mEmdNm = emdNm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mRoadAddress);
        dest.writeString(this.mRoadAddrPart1);
        dest.writeString(this.mRoadAddrPart2);
        dest.writeString(this.mJibunAddress);
        dest.writeString(this.mPostCode);
        dest.writeString(this.mSiNm);
        dest.writeString(this.mSggNm);
        dest.writeString(this.mEmdNm);
    }

    protected SearchAddress(Parcel in) {
        this.mRoadAddress = in.readString();
        this.mRoadAddrPart1 = in.readString();
        this.mRoadAddrPart2 = in.readString();
        this.mJibunAddress = in.readString();
        this.mPostCode = in.readString();
        this.mSiNm = in.readString();
        this.mSggNm = in.readString();
        this.mEmdNm = in.readString();
    }

    public static final Creator<SearchAddress> CREATOR = new Creator<SearchAddress>() {
        @Override
        public SearchAddress createFromParcel(Parcel source) {
            return new SearchAddress(source);
        }

        @Override
        public SearchAddress[] newArray(int size) {
            return new SearchAddress[size];
        }
    };
}
