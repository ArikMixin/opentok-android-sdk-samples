package io.slatch.app.ui.Messages;

import android.view.View;
import android.widget.ImageButton;

import com.stfalcon.chatkit.messages.MessageHolders;

import io.slatch.app.R;
import io.slatch.app.db.entity.Message;


/*
 * Created by troy379 on 05.04.17.
 */
public class CustomIncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<Message> {


    private final ImageButton mForwardIB;

    public CustomIncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        mForwardIB = (ImageButton) itemView.findViewById(R.id.forwardIV);
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