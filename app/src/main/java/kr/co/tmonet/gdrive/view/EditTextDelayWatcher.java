package kr.co.tmonet.gdrive.view;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jessehj on 08/06/2017.
 */

public abstract class EditTextDelayWatcher implements TextWatcher {

    private Timer mTimer = new Timer();
    private final long DELAY = 500;

    public EditTextDelayWatcher() {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(final Editable s) {
        final Handler handler = new Handler();
        mTimer.cancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onTextChangeDelayed(s.toString());
                    }
                });
            }
        }, DELAY);
    }

    public abstract void onTextChangeDelayed(String input);
}
