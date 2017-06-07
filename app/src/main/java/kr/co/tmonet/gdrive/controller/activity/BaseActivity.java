package kr.co.tmonet.gdrive.controller.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.view.RotateProgressDialog;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    public static final int BACK_PRESS_TIME_DELAY = 2000;
    public static long sBackPressedTime;

    public enum TransitionStyle {
        Modal,
        PushPop,
        None
    }

    private TransitionStyle mTransitionStyle = TransitionStyle.PushPop;
    private RotateProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.i(LOG_TAG, "saveInstanceState : OnCreate");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void finish() {
        super.finish();
        switch (mTransitionStyle) {
            case Modal:
                overridePendingTransition(R.anim.scale_up, R.anim.modal_close);
                break;
            case PushPop:
                overridePendingTransition(R.anim.scale_up, R.anim.slide_out_to_right);
                break;
            case None:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    public void finish(TransitionStyle transitionStyle) {
        mTransitionStyle = transitionStyle;
        finish();
    }

    public void setFinishTransitionStyle(TransitionStyle transitionStyle) {
        mTransitionStyle = transitionStyle;
    }

    public void setStartTransitionStyle(TransitionStyle transitionStyle) {
        switch (transitionStyle) {
            case Modal:
                overridePendingTransition(R.anim.modal_open, R.anim.scale_down);
                break;
            case PushPop:
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.scale_down);
                break;
            case None:
                break;
        }
    }

    public void enableToolbar(int toolbarResId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarResId);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showSnackbar(String msg) {
        ViewGroup snackbarContainer = (ViewGroup) findViewById(R.id.snackbar_container);
        if (snackbarContainer != null) {
            Snackbar.make(snackbarContainer, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new RotateProgressDialog(this);
            if (mProgressDialog.getWindow() != null && mProgressDialog.getWindow().getAttributes() != null) {
                mProgressDialog.getWindow().getAttributes().windowAnimations = R.style.AppProgressDialogAnimation;
            }
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void checkToCloseApp() {
        if (sBackPressedTime + BACK_PRESS_TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            showToast(getString(R.string.title_msg_press_back_again_to_exit));
        }
        sBackPressedTime = System.currentTimeMillis();
    }
}
