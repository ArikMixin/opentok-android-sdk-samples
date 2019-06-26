package io.wochat.app.model;

import java.util.List;

import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.GroupMember;

public class ConversationAndItsGroupMembers {

	private Conversation mConversation;
	private List<GroupMember> mGroupMembers;

	public Conversation getConversation() {
		return mConversation;
	}

	public void setConversation(Conversation conversation) {
		mConversation = conversation;
	}

	public List<GroupMember> getGroupMembers() {
		return mGroupMembers;
	}

	public void setGroupMembers(List<GroupMember> groupMembers) {
		mGroupMembers = groupMembers;
	}
}
