package kr.co.tmonet.gdrive.view.helper;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.adapter.SearchAddressAdapter;
import kr.co.tmonet.gdrive.databinding.FragmentSearchAddressBinding;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.view.EditTextDelayWatcher;
import kr.co.tmonet.gdrive.view.RecyclerPaginationListener;

/**
 * Created by Jessehj on 08/06/2017.
 */

public class SearchAddressDialogFragmentHelper extends ViewHelper {

    private FragmentSearchAddressBinding mBinding;
    private SearchAddressAdapter mAdapter;
    private EventCallback mEventCallback;
    private RecyclerPaginationListener mPaginationListener;

    public SearchAddressDialogFragmentHelper(AppCompatActivity activity, View rootView, FragmentSearchAddressBinding binding) {
        super(activity, rootView);
        mBinding = binding;

        setUpList();
        setUpActions();
    }

    public void setEventCallback(EventCallback eventCallback) {
        mEventCallback = eventCallback;
    }

    public void setPaginationEnable(boolean enable) {
        mPaginationListener.setPagingEnabled(enable);
    }

    public void updateTotalCount(String totalCount) {
        mBinding.resultCountTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_search_count_format), totalCount));
    }

    public void updateList(ArrayList<SearchAddress> searchResults) {
        mBinding.emptyLayout.setVisibility(searchResults.isEmpty() ? View.VISIBLE : View.GONE);
        mBinding.searchRecyclerView.setVisibility(searchResults.isEmpty() ? View.GONE : View.VISIBLE);

        mAdapter.setSearchAddresses(searchResults);
        mAdapter.notifyDataSetChanged();
    }

    private void setUpList() {
        mBinding.searchEditText.requestFocus();

        mAdapter = new SearchAddressAdapter(mActivity);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mPaginationListener = new RecyclerPaginationListener(linearLayoutManager) {
            @Override
            public void onLoadMore() {
                if (mEventCallback != null) {
                    mEventCallback.onLoadMore();
                }
            }
        };

        mBinding.searchRecyclerView.setLayoutManager(linearLayoutManager);
        mBinding.searchRecyclerView.addOnScrollListener(mPaginationListener);
        mBinding.searchRecyclerView.setAdapter(mAdapter);
    }

    private void setUpActions() {
        mAdapter.setItemClickListener(new SearchAddressAdapter.AddressItemClickListener() {
            @Override
            public void onStationItemClick(int position) {
                if (mEventCallback != null) {
                    mEventCallback.onAddressItemClick(position);
                }
            }
        });

        mBinding.searchEditText.addTextChangedListener(new EditTextDelayWatcher() {
            @Override
            public void onTextChangeDelayed(String input) {
                mBinding.searchClearImageView.setVisibility(input.isEmpty() ? View.GONE : View.VISIBLE);
                if (mEventCallback != null) {
                    mEventCallback.onSearchTyping(input);
                }
            }
        });

        mBinding.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mEventCallback != null) {
                    mEventCallback.onSearchSubmit(v.getText().toString());
                }
                return false;
            }
        });

        mBinding.searchClearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.searchEditText.setText("");
            }
        });
        mBinding.closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEventCallback != null) {
                    mEventCallback.onDismissDialog();
                }
            }
        });
    }

    public interface EventCallback {
        void onAddressItemClick(int position);

        void onLoadMore();

        void onSearchSubmit(String keyword);

        void onSearchTyping(String keyword);

        void onDismissDialog();
    }
}
