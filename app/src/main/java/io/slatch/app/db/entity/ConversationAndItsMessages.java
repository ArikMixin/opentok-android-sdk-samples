package io.slatch.app.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class ConversationAndItsMessages {

	@Embedded
	private Conversation mConversation;

	@Relation(parentColumn = "conversation_id", entityColumn = "conversation_id", entity = Message.class)
	private List<Message> mMessages;

	private List<GroupMember> mGroupMembers;


	public Conversation getConversation() {
		return mConversation;
	}

	public void setConversation(Conversation conversation) {
		mConversation = conversation;
	}

	public List<Message> getMessages() {
		return mMessages;
	}

	public void setMessages(List<Message> messages) {
		mMessages = messages;
	}

	public List<GroupMember> getGroupMembers() {
		return mGroupMembers;
	}

	public void setGroupMembers(List<GroupMember> groupMembers) {
		mGroupMembers = groupMembers;
	}
}
