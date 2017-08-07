package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class BlogPostsView extends FrameLayout {

    public BlogPostsView(Context context) {
        super(context);
        init(context);
    }

    public BlogPostsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BlogPostsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private void init(Context context) {
        inflate(context, R.layout.blog_post_recycler_item, this);
    }

    public void setupBlogPost(Activity activity, JSONObject blogPost) {

        String postId = blogPost.optString(AppConstants.POST_ID);
        String publishedDate = blogPost.optString(AppConstants.POST_PUBLISHED_DATE);

        JSONObject blog = blogPost.optJSONObject(AppConstants.BLOG);
        String blogId = blog.optString(AppConstants.BLOG_ID);
        String postLink = blogPost.optString(AppConstants.PUBLIC_POST_LINK);
        String selfLink = blogPost.optString(AppConstants.POST_SELF_LINK);

        String postTitle = blogPost.optString(AppConstants.POST_TITLE);
        String postContent = blogPost.optString(AppConstants.POST_CONTENT);

        //Author
        JSONObject author = blogPost.optJSONObject(AppConstants.AUTHOR);
        String postAuthorName = author.optString(AppConstants.AUTHOR_DISPLAY_NAME);
        String authorPublicUrl = author.optString(AppConstants.AUTHOR_PUBLIC_URL);

        JSONObject authorImage = author.optJSONObject(AppConstants.AUTHOR_IMAGE);
        String authorImageUrl = authorImage.optString(AppConstants.AUTHOR_IMAGE_URL);

        JSONObject postReplies = blogPost.optJSONObject(AppConstants.POST_REPLIES);
        String totalReplies = postReplies.optString(AppConstants.REPLIES_COUNT);

        JSONArray labels = blogPost.optJSONArray(AppConstants.LABELS);

    }

}
