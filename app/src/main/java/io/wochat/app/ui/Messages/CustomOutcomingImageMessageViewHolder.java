package io.wochat.app.ui.Messages;

import android.util.Pair;
import android.view.View;
import android.widget.ImageView;


import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.R;
import io.wochat.app.db.entity.Ack;
import io.wochat.app.db.entity.Message;


/*
 * Created by troy379 on 05.04.17.
 */
public class CustomOutcomingImageMessageViewHolder
        extends MessageHolders.OutcomingImageMessageViewHolder<Message> {

    private final ImageView mCocheIV;

    public CustomOutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        mCocheIV = (ImageView) itemView.findViewById(R.id.coche_iv);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);

        time.setText(time.getText());
        switch (message.getStatus()){
            case Ack.ACK_STATUS_PENDING:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_pending));
                break;
            case Ack.ACK_STATUS_SENT:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_sent));
                break;
            case Ack.ACK_STATUS_RECEIVED:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_arrived));
                break;
            case Ack.ACK_STATUS_READ:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_seen));
                break;
            default:
                mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_pending));
                break;
        }
    }

    //Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
    @Override
    protected Object getPayloadForImageLoader(Message message) {
        //For example you can pass size of placeholder before loading
        return new Pair<>(100, 100);
    }
}