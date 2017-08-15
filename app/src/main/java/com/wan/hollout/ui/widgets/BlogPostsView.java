package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.wan.hollout.R;
import com.wan.hollout.animations.BounceInterpolator;
import com.wan.hollout.ui.adapters.ReactionsAdapter;
import com.wan.hollout.ui.adapters.RemoteReactionsAdapter;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.StringComparator;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"FieldCanBeLocal", "unused", "ConstantConditions", "deprecation"})
public class BlogPostsView extends FrameLayout {

    @BindView(R.id.author_image)
    RoundedImageView authorImageView;

    @BindView(R.id.author_name)
    HolloutTextView authorNameView;

    @BindView(R.id.published_date)
    HolloutTextView publishedDateView;

    @BindView(R.id.feed_image_thumbnail)
    DynamicHeightImageView feedImageThumbnailView;

    @BindView(R.id.play_media_if_video_icon)
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

    @BindView(R.id.like_feed)
    HolloutTextView likeFeedView;

    @BindView(R.id.comment_on_feed)
    HolloutTextView commentOnFeedView;

    @BindView(R.id.share_feed)
    HolloutTextView shareFeedView;

    @BindView(R.id.reactions_pop_up_card_view)
    CardView reactionsCardView;

    @BindView(R.id.reactions_pop_up_recycler_view)
    RecyclerView reactionsRecyclerView;

    @BindView(R.id.post_reaction_indicators_layout)
    FrameLayout reactionsIndicatorLayout;

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

    private ValueEventListener postLikesValueEventListener;
    private DatabaseReference postLikesReference;
    private long comments;

    private ReactionsAdapter popUpReactionsAdapter;
    private RemoteReactionsAdapter remoteReactionsAdapter;

    private Vibrator vibe;

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
        vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.activity = context;
        HolloutLogger.d(TAG, blogPost.toString());
        final String postId = blogPost.optString(AppConstants.POST_ID);

        this.globalPostId = postId;
        String publishedDate = blogPost.optString(AppConstants.POST_PUBLISHED_DATE);

        JSONObject blog = blogPost.optJSONObject(AppConstants.BLOG);
        final String blogId = blog.optString(AppConstants.BLOG_ID);
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

        comments = Long.parseLong(totalReplies);

        if (comments > 0) {
            UiUtils.showView(feedCommentsCountView, true);
            AppConstants.commentPositions.put(getPostHashCode(postId), true);
            feedCommentsCountView.setText(comments == 1 ? "1 Comment" : HolloutUtils.format(comments) + " Comments");
        } else {
            UiUtils.showView(feedCommentsCountView, false);
            AppConstants.commentPositions.put(getPostHashCode(postId), false);
        }

        JSONArray labels = blogPost.optJSONArray(AppConstants.LABELS);

        Random random = new Random();
        int randomColor = ContextCompat.getColor(context, randomColors[random.nextInt(randomColors.length - 1)]);
        verticalFeedDivider.setBackgroundColor(randomColor);

        document = Jsoup.parse(DOCUMENT_START_GUARD + postContent + DOCUMENT_END_GUARD);
        prepareYoutubeVideoThumbnail(context, document, postId);

        View.OnClickListener onClickListener = new OnClickListener() {

            @Override
            public void onClick(View view) {
                loadBounceAnimation();
                view.startAnimation(bounceAnimation);
                switch (view.getId()) {
                    case R.id.like_feed:
                        handleLikesOnClickListener(postId);
                        break;
                    case R.id.comment_on_feed:
                        if (currentUser == null) {
//                            ((MainActivity) (activity)).initiateAuthentication(new DoneCallback<Boolean>() {
//                                @Override
//                                public void done(Boolean result, Exception e) {
//                                    if (e == null && result) {
//                                        launchCommentActivity(context, postId, blogId);
//                                    }
//                                }
//                            });
                        } else {
                        }
                        break;
                }

            }

        };

