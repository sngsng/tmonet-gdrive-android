package kr.co.tmonet.gdrive.controller.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.FragmentAlertDialogBinding;

/**
 * Created by Jessehj on 13/06/2017.
 */

public class AlertDialogFragment extends DialogFragment {

    private static final String ARG_DESTINATION_DISTANCE = "argDestinationDistance";
    private static final String ARG_RUNNABLE_DISTANCE = "argRunnableDistance";

    private static final String LOG_TAG = AlertDialogFragment.class.getSimpleName();
    private FragmentAlertDialogBinding mBinding;
    private OnFragmentInteractionListener mListener;

    private String mDestinationDistance;
    private String mRunnableDistance;

    public AlertDialogFragment() {
    }

    public static AlertDialogFragment newInstance(String destDistance, String runnableDistance) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DESTINATION_DISTANCE, destDistance);
        args.putString(ARG_RUNNABLE_DISTANCE, runnableDistance);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mDestinationDistance = getArguments().getString(ARG_DESTINATION_DISTANCE);
            mRunnableDistance = getArguments().getString(ARG_RUNNABLE_DISTANCE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().getAttributes().windowAnimations = R.style.AppDialogAnimation;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_alert_dialog, container, false);
        View rootView = mBinding.getRoot();
        setUpViews();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpViews() {
        mBinding.alertMessageTextView.setText(String.format(Locale.KOREA, getString(R.string.title_msg_ask_add_way_point_format), mDestinationDistance, mRunnableDistance));

        mBinding.submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAlertSubmitClick();
                }
                dismiss();
            }
        });

        mBinding.cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAlertCancelClick();
                }
                dismiss();
            }
        });

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mListener != null) {

                    mListener.onAlertCancelClick();
                }
                return false;
            }
        });
    }

    public interface OnFragmentInteractionListener {

        void onAlertSubmitClick();

        void onAlertCancelClick();
    }
}
