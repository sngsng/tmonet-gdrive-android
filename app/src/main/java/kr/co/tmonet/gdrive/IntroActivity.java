package kr.co.tmonet.gdrive;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import kr.co.tmonet.gdrive.databinding.ActivityIntroBinding;

public class IntroActivity extends BaseActivity {

    private ActivityIntroBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_intro);

        setUpViews();
        setUpActions();
    }

    @Override
    public void onBackPressed() {
        if (sBackPressedTime + BACK_PRESS_TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            showToast(getString(R.string.title_msg_press_back_again_to_exit));
        }
        sBackPressedTime = System.currentTimeMillis();
    }

    private void setUpViews() {
        // TODO Check isShareing state
        // TODO if isShareing -> Fill data
    }

    private void setUpActions() {
        mBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        setStartTransitionStyle(TransitionStyle.PushPop);
        finish();
    }
}
