package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IContact;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

import io.wochat.app.utils.Utils;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "conversation_table",
		indices = {@Index("last_message_id"), @Index("participant_id"), @Index("last_message_sender_id")},
		foreignKeys = {
			@ForeignKey(entity = Message.class,
				parentColumns = "message_id",
				childColumns = "last_message_id",
				onDelete = ForeignKey.SET_NULL),

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
	@SerializedName("participant_id")
	@ColumnInfo(name = "participant_id")
	@Nullable
	@Expose
	private String participantId;

	/***************************************************/
	@SerializedName("participant_profile_pic_url")
	@ColumnInfo(name = "participant_profile_pic_url")
	@Nullable
	@Expose
	private String participantProfilePicUrl;

	/***************************************************/
	@SerializedName("participant_name")
	@ColumnInfo(name = "participant_name")
	@Nullable
	@Expose
	private String participantName;

	/***************************************************/
	@SerializedName("participant_language")
	@ColumnInfo(name = "participant_language")
	@Nullable
	@Expose
	private String participantLanguage;

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
	@SerializedName("last_message_type")
	@ColumnInfo(name = "last_message_type")
	@Expose
	private String lastMessageType;
	/***************************************************/
	@SerializedName("last_message_sender_id")
	@ColumnInfo(name = "last_message_sender_id")
	@Expose
	private String mLastMessageSenderId;
	/***************************************************/
	@SerializedName("last_message_sender_name")
	@ColumnInfo(name = "last_message_sender_name")
	@Expose
	private String mLastMessageSenderName;
	/***************************************************/
	@SerializedName("last_message_duration")
	@ColumnInfo(name = "last_message_duration")
	@Expose
	int mLastMessageDuration;
	/***************************************************/
	@SerializedName("last_message_ack_status")
	@ColumnInfo(name = "last_message_ack_status")
	@Expose
	private @Message.ACK_STATUS String mLastMessageAckStatus;
	/***************************************************/

	@SerializedName("num_of_unread_messages")
	@ColumnInfo(name = "num_of_unread_messages")
	@Expose
	private int numOfUnreadMessages;

	/***************************************************/
	@Nullable
	@SerializedName("magic_button_lang_code")
	@ColumnInfo(name = "magic_button_lang_code")
	@Expose
	private String magicButtonLangCode;

	/***************************************************/

	@SerializedName("is_group")
	@ColumnInfo(name = "is_group")
	@Expose
	private boolean isGroup;
	/***************************************************/
	@SerializedName("group_name")
	@ColumnInfo(name = "group_name")
	@Expose
	private String groupName;
	/***************************************************/
	@SerializedName("group_description")
	@ColumnInfo(name = "group_description")
	@Expose
	private String groupDescription;
	/***************************************************/
	@SerializedName("group_image_url")
	@ColumnInfo(name = "group_image_url")
	@Expose
	private String groupImageUrl;
	/***************************************************/
	@SerializedName("group_created_date")
	@ColumnInfo(name = "group_created_date")
	@Expose
	private Date groupCreatedDate;
	/***************************************************/
	@SerializedName("group_created_by")
	@ColumnInfo(name = "group_created_by")
	@Expose
	private String groupCreatedBy;

	/***************************************************/
	@SerializedName("is_self_in_group")
	@ColumnInfo(name = "is_self_in_group")
	@Expose
	private boolean isSelfInGroup;


	/***************************************************/
	// for group //***********************/
	public Conversation(String conversationId,
						String groupName,
						String groupDescription,
						String groupImageUrl,
						String groupCreatedBy,
						int groupCreatedDate){

		this.conversationId = conversationId;
		this.participantId = null;
		this.isGroup = true;
		this.groupName = groupName;
		this.groupDescription = groupDescription;
		this.groupImageUrl = groupImageUrl;
		//this.groupCreatedDate = Utils.stringToDate(groupCreatedDate, "yyyy-MM-dd HH:mm:ss");
		this.groupCreatedDate = new Date(groupCreatedDate);
		this.groupCreatedBy = groupCreatedBy;
		this.isSelfInGroup = true;

	}


	public Conversation(String participantId, String selfId){
		this.conversationId = getConversationId(participantId, selfId);
		this.participantId = participantId;
	}

	public Conversation(String conversationId, String participantId, String selfId){
		this.conversationId = conversationId;
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

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getParticipantProfilePicUrl() {
		if (isGroup)
			return groupImageUrl;
		else
			return participantProfilePicUrl;
	}

	public void setParticipantProfilePicUrl(String participantProfilePicUrl) {
		this.participantProfilePicUrl = participantProfilePicUrl;
	}

	public String getParticipantLanguage() {
		if (isGroup)
			return null;
		else
			return participantLanguage;
	}

	public void setParticipantLanguage(String participantLanguage) {
		this.participantLanguage = participantLanguage;
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
		if (isGroup)
			return groupName;
		else
			return participantName;
	}

	@Override
	public Date getLastMessageCreatedDate() {
		return new Date(lastMessageTimeStamp);
	}

	public String getLastMessageText() {
		return lastMessageText;
	}

	@Override
	public String getLastMessageTextToDisplay() {
		if (isGroup()){
			String firstName = Utils.getUserFirstName(mLastMessageSenderName);
			if (Utils.isNullOrEmpty(firstName))
				return getLastMessageTextToDisplayInner();
			else
				return firstName + ": " + getLastMessageTextToDisplayInner();
		}
		else {
			return getLastMessageTextToDisplayInner();
		}
	}



	public String getLastMessageTextToDisplayInner() {
		if (lastMessageType.equals(Message.MSG_TYPE_VIDEO)) {
			return "Video (" + Utils.convertSecondsToHMmSs(mLastMessageDuration) + ") ";
		}

		else if (lastMessageType.equals(Message.MSG_TYPE_IMAGE)) {
			return "Image";
		}

		else if (lastMessageType.equals(Message.MSG_TYPE_AUDIO)) {
			return "Audio (" + Utils.convertSecondsToHMmSs(mLastMessageDuration) + ") ";
		}

		else if (lastMessageType.equals(Message.MSG_TYPE_SPEECHABLE)) {
			return "Audio (" + Utils.convertSecondsToHMmSs(mLastMessageDuration) + ") ";
		}

		else
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

	public String getLastMessageType() {
		return lastMessageType;
	}

	public void setLastMessageType(String lastMessageType) {
		this.lastMessageType = lastMessageType;
	}

	public int getLastMessageDuration() {
		return mLastMessageDuration;
	}

	public void setLastMessageDuration(int lastMessageDuration) {
		mLastMessageDuration = lastMessageDuration;
	}

	public String getMagicButtonLangCode() {
		return magicButtonLangCode;
	}

	public void setMagicButtonLangCode(String magicButtonLangCode) {
		this.magicButtonLangCode = magicButtonLangCode;
	}


	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public String getGroupImageUrl() {
		return groupImageUrl;
	}

	public void setGroupImageUrl(String groupImageUrl) {
		this.groupImageUrl = groupImageUrl;
	}

	public Date getGroupCreatedDate() {
		return groupCreatedDate;
	}

	public void setGroupCreatedDate(Date groupCreatedDate) {
		this.groupCreatedDate = groupCreatedDate;
	}

	public String getGroupCreatedBy() {
		return groupCreatedBy;
	}

	public void setGroupCreatedBy(String groupCreatedBy) {
		this.groupCreatedBy = groupCreatedBy;
	}

	public String getLastMessageSenderName() {
		return mLastMessageSenderName;
	}

	public void setLastMessageSenderName(String lastMessageSenderName) {
		mLastMessageSenderName = lastMessageSenderName;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("conversationId", conversationId).
			append("participantId", participantId).
			append("lastMessageId", lastMessageId).
			append("lastMessageType", lastMessageType).
			append("mLastMessageDuration", mLastMessageDuration).
			append("lastMessageText", lastMessageText).
			append("mLastMessageAckStatus", mLastMessageAckStatus).
			append("lastMessageTimeStamp", lastMessageTimeStamp).
			append("numOfUnreadMessages", numOfUnreadMessages).
			append("isGroup", isGroup).
			append("GroupName", groupName).
			toString();
	}


	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}


	public static Conversation fromJson(String jsonString){
		Gson gson = new Gson();
		return gson.fromJson(jsonString, Conversation.class);
	}


	public boolean isSelfInGroup() {
		return isSelfInGroup;
	}

	public void setSelfInGroup(boolean selfInGroup) {
		isSelfInGroup = selfInGroup;
	}
}
