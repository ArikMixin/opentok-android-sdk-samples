package io.wochat.app.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Relation;

import com.stfalcon.chatkit.commons.models.IContact;
import com.stfalcon.chatkit.commons.models.IDialog;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class ConversationComplete implements IDialog {

	@Embedded
	Conversation mConversation;

	String mConvId;
	String mLastMessageText;
	String mLastMessageSenderId;
	long mLastMessageTimestamp;

	@Ignore
	int mUnreadMessagesCount=5;

	String mLastMessageId;
	@Message.ACK_STATUS String mLastMessageAckStatus;
	String mContactProfilePicUrl;
	String mContactName;

	public String getConvId() {
		return mConvId;
	}

	public void setConvId(String convId) {
		mConvId = convId;
	}



	public Conversation getConversation() {
		return mConversation;
	}

	public void setConversation(Conversation conversation) {
		mConversation = conversation;
	}

	@Override
	public String getId() {
		return mConvId;
	}

	@Override
	public String getDialogPhoto() {
		return mContactProfilePicUrl;
	}

	@Override
	public String getDialogName() {
		return mContactName;
	}

//	@Override
//	public List<? extends IContact> getContacts() {
//		return null;
//	}
	@Override
	public boolean isGroup() {
		return mConversation.isGroup();
	}


	@Override
	public Date getLastMessageCreatedDate() {
		return new Date(mLastMessageTimestamp);
	}

	public String getLastMessageText() {
		return mLastMessageText;
	}

	@Override
	public String getLastMessageId() {
		return mLastMessageId;
	}

	public void setLastMessageId(String lastMessageId) {
		mLastMessageId = lastMessageId;
	}


	@Override
	public void setLastMessageCreatedDate(Date date) {
		mLastMessageTimestamp = date.getTime();
	}

	public void setLastMessageText(String lastMessageText) {
		mLastMessageText = lastMessageText;
	}

	@Override
	public int getUnreadMessagesCount() {
		return mUnreadMessagesCount;
	}

	public String getLastMessageSenderId() {
		return mLastMessageSenderId;
	}

	public void setLastMessageSenderId(String lastMessageSenderId) {
		mLastMessageSenderId = lastMessageSenderId;
	}

	public long getLastMessageTimestamp() {
		return mLastMessageTimestamp;
	}

	public void setLastMessageTimestamp(long lastMessageTimestamp) {
		mLastMessageTimestamp = lastMessageTimestamp;
	}

	public @Message.ACK_STATUS	String getLastMessageAckStatus() {
		return mLastMessageAckStatus;
	}

	public void setLastMessageAckStatus(@Message.ACK_STATUS String lastMessageAckStatus) {
		mLastMessageAckStatus = lastMessageAckStatus;
	}

	public String getContactProfilePicUrl() {
		return mContactProfilePicUrl;
	}

	public void setContactProfilePicUrl(String contactProfilePicUrl) {
		mContactProfilePicUrl = contactProfilePicUrl;
	}

	public String getContactName() {
		return mContactName;
	}

	public void setContactName(String contactName) {
		mContactName = contactName;
	}


	public void setUnreadMessagesCount(int unreadMessagesCount) {
		mUnreadMessagesCount = unreadMessagesCount;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("convId", mConvId).
			append("conversation", mConversation.toString()).
			append("lastMessageText", mLastMessageText).
			append("lastMessageSenderId", mLastMessageSenderId).
			append("lastMessageTimestamp", mLastMessageTimestamp).
			append("lastMessageAckStatus", mLastMessageAckStatus).
			append("contactProfilePicUrl", mContactProfilePicUrl).
			toString();
	}


}
