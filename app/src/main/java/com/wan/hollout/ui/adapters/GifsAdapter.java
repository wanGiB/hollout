package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wan.hollout.R;
import com.wan.hollout.utils.HolloutUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class GifsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private List<JSONObject> gifs;
    private LayoutInflater layoutInflater;

    public GifsAdapter(Activity activity, List<JSONObject> gifs) {
        this.activity = activity;
        this.gifs = gifs;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.recycler_view_item_gif, parent, false);
        return new GifItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GifItemHolder gifItemHolder = (GifItemHolder) holder;
        JSONObject gifObject = gifs.get(position);
        if (gifObject != null) {
            gifItemHolder.bindGif(activity, gifObject);
        }
    }

    @Override
    public int getItemCount() {
        return gifs != null ? gifs.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class GifItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.giphy_item_image)
        ImageView giphyImageView;

        public GifItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindGif(Activity activity, JSONObject gifObject) {
            String gifUrl = HolloutUtils.getGifUrl(gifObject);
            if (Build.VERSION.SDK_INT >= 17) {
                if (!activity.isDestroyed()) {
                    if (StringUtils.isNotEmpty(gifUrl)) {
                        Glide.with(activity).load(gifUrl).asGif().into(giphyImageView);
                    }
                }
            } else {
                if (StringUtils.isNotEmpty(gifUrl)) {
                    Glide.with(activity).load(gifUrl).asGif().into(giphyImageView);
                }
            }

        }

    }

}
