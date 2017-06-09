package kr.co.tmonet.gdrive.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.AbsListView;

/**
 * Created by Jessehj on 08/06/2017.
 */

public abstract class RecyclerPaginationListener extends RecyclerView.OnScrollListener {

    private static final String LOG_TAG = RecyclerPaginationListener.class.getSimpleName();
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean mUserScrolledLast;
    private boolean mIsPagingEnabled = true;

    public enum PaginationDirection {

        Bottom,
        Top
    }

    private PaginationDirection mDirection = PaginationDirection.Bottom;

    public void setDirection(PaginationDirection direction) {
        mDirection = direction;
    }

    public RecyclerPaginationListener(RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        mIsPagingEnabled = pagingEnabled;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

        //요청중이 아니고 && 마지막페이지를 스크롤하고 && 데이터 페이지가 마지막이아닐때만호출
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && mUserScrolledLast && mIsPagingEnabled) {


            if (recyclerView.getLayoutManager().canScrollVertically()) {

                Log.i(LOG_TAG, "추가 로드 : " + mDirection);
                onLoadMore();
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        int visibleItemCount = mLayoutManager.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();


        int firstVisibleItem;
        if (mLayoutManager instanceof LinearLayoutManager) {

            firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        } else if (mLayoutManager instanceof GridLayoutManager) {

            firstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        } else {

            firstVisibleItem = 0;
        }

        switch (mDirection) {

            case Bottom:
                mUserScrolledLast = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
                break;

            case Top:
                mUserScrolledLast = ((LinearLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPosition() == 0;
                break;
        }

    }

    abstract public void onLoadMore();
}
