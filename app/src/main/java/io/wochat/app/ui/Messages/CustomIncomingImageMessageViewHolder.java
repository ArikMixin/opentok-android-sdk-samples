package io.wochat.app.ui.Messages;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.db.entity.Message;


/*
 * Created by troy379 on 05.04.17.
 */
public class CustomIncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<Message> {


    public CustomIncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

        //boolean isOnline = message.getContact().isOnline();
    }

    @Override
    protected Object getPayloadForImageLoader(Message message) {
        return super.getPayloadForImageLoader(message);
    }
}