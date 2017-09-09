package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.activities.FullChatRequestsActivity;
import com.wan.hollout.utils.UiUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatRequestsAdapterView extends LinearLayout {

    @BindView(R.id.chat_requests_header)
    HolloutTextView chatRequestsHeaderView;

    @BindView(R.id.adapter_items_container)
    LinearLayout adapterItemsContainer;

    @BindView(R.id.see_all_connection_requests_)
    TextView seeAllConnectionRequestsView;

    private List<ParseObject> chatRequests;
    private HashMap<String, View> chatsViewMap = new HashMap<>();

    private Activity activity;

    public ChatRequestsAdapterView(Context context) {
        this(context, null);
    }

    public ChatRequestsAdapterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRequestsAdapterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.chat_requests_adapter_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setChatRequests(Activity activity, List<ParseObject> chatRequests) {
        this.activity = activity;
        this.chatRequests = chatRequests;
        if (!chatRequests.isEmpty()) {
            loadRequests();
        }
    }

    private void addChatRequestsView(Activity context, ParseObject chatRequest) {
        ChatRequestView chatRequestView = new ChatRequestView(context);
        chatRequestView.bindData(context, this, chatRequest);
        adapterItemsContainer.addView(chatRequestView);
        chatsViewMap.put(chatRequest.getObjectId(), chatRequestView);
        notifyDataSetChanged();
    }

    public void removeChatRequest(ParseObject removableChatRequest) {
        if (chatsViewMap.containsKey(removableChatRequest.getObjectId())) {
            View viewValue = chatsViewMap.get(removableChatRequest.getObjectId());
            adapterItemsContainer.removeView(viewValue);
            notifyDataSetChanged();
            refreshChatRequests(removableChatRequest);
        }
    }

    private void refreshChatRequests(ParseObject removableParseObject) {
        try {
            chatRequests.remove(removableParseObject);
        } catch (Exception ignored) {
            Crashlytics.logException(ignored);
        }
        loadRequests();
    }

    private void loadRequests() {
        chatRequestsHeaderView.setText(activity.getString(R.string.chat_requests));
        if (chatRequests.isEmpty()) {
            seeAllConnectionRequestsView.setTextColor(ContextCompat.getColor(activity, R.color.grey500));
            seeAllConnectionRequestsView.setText(activity.getString(R.string.thats_all_for_now));
            return;
        }
        seeAllConnectionRequestsView.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        seeAllConnectionRequestsView.setText(UiUtils.fromHtml(activity.getString(R.string.see_all) + "<b>" + chatRequests.size() + "</b>"));
        if (chatRequests.size() == 1) {
            try {
                ParseObject singleChatRequest = chatRequests.get(0);
                addChatRequestsView(activity, singleChatRequest);
            } catch (IndexOutOfBoundsException ignored) {

            }
        } else if (chatRequests.size() > 1) {
            try {
                ParseObject firstChatRequest = chatRequests.get(0);
                ParseObject secondChatRequest = chatRequests.get(1);
                if (firstChatRequest != null) {
                    addChatRequestsView(activity, firstChatRequest);
                }
                if (secondChatRequest != null) {
                    addChatRequestsView(activity, secondChatRequest);
                }
            } catch (IndexOutOfBoundsException ignored) {

            }
        }
        seeAllConnectionRequestsView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.blinkView(v);
                Intent mFullChatRequestsIntent = new Intent(activity, FullChatRequestsActivity.class);
                activity.startActivity(mFullChatRequestsIntent);
            }
        });
    }

    private void notifyDataSetChanged() {
        adapterItemsContainer.invalidate();
        adapterItemsContainer.requestLayout();
    }

}
