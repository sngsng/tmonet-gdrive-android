package kr.co.tmonet.gdrive.controller.fragment;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.activity.BaseActivity;
import kr.co.tmonet.gdrive.databinding.FragmentSearchAddressBinding;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.network.RestClient;
import kr.co.tmonet.gdrive.view.helper.SearchAddressDialogFragmentHelper;

import static kr.co.tmonet.gdrive.model.SearchAddress.getSearchAddressFromJson;

/**
 * Created by Jessehj on 08/06/2017.
 */

public class SearchAddressDialogFragment extends DialogFragment {

    private static final String LOG_TAG = SearchAddressDialogFragment.class.getSimpleName();

    private ArrayList<SearchAddress> mSearchResults = new ArrayList<>();

    private FragmentSearchAddressBinding mBinding;
    private SearchAddressDialogFragmentHelper mFragmentHelper;
    private OnFragmentInteractionListener mListener;

    private String mSearchKeyword = "";
    private String mTotalCount = "";
    private int mCurrentPage = 1;

    public SearchAddressDialogFragment() {
    }

    public static SearchAddressDialogFragment newInstance() {

        return new SearchAddressDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            dialog.getWindow().setGravity(Gravity.TOP | Gravity.START);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_address, container, false);
        View rootView = mBinding.getRoot();

        setUpViews(rootView);
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


    private void setUpViews(View rootView) {
        mFragmentHelper = new SearchAddressDialogFragmentHelper((AppCompatActivity) getActivity(), rootView, mBinding);
        mFragmentHelper.setEventCallback(new SearchAddressDialogFragmentHelper.EventCallback() {
            @Override
            public void onAddressItemClick(int position) {
                SearchAddress address = mSearchResults.get(position);
                if (mListener != null) {
                    mListener.onSelectAddress(address);
                }
                dismiss();
            }

            @Override
            public void onLoadMore() {
                requestSearchAddress();
            }

            @Override
            public void onSearchTyping(String keyword) {
                Log.i(LOG_TAG, "typing keyword : " + keyword);

                mSearchKeyword = keyword;
                mCurrentPage = 1;
                mSearchResults.clear();
                requestSearchAddress();
            }

            @Override
            public void onSearchSubmit(String keyword) {
                ((BaseActivity) getActivity()).hideKeyboard(getActivity());
                Log.i(LOG_TAG, "search keyword : " + keyword);

                mSearchKeyword = keyword;
                mCurrentPage = 1;
                mSearchResults.clear();
                requestSearchAddress();

            }

            @Override
            public void onDismissDialog() {
                dismiss();
            }
        });
    }

    private void requestSearchAddress() {
        Log.i(LOG_TAG, "search keyword: " + mSearchKeyword);
        SearchAddress.getSearchResults(getActivity(), mSearchKeyword, mCurrentPage, new RestClient.RestListener() {
            @Override
            public void onBefore() {
                if (mCurrentPage == 0) {
                    ((BaseActivity) getActivity()).showProgressDialog();
                }
                mFragmentHelper.setPaginationEnable(false);
            }

            @Override
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    JSONObject result = (JSONObject) response;
                    try {
                        JSONObject common = result.getJSONObject(APIConstants.AddressSearch.COMMON);
                        mTotalCount = common.getString(APIConstants.AddressSearch.TOTAL_COUNT);
                        JSONArray jusoJsonArray = result.getJSONArray(APIConstants.AddressSearch.JUSO);

                        for (int i = 0; i < jusoJsonArray.length(); i++) {
                            JSONObject jusoJson = jusoJsonArray.getJSONObject(i);
                            SearchAddress address = getSearchAddressFromJson(jusoJson);

                            mSearchResults.add(address);
                        }

                        if (mSearchResults.isEmpty()) {
                            mFragmentHelper.setPaginationEnable(false);
                        } else {
                            mFragmentHelper.setPaginationEnable(true);
                            mCurrentPage++;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (response == null) {
                    mSearchResults.clear();
                    mCurrentPage = 1;
                }
                ((BaseActivity) getActivity()).dismissProgressDialog();
                mFragmentHelper.updateList(mSearchResults);
                mFragmentHelper.updateTotalCount(mTotalCount);
            }

            @Override
            public void onFail(Error error) {
                mFragmentHelper.setPaginationEnable(true);
                ((BaseActivity) getActivity()).dismissProgressDialog();
                ((BaseActivity) getActivity()).showToast(error.getLocalizedMessage());
            }

            @Override
            public void onError(Error error) {
                mFragmentHelper.setPaginationEnable(true);
                ((BaseActivity) getActivity()).dismissProgressDialog();
                ((BaseActivity) getActivity()).showToast(error.getLocalizedMessage());
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onSelectAddress(SearchAddress address);
    }


}
