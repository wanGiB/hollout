package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wan.hollout.R;
import com.wan.hollout.animations.BounceInterpolator;
import com.wan.hollout.ui.adapters.ReactionsAdapter;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class BlogPostsView extends FrameLayout {

    @BindView(R.id.author_image)
    RoundedImageView authorImageView;

    @BindView(R.id.author_name)
    HolloutTextView authorNameView;

    @BindView(R.id.published_date)
    HolloutTextView publishedDateView;

    @BindView(R.id.feed_image_thumbnail)
    DynamicHeightImageView feedImageThumbnailView;

    @BindView(R.id.play_media_if_video)
    ImageView playMediaIfVideo;

    @BindView(R.id.feed_title)
    HolloutTextView feedTitleView;

    @BindView(R.id.vertical_feed_divider)
    View verticalFeedDivider;

    @BindView(R.id.youtube_player_fragment)
    FrameLayout videoFragment;

    @BindView(R.id.add_reactions_container)
    LinearLayout addReactionsContainer;

    @BindView(R.id.reactions_layout)
    View reactionsLayout;

    @BindView(R.id.feed_likes_count)
    HolloutTextView feedLikesCountView;

    @BindView(R.id.feed_comments_count)
    HolloutTextView feedCommentsCountView;

    @BindView(R.id.feed_views)
    HolloutTextView feedViews;

    @BindView(R.id.tint_vew)
    View tintView;

    @BindView(R.id.like_feed)
    HolloutTextView likeFeedView;

    @BindView(R.id.reactions_card_view)
    CardView reactionsCardView;

    @BindView(R.id.reactions_recycler_view)
    RecyclerView reactionsRecyclerView;

    YouTubePlayer globalYoutubePlayer;
    private YouTubePlayerFragment youTubePlayerFragment;

    private Animation bounceAnimation;

    private Activity activity;
    private Document document;

    private String globalPostId;
    private String youtubeVideoSource;

    public static String TAG = "BlogPosts";

    public static String DOCUMENT_START_GUARD = "<html>\n" +
            " <head></head>\n" +
            " <body></body>";

    public static String DOCUMENT_END_GUARD = "</html>";

    private FirebaseUser currentUser;

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

    private void loadBounceAnimation() {
        bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.bounce);
        BounceInterpolator bounceInterpolator = new BounceInterpolator(0.2, 20);
        bounceAnimation.setInterpolator(bounceInterpolator);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private void init(Context context) {
        inflate(context, R.layout.blog_post_item, this);
    }

    public void bindData(final Activity context, JSONObject blogPost) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.activity = context;
        HolloutLogger.d(TAG, blogPost.toString());
        String postId = blogPost.optString(AppConstants.POST_ID);

        this.globalPostId = postId;
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

        document = Jsoup.parse(DOCUMENT_START_GUARD + postContent + DOCUMENT_END_GUARD);
        prepareYoutubeVideoThumbnail(context, document, postId);

        View.OnClickListener onClickListener = new OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.like_feed:
                        loadBounceAnimation();
                        likeFeedView.startAnimation(bounceAnimation);
                        break;
                }
            }

        };

        likeFeedView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                loadReactions(context);
                UiUtils.showView(reactionsCardView, true);
                return true;
            }

        });

        likeFeedView.setOnClickListener(onClickListener);
        refreshViews(postId);
    }

    private void loadReactions(Context context) {
        ReactionsAdapter reactionsAdapter = new ReactionsAdapter(context, new ReactionsAdapter.ReactionSelectedListener() {
            @Override
            public void onReactionSelected(String reaction) {
                UiUtils.showView(reactionsCardView, false);
            }
        });
        reactionsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        reactionsRecyclerView.setAdapter(reactionsAdapter);
    }


    private void fetchPostLikes() {
        long postLikes = HolloutPreferences.getPostLikes(globalPostId);
    }

    private void prepareImageThumbnail(Activity activity, Document document) {
        Elements elements = document.select("img");
        if (elements != null) {
            if (!elements.isEmpty()) {
                Element lastElement = elements.last();
                String src = lastElement.attr("src");
                if (StringUtils.isNotEmpty(src)) {
                    HolloutLogger.d("MediaLoading", "Image Src = " + src);
                    UiUtils.showView(playMediaIfVideo, false);
                    UiUtils.showView(feedImageThumbnailView, true);
                    UiUtils.loadImage(activity, src, feedImageThumbnailView);
                }
            }
        }
    }

    private void prepareYoutubeVideoThumbnail(final Activity activity, Document document, final String postId) {
        Elements elements = document.select(".YOUTUBE-iframe-video");
        if (elements != null) {
            if (!elements.isEmpty()) {
                Element lastElement = elements.last();
                youtubeVideoSource = lastElement.attr("src");
                if (StringUtils.isNotEmpty(youtubeVideoSource)) {
                    youtubeVideoSource = StringUtils.substringBefore(StringUtils.substringAfterLast(youtubeVideoSource, "/"), "?");
                    String videoThumbnailPath = lastElement.attr("data-thumbnail-src");
                    UiUtils.showView(playMediaIfVideo, true);
                    UiUtils.loadImage(activity, videoThumbnailPath, feedImageThumbnailView);
                    attemptVideoPlay(activity, postId);
                    feedImageThumbnailView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            playMediaIfVideo.performClick();
                        }
                    });
                } else {
                    prepareImageThumbnail(activity, document);
                }
            } else {
                prepareImageThumbnail(activity, document);
            }
        } else {
            prepareImageThumbnail(activity, document);
        }
    }

    private void attemptVideoPlay(final Activity activity, final String postId) {
        playMediaIfVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                HolloutLogger.d("VideoPath", youtubeVideoSource);
                UiUtils.blinkView(playMediaIfVideo);
                UiUtils.showView(feedImageThumbnailView, false);
                UiUtils.showView(playMediaIfVideo, false);
                UiUtils.showView(videoFragment, true);

                // Delete previous added fragment
                int currentContainerId = videoFragment.getId();
                YouTubePlayerFragment oldFragment = (YouTubePlayerFragment) activity.getFragmentManager().findFragmentById(currentContainerId);
                if (oldFragment != null) {
                    activity.getFragmentManager().beginTransaction().remove(oldFragment).commit();
                }
                // In order to be able of replacing a fragment on a recycler view
                // the target container should always have a different id ALWAYS
                int newContainerId = getUniqueId();
                // Set the new Id to our know fragment container
                videoFragment.setId(newContainerId);
                youTubePlayerFragment = YouTubePlayerFragment.newInstance();
                activity.getFragmentManager().beginTransaction().replace(newContainerId, youTubePlayerFragment).commit();
                youTubePlayerFragment.initialize(AppKeys.GOOGLE_API_KEY, new YouTubePlayer.OnInitializedListener() {

                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean restored) {
                        globalYoutubePlayer = youTubePlayer;
                        int duration = HolloutPreferences.getLastPlaybackTime(postId);
                        if (duration != 0) {
                            youTubePlayer.loadVideo(youtubeVideoSource, duration);
                        } else {
                            youTubePlayer.cueVideo(youtubeVideoSource);
                            youTubePlayer.play();
                        }
                        HolloutLogger.d("SavedVideoPosition", duration + "");
                        youTubePlayer.setShowFullscreenButton(false);
                        youTubePlayer.setManageAudioFocus(true);
                        youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {

                            @Override
                            public void onPlaying() {
                                toggleReactionsParentContainer(false, postId);
                                HolloutPreferences.saveCurrentPlaybackTime(postId, 0);
                            }

                            @Override
                            public void onPaused() {
                                toggleReactionsParentContainer(true, postId);
                            }

                            @Override
                            public void onStopped() {
                                toggleReactionsParentContainer(true, postId);
                            }

                            @Override
                            public void onBuffering(boolean b) {
                                toggleReactionsParentContainer(false, postId);
                            }

                            @Override
                            public void onSeekTo(int i) {

                            }

                        });

                        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {

                            @Override
                            public void onLoading() {

                            }

                            @Override
                            public void onLoaded(String s) {

                            }

                            @Override
                            public void onAdStarted() {

                            }

                            @Override
                            public void onVideoStarted() {
                                toggleReactionsParentContainer(false, postId);

                            }

                            @Override
                            public void onVideoEnded() {
                                toggleReactionsParentContainer(true, postId);
                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason errorReason) {
                                toggleReactionsParentContainer(true, postId);
                            }

                        });

                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                    }

                });

            }
        });
    }

    // Method that could us an unique id
    public int getUniqueId() {
        return (int) SystemClock.currentThreadTimeMillis();
    }

    private void refreshViews(String postId) {
        if (AppConstants.reactionsBackgroundPositions.get((postId + "").hashCode())) {
            addReactionsContainer.setBackgroundColor(Color.parseColor("#00628F"));
        } else {
            addReactionsContainer.setBackgroundColor(Color.parseColor("#7b000000"));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (globalYoutubePlayer != null && document != null && globalPostId != null) {
            Elements elements = document.select(".YOUTUBE-iframe-video");
            if (elements != null) {
                if (!elements.isEmpty()) {
                    Element lastElement = elements.last();
                    youtubeVideoSource = lastElement.attr("src");
                    if (StringUtils.isNotEmpty(youtubeVideoSource)) {
                        youtubeVideoSource = StringUtils.substringBefore(StringUtils.substringAfterLast(youtubeVideoSource, "/"), "?");
                        String videoThumbnailPath = lastElement.attr("data-thumbnail-src");
                        UiUtils.showView(playMediaIfVideo, true);
                        UiUtils.showView(videoFragment, false);
                        UiUtils.showView(feedImageThumbnailView, true);
                        UiUtils.loadImage(activity, videoThumbnailPath, feedImageThumbnailView);
                        toggleReactionsParentContainer(true, globalPostId);
                    }
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (globalYoutubePlayer != null) {
                HolloutPreferences.saveCurrentPlaybackTime(globalPostId, globalYoutubePlayer.getCurrentTimeMillis());
                globalYoutubePlayer.pause();
                globalYoutubePlayer.release();
                videoFragment.setId(getUniqueId());
            }
        } catch (IllegalStateException ignored) {

        }
    }

    private void toggleReactionsParentContainer(boolean show, String postId) {
        UiUtils.showView(addReactionsContainer, show);
        addReactionsContainer.setBackgroundColor(Color.parseColor("#00628F"));
        AppConstants.reactionsBackgroundPositions.put((postId + "").hashCode(), true);
        if (!show) {
            UiUtils.showView(tintView, false);
        }
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
            if (!authorImageUrl.startsWith("http")) {
                authorImageView.setImageResource(R.drawable.web_hi_res_512);
            } else {
                authorImageView.setBorderColor(ContextCompat.getColor(activity, R.color.white));
                UiUtils.loadImage(activity, authorImageUrl, authorImageView);
            }

        }

    }

}
