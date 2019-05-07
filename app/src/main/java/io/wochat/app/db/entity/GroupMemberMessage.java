package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.wochat.app.R;
import io.wochat.app.utils.Utils;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = "group_member_message_table",
	primaryKeys= { "user_id", "group_id", "message_id" },
	indices = {@Index("group_id"), @Index("user_id"), @Index("message_id")},
	foreignKeys = {

		@ForeignKey(entity = Contact.class,
			parentColumns = "contact_id",
			childColumns = "user_id",
			onDelete = CASCADE),

		@ForeignKey(entity = Conversation.class,
			parentColumns = "conversation_id",
			childColumns = "group_id",
			onDelete = CASCADE),

		@ForeignKey(entity = Message.class,
			parentColumns = "message_id",
			childColumns = "message_id",
			onDelete = CASCADE)})


public class GroupMemberMessage {

	@NonNull
    @SerializedName("user_id")
	@ColumnInfo(name = "user_id")
    @Expose
    private String userId;

	@NonNull
	@SerializedName("group_id")
	@ColumnInfo(name = "group_id")
	@Expose
	private String groupId;




	@NonNull
	@SerializedName("message_id")
	@ColumnInfo(name = "message_id")
	@Expose
	private String messageId;


	@SerializedName("ack_status")
	@ColumnInfo(name = "ack_status")
	@Expose
	private @Message.ACK_STATUS
	String ackStatus;


	public GroupMemberMessage(){
		this.ackStatus = Message.ACK_STATUS_PENDING;
	}

	public GroupMemberMessage(String userId, String groupId, String userName, String messageId){
		this.userId = userId;
		this.groupId = groupId;
		this.messageId = messageId;
		this.ackStatus = Message.ACK_STATUS_PENDING;
	}


	@Override
    public String toString() {
		return new ToStringBuilder(this).
			append("userId", userId).
			append("groupId", groupId).
			append("messageId", messageId).
			append("ackStatus", ackStatus).
			toString();
    }


	@NonNull
	public String getUserId() {
		return userId;
	}

	public void setUserId(@NonNull String userId) {
		this.userId = userId;
	}

	@NonNull
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(@NonNull String groupId) {
		this.groupId = groupId;
	}


	public @Message.ACK_STATUS String getAckStatus() {
		return ackStatus;
	}

	public void setAckStatus(@Message.ACK_STATUS String ackStatus) {
		this.ackStatus = ackStatus;
	}

	@NonNull
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(@NonNull String messageId) {
		this.messageId = messageId;
	}
}
