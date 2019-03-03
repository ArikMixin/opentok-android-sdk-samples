package io.wochat.app.ui.RecentChats;

import android.view.View;
import android.widget.ImageView;


import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.Utils;


/*
 * Created by Anton Bevza on 1/18/17.
 */
public class CustomDialogViewHolder
        extends DialogsListAdapter.DialogViewHolder<Conversation> {

    private final ImageView mCocheIV;
	private final CircleFlagImageView mAvatarcfiv;
	private final ImageView mMsgTypeIV;

	//private View onlineIndicator;

    public CustomDialogViewHolder(View itemView) {
        super(itemView);
        //onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        mCocheIV = (ImageView)itemView.findViewById(R.id.dialogCocheIV);
		mMsgTypeIV = (ImageView)itemView.findViewById(R.id.dialogMsgTypeIV);
		mAvatarcfiv = (CircleFlagImageView) itemView.findViewById(R.id.dialogAvatar);
    }

    @Override
    public void onBind(Conversation conversation) {
        super.onBind(conversation);

        mAvatarcfiv.setInfo(conversation.getParticipantProfilePicUrl(), conversation.getParticipantLanguage());

        if (conversation.getLastMessageAckStatus()!= null) {
			boolean isIncoming = conversation.getLastMessageSenderId().equals(conversation.getParticipantId());
			mCocheIV.setVisibility(Utils.booleanToVisibilityGone(!isIncoming));

			if (conversation.getLastMessageType().equals(Message.MSG_TYPE_VIDEO)){
				mMsgTypeIV.setVisibility(View.VISIBLE);
				imageLoader.loadImageNoPlaceholder(mMsgTypeIV, R.drawable.msg_in_video_dark);
			}
			else if (conversation.getLastMessageType().equals(Message.MSG_TYPE_IMAGE)){
				mMsgTypeIV.setVisibility(View.VISIBLE);
				imageLoader.loadImageNoPlaceholder(mMsgTypeIV, R.drawable.msg_in_camera_dark);
			}
			else if (conversation.getLastMessageType().equals(Message.MSG_TYPE_AUDIO)){
				mMsgTypeIV.setVisibility(View.VISIBLE);
				imageLoader.loadImageNoPlaceholder(mMsgTypeIV, R.drawable.msg_in_mic_dark);
			}
			else if (conversation.getLastMessageType().equals(Message.MSG_TYPE_GIF)){
				mMsgTypeIV.setVisibility(View.VISIBLE);
				imageLoader.loadImageNoPlaceholder(mMsgTypeIV, R.drawable.msg_in_gif_dark);
			}
			else {
				mMsgTypeIV.setVisibility(View.GONE);
			}

			switch (conversation.getLastMessageAckStatus()) {
				case Message.ACK_STATUS_PENDING:
					imageLoader.loadImageNoPlaceholder(mCocheIV, R.drawable.coche_pending);
					break;
				case Message.ACK_STATUS_READ:
					imageLoader.loadImageNoPlaceholder(mCocheIV, R.drawable.coche_seen);
					break;
				case Message.ACK_STATUS_RECEIVED:
					imageLoader.loadImageNoPlaceholder(mCocheIV, R.drawable.coche_arrived);
					break;
				case Message.ACK_STATUS_SENT:
					imageLoader.loadImageNoPlaceholder(mCocheIV, R.drawable.coche_sent);
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
