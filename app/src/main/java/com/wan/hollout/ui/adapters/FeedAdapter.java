package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wan.hollout.R;
import com.wan.hollout.models.HolloutObject;
import com.wan.hollout.ui.widgets.BlogPostsView;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Activity context;
    private List<HolloutObject> blogPosts;

    public FeedAdapter(Activity context, List<HolloutObject> blogPosts) {
        this.context = context;
        this.blogPosts = blogPosts;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.blog_post_recycler_item, parent, false);
        return new FeedItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FeedItemHolder feedItemHolder = (FeedItemHolder) holder;
        JSONObject blogPost = blogPosts.get(position).getJsonObject();
        if (blogPost != null) {
            feedItemHolder.bindBlogPost(context, blogPost, position);
        }
    }

    @Override
    public int getItemCount() {
        return blogPosts != null ? blogPosts.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class FeedItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.blog_post_view)
        BlogPostsView blogPostsView;

        public FeedItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindBlogPost(Activity context, JSONObject blogPost, int position) {
            blogPostsView.bindData(context,blogPost,position);
        }

    }

}
