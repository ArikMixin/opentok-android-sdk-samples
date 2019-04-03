package io.wochat.app.ui.Messages;

import android.view.View;
import android.widget.ImageView;


import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.R;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.components.MessageReplyLayout;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.Utils;

public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {


	private final MessageReplyLayout mMessageReplyLayout;
	private final Payload mPayload;
	private final CircleImageView mMagicIndicator;

	public CustomIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
		mMessageReplyLayout = (MessageReplyLayout) itemView.findViewById(R.id.reply_layout);
		mMessageReplyLayout.showCloseBtn(false);
		mMagicIndicator = (CircleImageView)itemView.findViewById(R.id.magicIndicatorCIV);
		mPayload = (Payload) payload;

    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
		Message replyMessage = mPayload.onPayloadListener.getRepliedMessage(message.getRepliedMessageId());
		if (replyMessage == null){
			mMessageReplyLayout.setVisibility(View.GONE);
		}
		else {
			mMessageReplyLayout.setVisibility(View.VISIBLE);
			String name = mPayload.onPayloadListener.getSenderName(message.getRepliedMessageId());
			mMessageReplyLayout.showReplyMessage(replyMessage, name);
		}

		//itemView.setMinimumWidth(Utils.dp2px(itemView.getContext(), 550));
		//itemView.getLayoutParams().width = 1000;


		if (message.isMagic()){
			mMagicIndicator.setVisibility(View.VISIBLE);
			int country = Utils.getCountryFlagDrawableFromLang(message.getDisplayedLang());
			mMagicIndicator.setImageDrawable(itemView.getResources().getDrawable(country));
		}
		else {
			mMagicIndicator.setVisibility(View.GONE);
		}

    }

    public static class Payload {
        public OnPayloadListener onPayloadListener;
    }

    public interface OnPayloadListener {
		Message getRepliedMessage(String repliedMessageId);
		String getSenderName(String repliedMessageId);
	}


}
