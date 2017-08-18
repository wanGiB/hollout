package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.models.Chat;
import com.wan.hollout.ui.widgets.ConversationView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> conversations;
    private Context context;
    private LayoutInflater layoutInflater;

    public ConversationsAdapter(Context context, List<ParseObject> conversations) {
        this.context = context;
        this.conversations = conversations;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.chat_recycler_item,parent,false);
        return new ConversationsItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class ConversationsItemHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.conversation_view)
        ConversationView conversationView;

        public ConversationsItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bindData(Context context,Chat chat){

        }

    }

}
