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
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.controller.adapter.ChargeStationAdapter;
import kr.co.tmonet.gdrive.utils.DialogUtils;
import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.FragmentChargeListDialogBinding;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class ChargeListDialogFragment extends DialogFragment {

    private static final String ARG_CHARGE_STATION_ITEMS = "argChargeStationItems";
    private static final String ARG_SET_AS_WAY_POINT = "argSetAsWayPoint";

    private ArrayList<ChargeStation> mChargeStations = new ArrayList<>();
    private FragmentChargeListDialogBinding mBinding;
    private ChargeStationAdapter mChargeStationAdapter;
    private OnFragmentInteractionListener mListener;
    private boolean mIsWayPoint = false;

    public ChargeListDialogFragment() {
    }

    public static ChargeListDialogFragment newInstance(ArrayList<ChargeStation> chargeStations, boolean isWayPoint) {
        ChargeListDialogFragment fragment = new ChargeListDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CHARGE_STATION_ITEMS, chargeStations);
        args.putBoolean(ARG_SET_AS_WAY_POINT, isWayPoint);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChargeStations = getArguments().getParcelableArrayList(ARG_CHARGE_STATION_ITEMS);
            mIsWayPoint = getArguments().getBoolean(ARG_SET_AS_WAY_POINT);
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
            dialog.getWindow().setGravity(Gravity.TOP | Gravity.END);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.x = DialogUtils.CHARGE_LIST_DIALOG_MARGIN;
            layoutParams.y = DialogUtils.CHARGE_LIST_DIALOG_MARGIN;
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_charge_list_dialog, container, false);
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
        mChargeStationAdapter = new ChargeStationAdapter(getActivity());
        mBinding.chargeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.chargeRecyclerView.setAdapter(mChargeStationAdapter);

        updateList();
        setUpActions();
    }

    private void updateList() {
        mChargeStationAdapter.setChargeStations(mChargeStations);
        mChargeStationAdapter.notifyDataSetChanged();
    }

    private void setUpActions() {
        mChargeStationAdapter.setItemClickListener(new ChargeStationAdapter.StationItemClickListener() {
            @Override
            public void onStationItemClick(int position) {
                if (mListener != null) {
                    mListener.onStationItemClick(position, mIsWayPoint);
                }
                dismiss();
            }
        });
        mBinding.closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onStationDialogCancelClick(mIsWayPoint);
                }
                dismiss();
            }
        });

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && mListener != null) {
                    mListener.onStationDialogCancelClick(mIsWayPoint);
                }
                return false;
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onStationItemClick(int position, boolean isWayPoint);

        void onStationDialogCancelClick(boolean isWayPoint);
    }
}
