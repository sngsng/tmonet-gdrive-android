package kr.co.tmonet.gdrive.network;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.NetworkError;
import com.android.volley.error.NoConnectionError;
import com.android.volley.error.ServerError;
import com.android.volley.error.TimeoutError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kr.co.tmonet.gdrive.R;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class RestClient {

    private static final String LOG_TAG = RestClient.class.getSimpleName();

    private static final String METHOD = "_method";
    private static final String METHOD_DELETE = "DELETE";
    private static final int TIME_OUT_MS = 10000;

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    private Context mContext;

    public RestClient(Context context) {
        mContext = context;
    }

    public void request(Method method, String subUrl, @Nullable JSONObject params, final RestListener listener) {

        // TODO setUrl
        String url = subUrl;
        jsonRequest(method, url, params, listener);
    }

    public void requestForGenericAPI(Method method, String url, @Nullable JSONObject params, final RestListener listener) {
        jsonRequest(method, url, params, listener);
    }

    private void jsonRequest(Method method, String url, @Nullable JSONObject params, final RestListener listener) {
        listener.onBefore();

        int volleyMethod = Request.Method.GET;

        switch (method) {
            case GET:
                volleyMethod = Request.Method.GET;
                break;
            case POST:
                volleyMethod = Request.Method.POST;
                break;
            case PUT:
                volleyMethod = Request.Method.PUT;
                break;
            case DELETE:
                volleyMethod = Request.Method.DELETE;
                try {
                    if (params == null) {
                        params = new JSONObject();
                    }
                    params.put(METHOD, METHOD_DELETE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(volleyMethod, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    if (error.networkResponse != null && error.networkResponse.statusCode > 0) {

                        if (error.networkResponse.statusCode < 500) {
                            Error err = new Error(error.getMessage());
                            listener.onFail(err);

                        } else {
                            Error err = new Error(mContext.getString(R.string.error_no_server));
                            listener.onError(err);
                        }

                    } else {
                        String msg = "";
                        if (error instanceof TimeoutError) {
                            msg = mContext.getString(R.string.error_timeout);
                        } else if (error instanceof NoConnectionError) {
                            msg = mContext.getString(R.string.error_no_connection);
                        } else if (error instanceof ServerError) {
                            msg = mContext.getString(R.string.error_no_server);
                        } else if (error instanceof NetworkError) {
                            msg = mContext.getString(R.string.error_no_network);
                        } else {
                            msg = mContext.getString(R.string.error_no_unexpected);
                        }
                        Error err = new Error(msg);
                        listener.onError(err);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                Log.d(LOG_TAG, "getHeaders: ");
                headers.put("appKey", mContext.getString(R.string.t_map_api_key));
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setShouldCache(false);
        RequestQueueManager.getInstance(mContext).getRequestQueue().add(jsonObjectRequest);
    }

    public interface RestListener {
        void onBefore();

        void onSuccess(Object response);

        void onFail(Error error);

        void onError(Error error);
    }
}
