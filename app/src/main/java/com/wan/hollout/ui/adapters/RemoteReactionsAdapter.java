package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wan.hollout.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class RemoteReactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<String> reactions;

    public RemoteReactionsAdapter(Context context, List<String> reactions) {
        this.reactions = reactions;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.post_reactions_item, parent, false);
        return new ReactionItemsHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ReactionItemsHolder reactionItemsHolder = (ReactionItemsHolder) holder;
        String keyTag = reactions.get(position);
        reactionItemsHolder.loadEmoji(keyTag);
    }

    @Override
    public int getItemCount() {
        return reactions.size();
    }

    @SuppressWarnings("WeakerAccess")
    static class ReactionItemsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.emoji_view)
        ImageView emojiView;

        public ReactionItemsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void loadEmoji(final String reactionTag) {
            loadDrawables(emojiView, reactionTag);
        }

        private void loadDrawables(ImageView emojiView, String reactionTag) {
            if (reactionTag.contains("Like")) {
                emojiView.setImageResource(R.drawable.reactions_like_sutro);
            } else if (reactionTag.contains("Anger")) {
                emojiView.setImageResource(R.drawable.reactions_anger_sutro);
            } else if (reactionTag.contains("Haha")) {
                emojiView.setImageResource(R.drawable.reactions_haha_sutro);
            } else if (reactionTag.contains("Love")) {
                emojiView.setImageResource(R.drawable.reactions_love_sutro);
            } else if (reactionTag.contains("Sorry")) {
                emojiView.setImageResource(R.drawable.reactions_sorry_sutro);
            } else if (reactionTag.contains("Wow")) {
                emojiView.setImageResource(R.drawable.reactions_wow_sutro);
            }
        }
    }
}
