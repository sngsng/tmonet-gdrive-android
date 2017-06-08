package kr.co.tmonet.gdrive.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class RequestHelper {

    private static final String LOG_TAG = RequestHelper.class.getSimpleName();

    public void requestRouteTimeWithDistance(Context context, double startLat, double startLng, double endLat, double endLng, final RestClient.RestListener listener) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(APIConstants.TMap.API_ROOT_AUTHORITY)
                .appendPath(APIConstants.TMap.TMAP)
                .appendPath(APIConstants.TMap.ROUTES)
                .appendQueryParameter(APIConstants.TMap.VERSION, "1")
                .appendQueryParameter(APIConstants.TMap.START_X, Double.toString(startLat))
                .appendQueryParameter(APIConstants.TMap.START_Y, Double.toString(startLng))
                .appendQueryParameter(APIConstants.TMap.END_X, Double.toString(endLat))
                .appendQueryParameter(APIConstants.TMap.END_Y, Double.toString(endLng))
                .appendQueryParameter(APIConstants.TMap.COORD_TYPE, APIConstants.TMap.COORD_TYPE_VALUE);

        String routesUrl = builder.toString();

        RestClient restClient = new RestClient(context);
        restClient.request(RestClient.Method.POST, routesUrl, null, new RestClient.RestListener() {
            @Override
            public void onBefore() {
                listener.onBefore();
            }

            @Override
            public void onSuccess(Object response) {
                Log.i(LOG_TAG, "RequestRouteTimeWithDistance: " + response.toString());
                JSONObject responseJson = (JSONObject) response;
                try {
                    JSONArray features = responseJson.getJSONArray(APIConstants.TMap.FEATURES);
                    if(features.length() > 0) {
                        JSONObject feature = features.getJSONObject(0);
                        JSONObject properties = feature.getJSONObject(APIConstants.TMap.PROPERTIES);
                        listener.onSuccess(properties);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
}
