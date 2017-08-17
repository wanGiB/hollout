package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.parse.ParseObject;

import java.util.List;

/**
 * @author Wan Clem
 */

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ParseObject>chats;
    private Context context;

    public ChatsAdapter(Context context,List<ParseObject>chats){
        this.context = context;
        this.chats = chats;
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
