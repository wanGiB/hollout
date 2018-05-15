package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.GifMessageEvent;
import com.wan.hollout.ui.widgets.LoadingImageView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class GifsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> gifs;
    private LayoutInflater layoutInflater;

    public GifsAdapter(Activity activity, List<String> gifs) {
        this.gifs = gifs;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.recycler_view_item_gif, parent, false);
        return new GifItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GifItemHolder gifItemHolder = (GifItemHolder) holder;
        String gifObjectUrl = gifs.get(position);
        if (gifObjectUrl != null) {
            gifItemHolder.bindGif(gifObjectUrl);
        }
    }

    @Override
    public int getItemCount() {
        return gifs != null ? gifs.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class GifItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.giphy_item_image)
        LoadingImageView giphyImageView;

        @BindView(R.id.parent_view)
        View parentView;

        public GifItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindGif(final String gifUrl) {
            if (StringUtils.isNotEmpty(gifUrl)) {
                giphyImageView.startLoading();
                Glide.with(ApplicationLoader.getInstance()).asGif().load(gifUrl).listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        giphyImageView.stopLoading();
                        return false;
                    }
                }).into(giphyImageView);
            }
            parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new GifMessageEvent(gifUrl));
                }
            });
            giphyImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parentView.performClick();
                }
            });

        }

    }

}
