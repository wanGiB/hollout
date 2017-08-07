package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.youtube.player.YouTubeThumbnailView;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.DateUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class BlogPostsView extends FrameLayout {

    @BindView(R.id.author_image)
    CircleImageView authorImageView;

    @BindView(R.id.author_name)
    HolloutTextView authorNameView;

    @BindView(R.id.published_date)
    HolloutTextView publishedDateView;

    @BindView(R.id.feed_image_thumbnail)
    DynamicHeightImageView feedImageThumbnailView;

    @BindView(R.id.youtube_thumbnail_view)
    YouTubeThumbnailView youTubeThumbnailView;

    @BindView(R.id.feed_title)
    HolloutTextView feedTitleView;

    @BindView(R.id.vertical_feed_divider)
    View verticalFeedDivider;

    public static String TAG = "BlogPosts";

    public static String DOCUMENT_START_GUARD = "<html>\n" +
            " <head></head>\n" +
            " <body></body>";

    public static String DOCUMENT_END_GUARD = "</html>";

    private static int[] randomColors = new int[]{R.color.hollout_color,
            R.color.hollout_color_one,
            R.color.gplus_color_1,
            R.color.colorTwitter,
            R.color.hollout_color_three,
            R.color.hollout_color_four, R.color.hollout_color_five};

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
        inflate(context, R.layout.blog_post_item, this);
    }

    public void bindData(Activity context, JSONObject blogPost, int position) {

        HolloutLogger.d(TAG, blogPost.toString());

        String postId = blogPost.optString(AppConstants.POST_ID);
        String publishedDate = blogPost.optString(AppConstants.POST_PUBLISHED_DATE);

        JSONObject blog = blogPost.optJSONObject(AppConstants.BLOG);
        String blogId = blog.optString(AppConstants.BLOG_ID);
        String postLink = blogPost.optString(AppConstants.PUBLIC_POST_LINK);
        String selfLink = blogPost.optString(AppConstants.POST_SELF_LINK);

        String postTitle = blogPost.optString(AppConstants.POST_TITLE);
        feedTitleView.setText(postTitle);

        String postContent = blogPost.optString(AppConstants.POST_CONTENT);

        //Author
        JSONObject author = blogPost.optJSONObject(AppConstants.AUTHOR);
        setupAuthorAndPublishedDate(context, author, publishedDate);

        JSONObject postReplies = blogPost.optJSONObject(AppConstants.POST_REPLIES);
        String totalReplies = postReplies.optString(AppConstants.REPLIES_COUNT);

        JSONArray labels = blogPost.optJSONArray(AppConstants.LABELS);

        Random random = new Random();
        verticalFeedDivider.setBackgroundColor(ContextCompat.getColor(context, randomColors[random.nextInt(randomColors.length - 1)]));
    }

    private void prepareImageThumbnail() {

    }

    private void setupAuthorAndPublishedDate(Activity activity, JSONObject author, String publishedDate) {
        if (author != null) {
            String postAuthorName = author.optString(AppConstants.AUTHOR_DISPLAY_NAME);
            String authorPublicUrl = author.optString(AppConstants.AUTHOR_PUBLIC_URL);

            JSONObject authorImage = author.optJSONObject(AppConstants.AUTHOR_IMAGE);
            String authorImageUrl = authorImage.optString(AppConstants.AUTHOR_IMAGE_URL);

            authorNameView.setText(postAuthorName);
            Instant instant = Instant.parse(publishedDate);
            String timeAgo = UiUtils.getTimeAgo(new Date(instant.getMillis()));
            publishedDateView.setText(timeAgo);
            UiUtils.loadImage(activity, authorImageUrl, authorImageView);
        }

    }

}
