package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.FeedView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Activity context;
    private List<ParseObject> feeds;

    private final int REAL_FEED = 0;

    public FeedAdapter(Activity context, List<ParseObject> feeds) {
        this.context = context;
        this.feeds = feeds;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(viewType == REAL_FEED ? R.layout.feed_recycler_item : R.layout.padded_empty_view, parent, false);
        return new FeedItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FeedItemHolder feedItemHolder = (FeedItemHolder) holder;
        ParseObject feedItem = feeds.get(position);
        if (feedItem != null) {
            feedItemHolder.bindBlogPost(context, feedItem);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject feedObject = feeds.get(position);
        if (!feedObject.keySet().isEmpty()) {
            return REAL_FEED;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return feeds != null ? feeds.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class FeedItemHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.feed_view_item)
        FeedView feedView;

        public FeedItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindBlogPost(Activity context, ParseObject feedItem) {
            if (feedView != null) {
                feedView.bindData(context, feedItem);
            }
        }

    }

}
