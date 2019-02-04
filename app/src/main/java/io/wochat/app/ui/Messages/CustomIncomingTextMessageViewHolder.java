package io.wochat.app.ui.Messages;

import android.view.View;



import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.db.entity.Message;

public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {



    public CustomIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);


        //We can set click listener on view from payload
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payload != null && payload.avatarClickListener != null) {
                    payload.avatarClickListener.onAvatarClick();
                }
            }
        });
    }

    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    public interface OnAvatarClickListener {
        void onAvatarClick();
    }


}
