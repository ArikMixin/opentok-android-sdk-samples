package io.wochat.app.ui.Messages;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.R;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.Utils;


/*
 * Created by troy379 on 05.04.17.
 */
public class CustomIncomingVideoMessageViewHolder
        extends MessageHolders.IncomingVideoMessageViewHolder<Message> {


    private final TextView mVideoDurationTV;
    private final ImageButton mForwardIB;

    public CustomIncomingVideoMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        mVideoDurationTV = (TextView)itemView.findViewById(R.id.videoDuration);
        mForwardIB = (ImageButton) itemView.findViewById(R.id.forwardIV);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
		mVideoDurationTV.setText(Utils.convertSecondsToHMmSs(message.getDurationMili()));

        //boolean isOnline = message.getContact().isOnline();
    }

    @Override
    protected Object getPayloadForImageLoader(Message message) {
        return super.getPayloadForImageLoader(message);
    }

}