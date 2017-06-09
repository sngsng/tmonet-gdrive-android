package kr.co.tmonet.gdrive.view.helper;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Jessehj on 08/06/2017.
 */

public abstract class ViewHelper {

    protected AppCompatActivity mActivity;
    protected View mRootView;

    public ViewHelper(AppCompatActivity activity) {
        mActivity = activity;
    }

    public ViewHelper(AppCompatActivity activity, View rootView) {
        mActivity = activity;
        mRootView = rootView;
    }
}
