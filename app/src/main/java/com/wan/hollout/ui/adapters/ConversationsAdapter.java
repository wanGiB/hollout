package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.ConversationView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> conversations;
    private Activity context;
    private LayoutInflater layoutInflater;
    private String searchString;

    public ConversationsAdapter(Activity context, List<ParseObject> conversations) {
        this.context = context;
        this.conversations = conversations;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.chat_recycler_item,parent,false);
        return new ConversationsItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ConversationsItemHolder conversationsItemHolder = (ConversationsItemHolder)holder;
        ParseObject conversation = conversations.get(position);
        if (conversation!=null){
            conversationsItemHolder.bindData(context,getSearchString(),conversation);
        }
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

        public void bindData(Activity context, String searchString,ParseObject chat){
            conversationView.bindData(context,searchString,chat);
        }

    }

}
