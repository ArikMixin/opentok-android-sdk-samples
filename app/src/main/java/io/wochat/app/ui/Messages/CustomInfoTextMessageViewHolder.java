package io.wochat.app.ui.Messages;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.R;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.components.MessageReplyLayout;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.Utils;

public class CustomInfoTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {

	public CustomInfoTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
    }



}
