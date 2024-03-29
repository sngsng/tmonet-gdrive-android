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
import com.android.volley.request.StringRequest;

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
        jsonRequest(method, url, params, null, listener);
    }

    public void requestForTMapAPI(Method method, String url, @Nullable JSONObject params, final RestListener listener) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(APIConstants.TMap.APP_KEY, mContext.getString(R.string.t_map_api_key));
        headers.put(APIConstants.TMap.ACCEPT, APIConstants.TMap.APPLICATION_JSON);
        jsonRequest(method, url, params, headers, listener);
    }

    public void requestForSearchAddress(String url, final RestListener listener) {
        listener.onBefore();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.startsWith("(") && response.endsWith(")")) {
                    response = response.substring(1, response.length() - 1);
                    Log.i(LOG_TAG, response);

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        listener.onSuccess(jsonResponse);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                checkError(error, listener);
            }
        });
        RequestQueueManager.getInstance(mContext).getRequestQueue().add(stringRequest);

    }



    private void jsonRequest(Method method, String url, @Nullable JSONObject params, final HashMap<String, String> headers, final RestListener listener) {
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
                Log.i(LOG_TAG, "response: " + response.toString());
                listener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                checkError(error, listener);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (headers != null) {
                    return headers;
                } else {
                    return super.getHeaders();
                }
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonObjectRequest.setShouldCache(false);
        RequestQueueManager.getInstance(mContext).getRequestQueue().add(jsonObjectRequest);
    }

    private void checkError(VolleyError error, RestListener listener) {
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

    public interface RestListener {
        void onBefore();

        void onSuccess(Object response);

        void onFail(Error error);

        void onError(Error error);
    }
}