        likeFeedView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                vibe.vibrate(100);
                UiUtils.showView(reactionsCardView, true);
                loadReactions(postId, context);
                AppConstants.ARE_REACTIONS_OPEN = true;
                addReactionValueToInvalidator(true);
                return true;
            }
        });

        likeFeedView.setOnClickListener(onClickListener);
        commentOnFeedView.setOnClickListener(onClickListener);
        shareFeedView.setOnClickListener(onClickListener);

        fetchPostLikes(postId);
        setPostDescription();
        checkAndRegEventBus();
        invalidateView(postId);
    }

    private void setPostDescription() {
        String description = null;
        if (document != null) {
            Elements images = document.select("img");
            if (images != null) {
                for (Element element : images) {
                    String html = element.outerHtml();
                    description = StringUtils.strip(document.toString(), html);
                }
            }
        }
        Elements videoElements = document.select(".YOUTUBE-iframe-video");
        if (videoElements != null) {
            for (Element element : videoElements) {
                String html = element.outerHtml();
                description = StringUtils.strip(document.toString(), html);
            }
        }
        HolloutLogger.d("Html", "Result after stripping = " + description);
    }

    private void handleLikesOnClickListener(final String postId) {
        if (reactionsCardView.getVisibility() == VISIBLE) {
            closeReactionsPopUp();
        } else {
            if (currentUser == null) {
//                ((MainActivity) (activity)).initiateAuthentication(new DoneCallback<Boolean>() {
//                    @Override
//                    public void done(Boolean result, Exception e) {
//                        if (e == null && result) {
//                            likeFeed(postId, "Like.json");
//                        }
//                    }
//                });
            } else {
                likeFeed(postId, "Like.json");
            }
        }
    }

    private int getPostHashCode(String postId) {
        return (postId + "").hashCode();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAnUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String message = (String) o;
                    if (message.equals(AppConstants.CLOSE_REACTIONS)) {
                        closeReactionsPopUp();
                    }
                }
            }
        });
    }

    private void closeReactionsPopUp() {
        UiUtils.showView(reactionsCardView, false);
        AppConstants.ARE_REACTIONS_OPEN = false;
        addReactionValueToInvalidator(false);
    }

    private void addReactionValueToInvalidator(boolean value) {
        AppConstants.reactionsOpenPositions.put(getPostHashCode(globalPostId), value);
    }

    private void loadReactions(final String postId, Context context) {
        popUpReactionsAdapter = new ReactionsAdapter(context,
                new ReactionsAdapter.ReactionSelectedListener() {
                    @Override
                    public void onReactionSelected(final String reaction) {
                        UiUtils.showView(reactionsCardView, false);
                        if (currentUser == null) {
//                            ((MainActivity) (activity)).initiateAuthentication(new DoneCallback<Boolean>() {
//                                @Override
//                                public void done(Boolean result, Exception e) {
//                                    if (e == null && result) {
//                                        likeFeed(postId, reaction);
//                                    }
//                                }
//                            });
                            return;
                        }
                        likeFeed(postId, reaction);
                    }
                });
        reactionsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        reactionsRecyclerView.setAdapter(popUpReactionsAdapter);
    }

    private void likeFeed(final String postId, String reaction) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            HashMap<String, Object> updatableLikeProps = new HashMap<>();
            HashMap<String, String> reactors = new HashMap<>();
            FirebaseUtils.getLikesReference(postId + "/" + AppConstants.REACTORS + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(reaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    fetchPostLikes(postId);
                }
            });
        } else {
//            ((MainActivity) (activity)).initiateAuthentication(new DoneCallback<Boolean>() {
//                @Override
//                public void done(Boolean result, Exception e) {
//                    if (e == null && result) {
//                        likeFeed(postId, "Like.json");
//                    }
//                }
//            });
        }
    }

    private void fetchPostLikes(final String postId) {
        postLikesReference = FirebaseUtils.getLikesReference(postId + "/" + AppConstants.REACTORS);
        final List<String> reactions = new LinkedList<>();
        reactions.clear();

        postLikesValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, String>> hashMapGenericTypeIndicator = new GenericTypeIndicator<HashMap<String, String>>() {
                };
                HashMap<String, String> stringObjectHashMap = dataSnapshot.getValue(hashMapGenericTypeIndicator);
                if (stringObjectHashMap != null) {
                    Collection<String> values = stringObjectHashMap.values();
                    for (String value : values) {
                        if (!reactions.contains(value)) {
                            reactions.add(value);
                        }
                    }
                    if (!reactions.isEmpty()) {
                        UiUtils.showView(reactionsIndicatorLayout, true);
                        AppConstants.likesPositions.put(getPostHashCode(postId), true);
                        if (currentUser != null) {
                            setupPostLikes(reactions, stringObjectHashMap.keySet().contains(currentUser.getUid()), stringObjectHashMap.get(currentUser.getUid()));
                        } else {
                            setupPostLikes(reactions, false, null);
                        }
                    } else {
                        likeFeedView.setText(activity.getString(R.string.fa_icon_heart));
                        UiUtils.removeAllDrawablesFromTextView(likeFeedView);
                        AppConstants.likesPositions.put(getPostHashCode(postId), false);
                        UiUtils.showView(feedLikesCountView, false);
                        UiUtils.showView(reactionsIndicatorLayout, false);
                        refreshLikeFeedButtonClickListeners();
                    }
                } else {
                    likeFeedView.setText(activity.getString(R.string.fa_icon_heart));
                    UiUtils.removeAllDrawablesFromTextView(likeFeedView);
                    UiUtils.showView(feedLikesCountView, false);
                    UiUtils.showView(reactionsIndicatorLayout, false);
                    AppConstants.likesPositions.put(getPostHashCode(postId), false);
                    refreshLikeFeedButtonClickListeners();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                refreshLikeFeedButtonClickListeners();
            }

        };

        postLikesReference.addValueEventListener(postLikesValueEventListener);

    }

    private void refreshLikeFeedButtonClickListeners() {
        likeFeedView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.blinkView(view);
                handleLikesOnClickListener(globalPostId);
            }
        });
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
                    feedImageThumbnailView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            closeReactionsPopUp();
                        }
                    });
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
                            closeReactionsPopUp();
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
                                addVideoStateToReactionParentContainer(false, postId);
                                HolloutPreferences.saveCurrentPlaybackTime(postId, 0);
                            }

                            @Override
                            public void onPaused() {
                                addVideoStateToReactionParentContainer(true, postId);
                            }

                            @Override
                            public void onStopped() {
                                addVideoStateToReactionParentContainer(true, postId);
                            }

                            @Override
                            public void onBuffering(boolean b) {
                                addVideoStateToReactionParentContainer(false, postId);
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
                                addVideoStateToReactionParentContainer(false, postId);

                            }

                            @Override
                            public void onVideoEnded() {
                                addVideoStateToReactionParentContainer(true, postId);
                            }

                            @Override
                            public void onError(YouTubePlayer.ErrorReason errorReason) {
                                addVideoStateToReactionParentContainer(true, postId);
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

    private void invalidateView(String postId) {
        UiUtils.showView(reactionsCardView, AppConstants.reactionsOpenPositions.get(getPostHashCode(postId)));
        UiUtils.showView(feedCommentsCountView, AppConstants.commentPositions.get(getPostHashCode(postId)));
        UiUtils.showView(feedLikesCountView, AppConstants.likesPositions.get(getPostHashCode(postId)));
        UiUtils.showView(reactionsIndicatorLayout, AppConstants.likesPositions.get(getPostHashCode(postId)));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkAndRegEventBus();
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
                        addVideoStateToReactionParentContainer(true, globalPostId);
                    }
                }
            }
        }
        if (globalPostId != null) {
            fetchPostLikes(globalPostId);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        checkAnUnRegEventBus();
        try {
            if (globalYoutubePlayer != null) {
                HolloutPreferences.saveCurrentPlaybackTime(globalPostId, globalYoutubePlayer.getCurrentTimeMillis());
                globalYoutubePlayer.pause();
                globalYoutubePlayer.release();
                videoFragment.setId(getUniqueId());
            }
        } catch (IllegalStateException ignored) {

        }
        if (popUpReactionsAdapter != null) {
            popUpReactionsAdapter.cleanUp();
        }
        try {
            if (postLikesReference != null && postLikesValueEventListener != null) {
                postLikesReference.removeEventListener(postLikesValueEventListener);
            }
        } catch (NullPointerException ignored) {

        }
        closeReactionsPopUp();
    }

    private void addVideoStateToReactionParentContainer(boolean show, String postId) {
        AppConstants.reactionsBackgroundPositions.put(getPostHashCode(postId), true);
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

    private void setUpReactions(List<String> reactions) {
        Collections.sort(reactions, new StringComparator());
        reactionsIndicatorLayout.removeAllViews();
        for (int i = 0; i < reactions.size(); i++) {
            ImageView imageView = new ImageView(activity);
            FrameLayout.LayoutParams layoutParams = new LayoutParams(HolloutUtils.dp(16), HolloutUtils.dp(16));
            imageView.setImageResource(getReactionsImage(activity, reactions.get(i)));
            if (i != 0) {
                layoutParams.leftMargin = HolloutUtils.dp(i * 11);
            }
            imageView.setLayoutParams(layoutParams);
            reactionsIndicatorLayout.addView(imageView, 0);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setupPostLikes(List<String> reactions, boolean signedInUserLikesPost, String signedInUserReactionValue) {
        UiUtils.showView(reactionsIndicatorLayout, true);
        setUpReactions(reactions);
        UiUtils.showView(feedLikesCountView, true);
        if (signedInUserLikesPost) {
            if (!signedInUserReactionValue.equals("Like.json")) {
                likeFeedView.setText(StringUtils.strip(signedInUserReactionValue, ".json"));
            } else {
                likeFeedView.setText(activity.getString(R.string.unlike));
            }
            likeFeedView.setGravity(Gravity.CENTER_VERTICAL);
            likeFeedView.setCompoundDrawablePadding(5);
            int keyframesDrawable = getReactionsImage(activity, signedInUserReactionValue);
            UiUtils.attachDrawableToTextView(activity, likeFeedView, keyframesDrawable, UiUtils.DrawableDirection.LEFT);
            likeFeedView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (reactionsCardView.getVisibility() == VISIBLE) {
                        closeReactionsPopUp();
                    } else {
                        FirebaseUtils.getLikesReference(globalPostId + "/" + AppConstants.REACTORS + "/" + currentUser.getUid())
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        fetchPostLikes(globalPostId);
                                    }
                                });
                    }
                }
            });
        } else {
            UiUtils.removeAllDrawablesFromTextView(likeFeedView);
            likeFeedView.setText(activity.getString(R.string.fa_icon_heart));
            likeFeedView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.blinkView(view);
                    likeFeed(globalPostId, "Like.json");
                }
            });
        }
        if (reactions.size() == 1 && signedInUserLikesPost) {
            feedLikesCountView.setText(" " + activity.getString(R.string.you));
        } else {
            if (reactions.size() > 1) {
                if (signedInUserLikesPost) {
                    long likesDiff = reactions.size() - 1;
                    feedLikesCountView.setText(" " + activity.getString(R.string.you_and) + " ".concat(HolloutUtils.format(likesDiff) + (likesDiff == 1 ? " other " : " others")));
                } else {
                    feedLikesCountView.setText(HolloutUtils.format(reactions.size()));
                }
            } else if (reactions.size() == 1) {
                feedLikesCountView.setText(" " + HolloutUtils.format(reactions.size()));
            }
        }
    }

    private int getReactionsImage(Context context, String reactionTag) {
        if (reactionTag.contains("Like")) {
            return R.drawable.reactions_like_sutro;
        } else if (reactionTag.contains("Anger")) {
            return R.drawable.reactions_anger_sutro;
        } else if (reactionTag.contains("Haha")) {
            return R.drawable.reactions_haha_sutro;
        } else if (reactionTag.contains("Love")) {
            return R.drawable.reactions_love_sutro;
        } else if (reactionTag.contains("Sorry")) {
            return R.drawable.reactions_sorry_sutro;
        } else if (reactionTag.contains("Wow")) {
            return R.drawable.reactions_wow_sutro;
        }
        return R.drawable.reactions_like_sutro;
    }

    private class ItemDecorator extends RecyclerView.ItemDecoration {

        private final int mSpace;

        ItemDecorator(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position != 0)
                outRect.top = mSpace;
        }

    }

}
