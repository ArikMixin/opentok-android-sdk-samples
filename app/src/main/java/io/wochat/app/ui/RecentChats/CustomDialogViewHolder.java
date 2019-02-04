package io.wochat.app.ui.RecentChats;

import android.view.View;
import android.widget.ImageView;


import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import io.wochat.app.R;
import io.wochat.app.db.entity.Ack;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.utils.Utils;


/*
 * Created by Anton Bevza on 1/18/17.
 */
public class CustomDialogViewHolder
        extends DialogsListAdapter.DialogViewHolder<Conversation> {

    private final ImageView mCocheIV;

    //private View onlineIndicator;

    public CustomDialogViewHolder(View itemView) {
        super(itemView);
        //onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        mCocheIV = (ImageView)itemView.findViewById(R.id.dialogCocheIV);
    }

    @Override
    public void onBind(Conversation conversation) {
        super.onBind(conversation);

        if (conversation.getLastMessageAckStatus()!= null) {
			boolean isIncoming = conversation.getLastMessageSenderId().equals(conversation.getParticipantId());
			mCocheIV.setVisibility(Utils.booleanToVisibilityGone(isIncoming));
			switch (conversation.getLastMessageAckStatus()) {
				case Ack.ACK_STATUS_PENDING:
					imageLoader.loadImage(mCocheIV, R.drawable.coche_pending);
					break;
				case Ack.ACK_STATUS_READ:
					imageLoader.loadImage(mCocheIV, R.drawable.coche_seen);
					break;
				case Ack.ACK_STATUS_RECEIVED:
					imageLoader.loadImage(mCocheIV, R.drawable.coche_arrived);
					break;
				case Ack.ACK_STATUS_SENT:
					imageLoader.loadImage(mCocheIV, R.drawable.coche_sent);
					break;

			}
		}
		else
			mCocheIV.setVisibility(View.GONE);


//        if (conversationComplete.getConversation().isGroup()) {
//            onlineIndicator.setVisibility(View.GONE);
//        } else {
//            //boolean isOnline = dialog.getUsers().get(0).isOnline();
//            boolean isOnline = true;
//            onlineIndicator.setVisibility(View.VISIBLE);
//            if (isOnline) {
//                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
//            } else {
//                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
//            }
//        }
    }
}
