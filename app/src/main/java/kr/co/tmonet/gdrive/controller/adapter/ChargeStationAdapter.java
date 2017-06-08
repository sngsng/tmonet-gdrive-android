package kr.co.tmonet.gdrive.controller.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ListItemChargeStationBinding;
import kr.co.tmonet.gdrive.model.ChargeStation;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class ChargeStationAdapter extends RecyclerView.Adapter<ChargeStationAdapter.StationItemViewHolder> {

    private static final String LOG_TAG = ChargeStationAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<ChargeStation> mChargeStations = new ArrayList<>();
    private StationItemClickListener mItemClickListener;

    public ChargeStationAdapter(Context context) {
        mContext = context;
    }

    public void setChargeStations(ArrayList<ChargeStation> chargeStations) {
        mChargeStations = chargeStations;
    }

    public void setItemClickListener(StationItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    @Override
    public StationItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_charge_station, parent, false);
        return new StationItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StationItemViewHolder holder, int position) {
        ListItemChargeStationBinding holding = holder.mBinding;

        ChargeStation station = mChargeStations.get(position);

        if (station.getName() != null) {
            holding.stationNameTextView.setText(station.getName());
        }
        if (station.getDistance() != null) {
            holding.stationDistanceTextView.setText(station.getDistance());
        }

        if (station.isChargeable()) {
            holding.chargeableTextView.setText(mContext.getString(R.string.title_o));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holding.chargeableTextView.setTextColor(mContext.getResources().getColor(R.color.colorSkyBlue, mContext.getTheme()));
            } else {
                holding.chargeableTextView.setTextColor(mContext.getResources().getColor(R.color.colorSkyBlue));
            }
        } else {
            holding.chargeableTextView.setText(mContext.getString(R.string.title_x));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holding.chargeableTextView.setTextColor(mContext.getResources().getColor(R.color.colorGrapeFruit, mContext.getTheme()));
            } else {
                holding.chargeableTextView.setTextColor(mContext.getResources().getColor(R.color.colorGrapeFruit));
            }
        }

        if (position == getItemCount() - 1) {

            holding.dividerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChargeStations.size();
    }

    public class StationItemViewHolder extends RecyclerView.ViewHolder {
        ListItemChargeStationBinding mBinding;

        public StationItemViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onStationItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface StationItemClickListener {
        void onStationItemClick(int position);
    }

}
