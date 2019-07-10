package io.slatch.app.ui.RecentChats;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import io.slatch.app.R;
import io.slatch.app.components.CircleFlagImageView;
import io.slatch.app.db.entity.Contact;
import io.slatch.app.db.entity.Conversation;
import io.slatch.app.db.entity.Message;
import io.slatch.app.utils.Utils;


/*
 * Created by Anton Bevza on 1/18/17.
 */
public class RecentChatsViewHolder
        extends DialogsListAdapter.DialogViewHolder<Conversation> implements View.OnClickListener {

    private final ImageView mCocheIV;
	private final CircleFlagImageView mAvatarcfiv;
	private final ImageView mMsgTypeIV;
	private final ImageButton mCameraIB;
	private final ImageButton mPhoneIB;
    private Conversation mConversation;

	public static final int BTN_CAMERA = 1;
	public static final int BTN_PHONE = 2;
    //private View onlineIndicator;

    public RecentChatsViewHolder(View itemView) {
        super(itemView);
        //onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        mCocheIV = (ImageView)itemView.findViewById(R.id.dialogCocheIV);
		mMsgTypeIV = (ImageView)itemView.findViewById(R.id.dialogMsgTypeIV);
		mAvatarcfiv = (CircleFlagImageView) itemView.findViewById(R.id.dialogAvatar);
		mCameraIB = (ImageButton)itemView.findViewById(R.id.camera_ib);
		mPhoneIB = (ImageButton)itemView.findViewById(R.id.phone_ib);

		mCameraIB.setOnClickListener(this);
		mPhoneIB.setOnClickListener(this);
    }

    @Override
    public void onBind(Conversation conversation) {
        super.onBind(conversation);

        String initials;

        mCameraIB.setTag(conversation);
		mPhoneIB.setTag(conversation);

		//Missed Call
		if (conversation.getLastMessageType() != null &&
				conversation.getLastMessageType().equals(Message.MSG_TYPE_CALL_MISSED_VIDEO)){
			mMsgTypeIV.setVisibility(View.VISIBLE);
			imageLoader.loadImageNoPlaceholder(mMsgTypeIV, R.drawable.msg_in_video_dark);
		}
		else if (conversation.getLastMessageType() != null &&
					conversation.getLastMessageType().equals(Message.MSG_TYPE_CALL_MISSED_AUDIO)){
			mMsgTypeIV.setVisibility(View.VISIBLE);
			mMsgTypeIV.setImageResource(R.drawable.phone_grey);
		}

		//Group
		if (conversation.isGroup()){
            mCameraIB.setVisibility(View.INVISIBLE);
            mPhoneIB.setVisibility(View.INVISIBLE);
            initials = Contact.getInitialsFromName(conversation.getGroupName());
        }
        else {
            mCameraIB.setVisibility(View.VISIBLE);
            mPhoneIB.setVisibility(View.VISIBLE);
            initials = Contact.getInitialsFromName(conversation.getParticipantName());
        }

		mAvatarcfiv.setInfo(conversation.getParticipantProfilePicUrl(),
			conversation.getParticipantLanguage(),
			initials);

		if (conversation.getLastMessageId() == null){
			mCocheIV.setVisibility(View.GONE);
			mMsgTypeIV.setVisibility(View.GONE);
			return;
		}

		if (conversation.getLastMessageType().equals(Message.MSG_TYPE_GROUP_EVENT)){
			mMsgTypeIV.setVisibility(View.GONE);
		}

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
			else if ((conversation.getLastMessageType().equals(Message.MSG_TYPE_AUDIO))||
					 (conversation.getLastMessageType().equals(Message.MSG_TYPE_SPEECHABLE))){
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

	@Override
	public void onClick(View v) {

		 mConversation = (Conversation) v.getTag();

		switch(v.getId()){
			case R.id.camera_ib:
					onButtonClickListener.onButtonClick(mConversation, BTN_CAMERA);
				break;
			case R.id.phone_ib:
					onButtonClickListener.onButtonClick(mConversation, BTN_PHONE);
				break;
		}
	}
}
