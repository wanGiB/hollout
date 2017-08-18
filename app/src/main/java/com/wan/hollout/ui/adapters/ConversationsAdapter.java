package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.wan.hollout.models.Chat;

import java.util.List;

/**
 * @author Wan Clem
 */

public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Chat>chats;
    private Context context;

    public ConversationsAdapter(Context context, List<Chat>chats){
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
