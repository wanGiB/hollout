package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.json.JSONObject;
import java.util.List;

/**
 * @author Wan Clem
 */
public class GiphyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<JSONObject> gifs;
    private LayoutInflater layoutInflater;

    public GiphyAdapter(Context context, List<JSONObject> gifs) {
        this.context = context;
        this.gifs = gifs;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

}
