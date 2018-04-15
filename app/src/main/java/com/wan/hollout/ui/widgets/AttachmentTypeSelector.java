package com.wan.hollout.ui.widgets;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.eventbuses.ReactionMessageEvent;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.interfaces.EndlessRecyclerViewScrollListener;
import com.wan.hollout.listeners.OnSingleClickListener;
import com.wan.hollout.ui.adapters.GifsAdapter;
import com.wan.hollout.ui.adapters.ReactionsAdapter;
import com.wan.hollout.utils.ApiUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.ItemOffSetDecoration;
import com.wan.hollout.utils.UiUtils;
import com.wan.hollout.utils.ViewUtil;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

@SuppressWarnings("deprecation")
public class AttachmentTypeSelector extends PopupWindow {

    public static final int ADD_IMAGE = 1;
    public static final int ADD_VIDEO = 2;
    public static final int OPEN_GALLERY = 3;
    public static final int ADD_CONTACT = 4;
    public static final int ADD_DOCUMENT = 5;
    public static final int ADD_LOCATION = 6;
    public static final int ADD_GIF = 7;
    public static final int ADD_REACTION = 8;

    private static final int ANIMATION_DURATION = 300;

    private final
    @NonNull
    ImageView imageButton;
    private final
    @NonNull
    ImageView galleryButton;
    private final
    @NonNull
    ImageView videoButton;
    private final
    @NonNull
    ImageView contactButton;
    private final
    @NonNull
    ImageView documentButton;
    private final
    @NonNull
    ImageView locationButton;
    private final
    @NonNull
    ImageView gifButton;
    private final
    @NonNull
    ImageView reactionsInvoker;

    private
    @NonNull
    HolloutEditText gifSearchBox;

    private
    @NonNull
    View attachmentWindow;

    private
    @NonNull
    View gifWindow;

    @NonNull
    private View reactionsWindow;

    @NotNull
    private RecyclerView gifRecyclerView;

    @NonNull
    private RecyclerView reactionsRecyclerView;

    @NonNull
    private ProgressBar gifLoadingProgressWheel;

    private
    @NonNull
    ImageView closeGifWindow;

    private
    @Nullable
    View currentAnchor;

    @NonNull
    private ImageView closeReactionsView;

    private
    @Nullable
    AttachmentClickedListener listener;

    private static int PAGE = 0;

    private GifsAdapter gifsAdapter;
    private List<String> gifs = new ArrayList<>();
    private Activity activity;
    private View footerView;

    private ReactionsAdapter reactionsAdapter;

    public AttachmentTypeSelector(@NonNull Context context, @NonNull LoaderManager loaderManager, @Nullable AttachmentClickedListener listener) {
        super(context);
        this.activity = (Activity) context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.attachment_type_selector, null, true);
        RecentPhotoViewRail recentPhotos = ViewUtil.findById(layout, R.id.recent_photos);
        this.listener = listener;
        this.imageButton = ViewUtil.findById(layout, R.id.photo_button);
        this.galleryButton = ViewUtil.findById(layout, R.id.gallery_button);
        this.videoButton = ViewUtil.findById(layout, R.id.video_button);
        this.contactButton = ViewUtil.findById(layout, R.id.contact_button);
        this.documentButton = ViewUtil.findById(layout, R.id.document_button);
        this.locationButton = ViewUtil.findById(layout, R.id.location_button);
        this.gifButton = ViewUtil.findById(layout, R.id.giphy_button);
        this.reactionsInvoker = ViewUtil.findById(layout, R.id.reactions_invoker);
        this.closeGifWindow = ViewUtil.findById(layout, R.id.close_giphy);
        this.attachmentWindow = ViewUtil.findById(layout, R.id.attachment_window);
        this.gifWindow = ViewUtil.findById(layout, R.id.giphy_window);
        this.reactionsWindow = ViewUtil.findById(layout, R.id.reactions_window);
        this.closeReactionsView = ViewUtil.findById(layout, R.id.close_reactions_view);

        ImageView dummySearchGifImageView = ViewUtil.findById(layout, R.id.dummy_search_image_view);

        this.gifSearchBox = ViewUtil.findById(layout, R.id.gif_search_box);
        this.gifRecyclerView = ViewUtil.findById(layout, R.id.gif_recycler_view);
        reactionsRecyclerView = ViewUtil.findById(layout, R.id.reactions_recycler_view);
        this.gifLoadingProgressWheel = ViewUtil.findById(layout, R.id.gif_progress_wheel);

        this.imageButton.setOnClickListener(new PropagatingClickListener(ADD_IMAGE));
        this.galleryButton.setOnClickListener(new PropagatingClickListener(OPEN_GALLERY));
        this.videoButton.setOnClickListener(new PropagatingClickListener(ADD_VIDEO));
        this.contactButton.setOnClickListener(new PropagatingClickListener(ADD_CONTACT));
        this.documentButton.setOnClickListener(new PropagatingClickListener(ADD_DOCUMENT));
        this.locationButton.setOnClickListener(new PropagatingClickListener(ADD_LOCATION));
        this.gifButton.setOnClickListener(new PropagatingClickListener(ADD_GIF));
        this.reactionsInvoker.setOnClickListener(new PropagatingClickListener(ADD_REACTION));

