package kr.co.tmonet.gdrive.controller.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ListItemSearchAddressBinding;
import kr.co.tmonet.gdrive.model.SearchAddress;

/**
 * Created by Jessehj on 08/06/2017.
 */

public class SearchAddressAdapter extends RecyclerView.Adapter<SearchAddressAdapter.AddressItemViewHolder> {

    private static final String LOG_TAG = SearchAddressAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<SearchAddress> mSearchAddresses = new ArrayList<>();
    private AddressItemClickListener mItemClickListener;

    public SearchAddressAdapter(Context context) {
        mContext = context;
    }

    public void setSearchAddresses(ArrayList<SearchAddress> searchAddresses) {
        mSearchAddresses = searchAddresses;
    }

    public void setItemClickListener(AddressItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    @Override
    public AddressItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_address, parent, false);
        return new AddressItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AddressItemViewHolder holder, int position) {
        ListItemSearchAddressBinding holding = holder.mBinding;

        SearchAddress address = mSearchAddresses.get(position);

        if (address.getRoadAddress() != null) {
            holding.searchResultTextView.setText(address.getRoadAddress());
        }
    }

    @Override
    public int getItemCount() {
        return mSearchAddresses.size();
    }

    public class AddressItemViewHolder extends RecyclerView.ViewHolder {
        ListItemSearchAddressBinding mBinding;

        public AddressItemViewHolder(View itemView) {
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

    public interface AddressItemClickListener {
        void onStationItemClick(int position);
    }
}
