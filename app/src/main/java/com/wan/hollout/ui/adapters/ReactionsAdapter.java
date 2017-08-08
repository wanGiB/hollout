package com.wan.hollout.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ReactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;

    private String[] reactions = new String[]{"Like.json", "Love.json", "Haha.json", "Wow.json", "Sorry.json", "Anger.json"};
    private LayoutInflater layoutInflater;
    private Context context;

    public interface ReactionSelectedListener {
        void onReactionSelected(String reaction);
    }

    private ReactionSelectedListener reactionSelectedListener;

    public ReactionsAdapter(Context context, ReactionSelectedListener reactionSelectedListener) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.reactionSelectedListener = reactionSelectedListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.reactions_item, parent, false);
        return new ReactionItemsHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);
        ReactionItemsHolder reactionItemsHolder = (ReactionItemsHolder) holder;
        String keyTag = reactions[position];
        reactionItemsHolder.loadEmoji(context, keyTag, reactionSelectedListener);
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(100);
            view.setAlpha(0.f);
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(20 * position)
                    .setInterpolator(new DecelerateInterpolator(2.f))
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true;
                        }
                    })
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return reactions.length;
    }

    @SuppressWarnings("WeakerAccess")
    static class ReactionItemsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.emoji_view)
        ImageView emojiView;

        public ReactionItemsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void loadEmoji(Context context, final String reactionTag, final ReactionSelectedListener reactionSelectedListener) {
            loadDrawables(context, emojiView, reactionTag);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reactionSelectedListener.onReactionSelected(reactionTag);
                }
            });
        }

        private void loadDrawables(Context context, ImageView emojiView, String reactionTag) {
            KeyframesDrawable imageDrawable = new KeyframesDrawableBuilder().withImage(getKFImage(context, reactionTag)).build();
            emojiView.setImageDrawable(imageDrawable);
            imageDrawable.startAnimation();
        }

        private KFImage getKFImage(Context context, String fileName) {
            AssetManager assetManager = context.getAssets();
            InputStream stream;
            KFImage kfImage = null;
            try {
                stream = assetManager.open(fileName);
                kfImage = KFImageDeserializer.deserialize(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return kfImage;
        }

    }

}
