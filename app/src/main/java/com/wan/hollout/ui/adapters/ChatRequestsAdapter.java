package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.ChatRequestView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> chatRequests;
    private Activity activity;
    private LayoutInflater layoutInflater;

    private final int REAL_FEED = 0;

    public ChatRequestsAdapter(Activity activity, List<ParseObject> chatRequests) {
        this.activity = activity;
        this.chatRequests = chatRequests;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(viewType == REAL_FEED ? R.layout.chat_request_recycler_view_item
                : R.layout.padded_empty_view, parent, false);
        return new ChatRequestsHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatRequestsHolder chatRequestsHolder = (ChatRequestsHolder)holder;
        ParseObject chatRequest = chatRequests.get(position);
        if (chatRequest!=null){
            chatRequestsHolder.bindData(activity,chatRequest);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject feedObject = chatRequests.get(position);
        if (!feedObject.keySet().isEmpty()) {
            return REAL_FEED;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return chatRequests != null ? chatRequests.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class ChatRequestsHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.chat_request_view)
        ChatRequestView chatRequestView;

        public ChatRequestsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bindData(Activity activity,ParseObject chatRequest){
            if (chatRequestView != null) {
                chatRequestView.bindData(activity,null,chatRequest);
            }
        }

    }

}
