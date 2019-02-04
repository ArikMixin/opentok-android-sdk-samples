package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IContact;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "conversation_table",
		indices = {@Index("last_message_id"), @Index("participant_id"), @Index("last_message_sender_id")},
		foreignKeys = {
			@ForeignKey(entity = Message.class,
				parentColumns = "message_id",
				childColumns = "last_message_id",
				onDelete = CASCADE),

			@ForeignKey(entity = Contact.class,
				parentColumns = "contact_id",
				childColumns = "participant_id",
				onDelete = CASCADE),

			@ForeignKey(entity = Contact.class,
				parentColumns = "contact_id",
				childColumns = "last_message_sender_id",
				onDelete = CASCADE)

		})

public class Conversation implements IDialog{

	/***************************************************/
	@PrimaryKey
	@NonNull
	@SerializedName("conversation_id")
	@ColumnInfo(name = "conversation_id")
	@Expose
	private String conversationId;

	/***************************************************/
	@NonNull
	@SerializedName("participant_id")
	@ColumnInfo(name = "participant_id")
	@Expose
	private String participantId;

	/***************************************************/
	@SerializedName("participant_profile_pic_url")
	@ColumnInfo(name = "participant_profile_pic_url")
	@Expose
	private String participantProfilePicUrl;

	/***************************************************/
	@SerializedName("participant_name")
	@ColumnInfo(name = "participant_name")
	@Expose
	private String participantName;

	/***************************************************/

	@SerializedName("last_message_id")
	@ColumnInfo(name = "last_message_id")
	@Expose
	private String lastMessageId;
	/***************************************************/
	@SerializedName("last_message_timestamp")
	@ColumnInfo(name = "last_message_timestamp")
	@Expose
	private long lastMessageTimeStamp;
	/***************************************************/
	@SerializedName("last_message_text")
	@ColumnInfo(name = "last_message_text")
	@Expose
	private String lastMessageText;
	/***************************************************/
	@SerializedName("last_message_sender_id")
	@ColumnInfo(name = "last_message_sender_id")
	@Expose
	String mLastMessageSenderId;
	/***************************************************/
	@SerializedName("last_message_ack_status")
	@ColumnInfo(name = "last_message_ack_status")
	@Expose
	@Ack.ACK_STATUS String mLastMessageAckStatus;
	/***************************************************/

	@SerializedName("num_of_unread_messages")
	@ColumnInfo(name = "num_of_unread_messages")
	@Expose
	private int numOfUnreadMessages;

	/***************************************************/
	@SerializedName("is_group")
	@ColumnInfo(name = "is_group")
	@Expose
	private boolean isGroup;
	/***************************************************/
	@SerializedName("group_name")
	@ColumnInfo(name = "group_name")
	@Expose
	private String GroupName;
	/***************************************************/

	public Conversation(String participantId, String selfId){
		this.conversationId = getConversationId(participantId, selfId);
		this.participantId = participantId;
	}


	public Conversation(){

	}

	public static String getConversationId(String participantId, String selfId){
		if (participantId.compareTo(selfId)>0)
			return participantId + "_" + selfId;
		else
			return selfId + "_" + participantId;
	}

	@NonNull
	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(@NonNull String conversationId) {
		this.conversationId = conversationId;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	public String getLastMessageId() {
		return lastMessageId;
	}

	public void setLastMessageId(String lastMessageId) {
		this.lastMessageId = lastMessageId;
	}

	public long getLastMessageTimeStamp() {
		return lastMessageTimeStamp;
	}

	public void setLastMessageTimeStamp(long lastMessageTimeStamp) {
		this.lastMessageTimeStamp = lastMessageTimeStamp;
	}

	public int getNumOfUnreadMessages() {
		return numOfUnreadMessages;
	}

	public void setNumOfUnreadMessages(int numOfUnreadMessages) {
		this.numOfUnreadMessages = numOfUnreadMessages;
	}

	public boolean isGroup() {
		return isGroup;
	}

	@Override
	public void setLastMessageCreatedDate(Date date) {

	}

	public void setGroup(boolean group) {
		isGroup = group;
	}

	public String getGroupName() {
		return GroupName;
	}

	public void setGroupName(String groupName) {
		GroupName = groupName;
	}

	public String getParticipantProfilePicUrl() {
		return participantProfilePicUrl;
	}

	public void setParticipantProfilePicUrl(String participantProfilePicUrl) {
		this.participantProfilePicUrl = participantProfilePicUrl;
	}

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	@Override
	public String getId() {
		return conversationId;
	}

	@Override
	public String getDialogPhoto() {
		return participantProfilePicUrl;
	}

	@Override
	public String getDialogName() {
		return participantName;
	}

	@Override
	public Date getLastMessageCreatedDate() {
		return new Date(lastMessageTimeStamp);
	}

	public String getLastMessageText() {
		return lastMessageText;
	}

	public void setLastMessageText(String lastMessageText) {
		this.lastMessageText = lastMessageText;
	}

	@Override
	public int getUnreadMessagesCount() {
		return numOfUnreadMessages;
	}

	public String getLastMessageSenderId() {
		return mLastMessageSenderId;
	}

	public void setLastMessageSenderId(String lastMessageSenderId) {
		mLastMessageSenderId = lastMessageSenderId;
	}

	public String getLastMessageAckStatus() {
		return mLastMessageAckStatus;
	}

	public void setLastMessageAckStatus(String lastMessageAckStatus) {
		mLastMessageAckStatus = lastMessageAckStatus;
	}



	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("conversationId", conversationId).
			append("participantId", participantId).
			append("lastMessageId", lastMessageId).
			append("lastMessageTimeStamp", lastMessageTimeStamp).
			append("numOfUnreadMessages", numOfUnreadMessages).
			append("isGroup", isGroup).
			append("GroupName", GroupName).
			toString();
	}


}
