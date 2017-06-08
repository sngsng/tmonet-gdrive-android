package kr.co.tmonet.gdrive.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class RequestQueueManager {
    
    private static RequestQueueManager sVolleyManager;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    public static synchronized RequestQueueManager getInstance(Context context) {
        if (sVolleyManager == null) {
            sVolleyManager = new RequestQueueManager(context);
        }
        return sVolleyManager;
    }

    private RequestQueueManager(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }
}
