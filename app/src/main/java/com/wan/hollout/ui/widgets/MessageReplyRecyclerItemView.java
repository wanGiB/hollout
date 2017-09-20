package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hyphenate.chat.EMMessage;
import com.wan.hollout.R;

/**
 * @author Wan Clem
 */

public class MessageReplyRecyclerItemView extends LinearLayout {

    private LinearLayout contentView;
    private HolloutTextView replyTitleView;
    private HolloutTextView replySubTitleView;
    private FrameLayout replyAttachmentView;
    private ImageView replyIconView;
    private ImageView playReplyIconView;

    public MessageReplyRecyclerItemView(Context context) {
        this(context, null);
    }

    public MessageReplyRecyclerItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageReplyRecyclerItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.message_reply_recycler_view_item, this);
        initView();
    }

    private void initView(){
        contentView = (LinearLayout)findViewById(R.id.content_view);
        replyTitleView = (HolloutTextView)findViewById(R.id.reply_title);
        replySubTitleView = (HolloutTextView)findViewById(R.id.reply_message_body);
        replyAttachmentView = (FrameLayout)findViewById(R.id.reply_attachment_view);
        replyIconView = (ImageView)findViewById(R.id.reply_icon);
        playReplyIconView = (ImageView)findViewById(R.id.play_reply_msg_if_video);
    }

    public void bindMessageReply(Activity activity, EMMessage replyMessageView){

    }

}
