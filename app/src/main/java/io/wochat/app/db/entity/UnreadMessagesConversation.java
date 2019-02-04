package io.wochat.app.db.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UnreadMessagesConversation {
	private int mUnreadMessagesCount;
	private String mConversationId;

	public int getUnreadMessagesCount() {
		return mUnreadMessagesCount;
	}

	public void setUnreadMessagesCount(int unreadMessagesCount) {
		mUnreadMessagesCount = unreadMessagesCount;
	}

	public String getConversationId() {
		return mConversationId;
	}

	public void setConversationId(String conversationId) {
		mConversationId = conversationId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("ConversationId", mConversationId).
			append("UnreadMessagesCount", mUnreadMessagesCount).
			toString();
	}
}
