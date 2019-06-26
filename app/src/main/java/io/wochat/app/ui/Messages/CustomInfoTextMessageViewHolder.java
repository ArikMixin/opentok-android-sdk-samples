package io.wochat.app.ui.Messages;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.R;
import io.wochat.app.db.entity.Message;

public class CustomInfoTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {

    private final ImageView mTypeMissedIV;

	public CustomInfoTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        mTypeMissedIV = (ImageView)itemView.findViewById(R.id.type_missed_iv);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
	    // Set missed call type (Video or Audio)
        if(message.getEventCode().equals(Message.EVENT_CODE_MISSED_VIDEO_CALL)){
            mTypeMissedIV.setVisibility(View.VISIBLE);
            mTypeMissedIV.setImageResource(R.drawable.msg_in_video_dark);
        }else if (message.getEventCode().equals(Message.EVENT_CODE_MISSED_VOICE_CALL)){
            mTypeMissedIV.setVisibility(View.VISIBLE);
            mTypeMissedIV.setImageResource(R.drawable.phone_grey);
        }else{
             mTypeMissedIV.setVisibility(View.GONE);
        }
    }
}
