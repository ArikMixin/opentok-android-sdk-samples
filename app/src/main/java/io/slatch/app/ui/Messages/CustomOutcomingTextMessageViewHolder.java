package io.slatch.app.ui.Messages;

import android.view.View;
import android.widget.ImageView;


import com.stfalcon.chatkit.messages.MessageHolders;

import io.slatch.app.R;
import io.slatch.app.components.CircleImageView;
import io.slatch.app.components.MessageReplyLayout;
import io.slatch.app.db.entity.Message;
import io.slatch.app.utils.Utils;

public class CustomOutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message> {

    private final ImageView mCocheIV;
    private final MessageReplyLayout mMessageReplyLayout;
    private final Payload mPayload;
    private final CircleImageView mMagicIndicator;


    public CustomOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        mCocheIV = (ImageView) itemView.findViewById(R.id.coche_iv);
        mMessageReplyLayout = (MessageReplyLayout) itemView.findViewById(R.id.reply_layout);
        mMessageReplyLayout.showCloseBtn(false);
        mMagicIndicator = (CircleImageView)itemView.findViewById(R.id.magicIndicatorCIV);
        mPayload = (Payload) payload;

    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

        time.setText(time.getText());
        switch (message.getStatus()){
            case Message.ACK_STATUS_PENDING:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_pending));
                break;
            case Message.ACK_STATUS_SENT:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_sent));
                break;
            case Message.ACK_STATUS_RECEIVED:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_arrived));
                break;
            case Message.ACK_STATUS_READ:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_seen));
                break;
            default:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_pending));
                break;
        }


        Message replyMessage = mPayload.onPayloadListener.getRepliedMessage(message.getRepliedMessageId());
        if (replyMessage == null){
			mMessageReplyLayout.setVisibility(View.GONE);
		}
		else {
			mMessageReplyLayout.setVisibility(View.VISIBLE);
			String name = mPayload.onPayloadListener.getSenderName(message.getRepliedMessageId());
			mMessageReplyLayout.showReplyMessage(replyMessage, name);
		}


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