        loadHahaToReactionsInvoker();

        dummySearchGifImageView.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void onSingleClick(View view) {
                gifSearchBox.requestFocus();
            }

        });

        recentPhotos.setListener(new RecentPhotoSelectedListener());

        setContentView(layout);
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(0);
        setFocusable(true);
        setTouchable(true);
        update();

        closeGifWindow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                UiUtils.showView(attachmentWindow, true);
                UiUtils.showView(gifWindow, false);
            }

        });

        closeReactionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.showView(attachmentWindow, true);
                UiUtils.showView(reactionsWindow, false);
            }
        });

        setupGifsFooterLoader();
        setupGifsAdapter();

        gifSearchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.isNotEmpty(charSequence.toString())) {
                    loadGifs(activity, charSequence.toString().trim(), 0);
                } else {
                    loadGifs(activity, null, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        recentPhotos.fetchRecentImages();
    }

    private static InputStream stream;

    private KFImage getKFImage(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        KFImage kfImage = null;
        try {
            stream = assetManager.open(fileName);
            kfImage = KFImageDeserializer.deserialize(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kfImage;
    }

    private void loadHahaToReactionsInvoker() {
        String hahaReactions = "reactions/Haha.json";
        KeyframesDrawable imageDrawable = new KeyframesDrawableBuilder().withImage(getKFImage(getContext(),
                hahaReactions)).build();
        reactionsInvoker.setImageDrawable(imageDrawable);
        imageDrawable.startAnimation();
    }

    @SuppressLint("InflateParams")
    private void setupGifsFooterLoader() {
        footerView = LayoutInflater.from(activity).inflate(R.layout.loading_footer, null);
    }

    public void show(final @NonNull View anchor) {
        this.currentAnchor = anchor;
        showAtLocation(anchor, Gravity.BOTTOM, 0, 0);

        getContentView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                getContentView().getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    animateWindowInCircular(anchor, getContentView());
                } else {
                    animateWindowInTranslate(getContentView());
                }

            }

        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateButtonIn(imageButton, ANIMATION_DURATION / 2);
            animateButtonIn(documentButton, ANIMATION_DURATION / 2);

            animateButtonIn(galleryButton, ANIMATION_DURATION / 3);
            animateButtonIn(locationButton, ANIMATION_DURATION / 3);
            animateButtonIn(videoButton, ANIMATION_DURATION / 4);
            animateButtonIn(gifButton, ANIMATION_DURATION / 4);
            animateButtonIn(contactButton, 0);
            animateButtonIn(reactionsInvoker, 0);
        }

    }

    private void closeGifWindow() {
        closeGifWindow.performClick();
    }

    private void closeReactionsWindow() {
        closeReactionsView.performClick();
    }

    @Override
    public void dismiss() {
        if (gifWindow.getVisibility() == View.VISIBLE) {
            closeGifWindow();
        } else if (reactionsWindow.getVisibility() == View.VISIBLE) {
            closeReactionsWindow();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateWindowOutCircular(currentAnchor, getContentView());
            } else {
                animateWindowOutTranslate(getContentView());
            }
        }
    }

    @Override
    public boolean isShowing() {
        return super.isShowing();
    }

    public void setListener(@Nullable AttachmentClickedListener listener) {
        this.listener = listener;
    }

    private void animateButtonIn(View button, int delay) {
        AnimationSet animation = new AnimationSet(true);
        Animation scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);

        animation.addAnimation(scale);
        animation.setInterpolator(new OvershootInterpolator(1));
        animation.setDuration(ANIMATION_DURATION);
        animation.setStartOffset(delay);
        button.startAnimation(animation);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateWindowInCircular(@Nullable View anchor, @NonNull View contentView) {
        Pair<Integer, Integer> coordinates = getClickOrigin(anchor, contentView);
        Animator animator = ViewAnimationUtils.createCircularReveal(contentView,
                coordinates.first,
                coordinates.second,
                0,
                Math.max(contentView.getWidth(), contentView.getHeight()));
        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }

    private void animateWindowInTranslate(@NonNull View contentView) {
        Animation animation = new TranslateAnimation(0, 0, contentView.getHeight(), 0);
        animation.setDuration(ANIMATION_DURATION);

        getContentView().startAnimation(animation);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateWindowOutCircular(@Nullable View anchor, @NonNull View contentView) {
        Pair<Integer, Integer> coordinates = getClickOrigin(anchor, contentView);
        Animator animator = ViewAnimationUtils.createCircularReveal(getContentView(),
                coordinates.first,
                coordinates.second,
                Math.max(getContentView().getWidth(), getContentView().getHeight()),
                0);

        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AttachmentTypeSelector.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });

        animator.start();
    }

    private void animateWindowOutTranslate(@NonNull View contentView) {
        Animation animation = new TranslateAnimation(0, 0, 0, contentView.getTop() + contentView.getHeight());
        animation.setDuration(ANIMATION_DURATION);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AttachmentTypeSelector.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        getContentView().startAnimation(animation);
    }

    private Pair<Integer, Integer> getClickOrigin(@Nullable View anchor, @NonNull View contentView) {
        if (anchor == null) return new Pair<>(0, 0);

        final int[] anchorCoordinates = new int[2];
        anchor.getLocationOnScreen(anchorCoordinates);
        anchorCoordinates[0] += anchor.getWidth() / 2;
        anchorCoordinates[1] += anchor.getHeight() / 2;

        final int[] contentCoordinates = new int[2];
        contentView.getLocationOnScreen(contentCoordinates);

        int x = anchorCoordinates[0] - contentCoordinates[0];
        int y = anchorCoordinates[1] - contentCoordinates[1];

        return new Pair<>(x, y);

    }

    private void setupGifsAdapter() {
        gifsAdapter = new GifsAdapter(activity, gifs);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(gifsAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        ItemOffSetDecoration itemOffSetDecoration = new ItemOffSetDecoration(activity, R.dimen.item_offset);
        gifRecyclerView.addItemDecoration(itemOffSetDecoration);
        gifRecyclerView.setLayoutManager(gridLayoutManager);
        gifRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        gifRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!gifs.isEmpty()) {
                    UiUtils.showView(footerView, true);
                    loadGifs(activity, gifSearchBox.getText().toString().trim(), PAGE);
                }
            }
        });
        RecyclerViewUtils.setFooterView(gifRecyclerView, footerView);
        UiUtils.showView(footerView, false);
    }

    public void loadGifs(final Activity activity, String searchKey, int page) {
        if (page == 0) {
            UiUtils.showView(gifLoadingProgressWheel, true);
            gifs.clear();
            PAGE = 0;
        }
        UiUtils.showView(gifWindow, true);
        UiUtils.showView(attachmentWindow, false);

        if (StringUtils.isNotEmpty(searchKey)) {

            ApiUtils.searchGif(searchKey, page, new DoneCallback<List<JSONObject>>() {

                @Override
                public void done(final List<JSONObject> result, Exception e) {

                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            UiUtils.showView(gifLoadingProgressWheel, false);
                            UiUtils.showView(footerView, false);

                            if (result != null && !result.isEmpty()) {
                                for (JSONObject jsonObject : result) {
                                    String gifUrl = HolloutUtils.getGifUrl(jsonObject);
                                    if (!gifs.contains(gifUrl)) {
                                        gifs.add(gifUrl);
                                    }
                                }
                                gifsAdapter.notifyDataSetChanged();
                                PAGE++;
                            }

                        }

                    });

                }

            });

        } else {

            ApiUtils.fetchTrendingGifs(page, new DoneCallback<List<JSONObject>>() {

                @Override
                public void done(final List<JSONObject> result, Exception e) {

                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            UiUtils.showView(gifLoadingProgressWheel, false);
                            UiUtils.showView(footerView, false);

                            if (result != null && !result.isEmpty()) {

                                for (JSONObject jsonObject : result) {
                                    String gifUrl = HolloutUtils.getGifUrl(jsonObject);
                                    if (!gifs.contains(gifUrl)) {
                                        gifs.add(gifUrl);
                                    }
                                }

                                gifsAdapter.notifyDataSetChanged();
                                PAGE++;
                            }

                        }

                    });

                }

            });

        }

    }

    public void loadReactions() {
        UiUtils.showView(gifWindow, false);
        UiUtils.showView(attachmentWindow, false);
        UiUtils.showView(reactionsWindow, true);
        if (reactionsAdapter == null) {
            initReactionsAdapter();
        } else {
            reactionsAdapter.notifyDataSetChanged();
        }
    }

    private void initReactionsAdapter() {
        reactionsAdapter = new ReactionsAdapter(getContext(),
                new ReactionsAdapter.ReactionSelectedListener() {
                    @Override
                    public void onReactionSelected(String reaction) {
                        EventBus.getDefault().post(new ReactionMessageEvent(reaction));
                        dismiss();
                        dismiss();
                    }
                });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        reactionsRecyclerView.addItemDecoration(new ItemOffSetDecoration(getContext(), R.dimen.item_offset_eight));
        reactionsRecyclerView.setLayoutManager(gridLayoutManager);
        reactionsRecyclerView.setAdapter(reactionsAdapter);
    }

    private class RecentPhotoSelectedListener implements RecentPhotoViewRail.OnItemClickedListener {

        @Override
        public void onItemClicked(Uri uri) {
            animateWindowOutTranslate(getContentView());
            if (listener != null) listener.onQuickAttachment(uri);
        }

    }

    private class PropagatingClickListener implements View.OnClickListener {

        private final int type;

        private PropagatingClickListener(int type) {
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() != R.id.giphy_button && v.getId() != R.id.reactions_invoker) {
                animateWindowOutTranslate(getContentView());
            }
            if (listener != null) listener.onClick(type);
        }

    }

    public interface AttachmentClickedListener {

        void onClick(int type);

        void onQuickAttachment(Uri uri);

    }

}
