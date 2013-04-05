package ro.citynow;

import android.util.Log;
import android.widget.AbsListView;

public class EndlessScrollListener implements AbsListView.OnScrollListener {
    private int visibleThreshold = 5;
    private int currentPage = 1;
    private boolean loading = true;
    private boolean moreItems = false;

    public void setMoreItems(boolean moreItems) {
        this.moreItems = moreItems;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public EndlessScrollListener() {}

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {}

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d("usr", String.format("{page=%d, loading=%b, moreItems=%b}", currentPage, loading, moreItems));
        if (loading) {
            if (moreItems) {
                loading = false;
                ++currentPage;
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                loadMore(currentPage);
                loading = true;
            }
        }
    }

    public void loadMore(int currentPage) {}
}
