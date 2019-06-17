package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.Date;


@Entity(tableName = "call_table")
public class Call implements IDialog{

	/***************************************************/
	@NonNull
	@PrimaryKey(autoGenerate = true)
	@SerializedName("call_id")
	@ColumnInfo(name = "call_id")
	@Expose
	private Integer callID;

	/***************************************************/
	@NonNull
	@SerializedName("participant_id")
	@ColumnInfo(name = "participant_id")
	@Expose
	private String participantId;

	/***************************************************/
	@SerializedName("participant_name")
	@ColumnInfo(name = "participant_name")
	@Expose
	private String participantName;

	/***************************************************/
	@SerializedName("participant_profile_pic_url")
	@ColumnInfo(name = "participant_profile_pic_url")
	@Expose
	private String participantProfilePicUrl;

	/***************************************************/
	@SerializedName("participant_language")
	@ColumnInfo(name = "participant_language")
	@Expose
	private String participantLanguage;

	/***************************************************/
	@SerializedName("is_video_call")
	@ColumnInfo(name = "is_video_call")
	@Expose
	private boolean isVideoCall; //Video Or Audio Call

	/***************************************************/
	@SerializedName("call_state")
	@ColumnInfo(name = "call_state")
	@Expose
	private String callState; // Incoming / Outgoing / Missed Call
	/***************************************************/

	@SerializedName("call_start_timestamp")
	@ColumnInfo(name = "call_start_timestamp")
	@Expose
	private long callStartTimeStamp;

	/***************************************************/
	@SerializedName("call_duration")
	@ColumnInfo(name = "call_duration")
	@Expose
	private long callDuration;

	/***************************************************/

	public Call(){ }

	public Call(@NonNull String participantId, String participantName, String participantProfilePicUrl, String participantLanguage,
				boolean isVideoCall, String callState, long callStartTimeStamp, long callDuration) {
		this.participantId = participantId;
		this.participantName = participantName;
		this.participantProfilePicUrl = participantProfilePicUrl;
		this.participantLanguage = participantLanguage;
		this.isVideoCall = isVideoCall;
		this.callState = callState;
		this.callStartTimeStamp = callStartTimeStamp;
		this.callDuration = callDuration;
	}

	@Override
	public void setLastMessageCreatedDate(Date date) {

	}

	@Override
	public void setLastMessageText(String text) {

	}

	@Override
	public String getId() {
		return "";
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
		return new Date(callStartTimeStamp);
	}

	@Override
	public String getLastMessageTextToDisplay() {
			return "";
	}

	@Override
	public String getLastMessageId() {
		return null;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public int getUnreadMessagesCount() {
		return 0;
	}

	@NonNull
	public Integer getCallID() {
		return callID;
	}

	public void setCallID(@NonNull Integer callID) {
		this.callID = callID;
	}

	@NonNull
	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(@NonNull String participantId) {
		this.participantId = participantId;
	}

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	public String getParticipantProfilePicUrl() {
		return participantProfilePicUrl;
	}

	public void setParticipantProfilePicUrl(String participantProfilePicUrl) {
		this.participantProfilePicUrl = participantProfilePicUrl;
	}

	public String getParticipantLanguage() {
		return participantLanguage;
	}

	public void setParticipantLanguage(String participantLanguage) {
		this.participantLanguage = participantLanguage;
	}

	public boolean isVideoCall() {
		return isVideoCall;
	}

	public void setVideoCall(boolean videoCall) {
		isVideoCall = videoCall;
	}

	public String getCallState() {
		return callState;
	}

	public void setCallState(String callState) {
		this.callState = callState;
	}

	public long getCallStartTimeStamp() {
		return callStartTimeStamp;
	}

	public void setCallStartTimeStamp(long callStartTimeStamp) {
		this.callStartTimeStamp = callStartTimeStamp;
	}

	public long getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(long callDuration) {
		this.callDuration = callDuration;
	}

	@Override
	public String toString() {
		return "Call{" +
				"participantId='" + participantId + '\'' +
				", participantName='" + participantName + '\'' +
				", participantProfilePicUrl='" + participantProfilePicUrl + '\'' +
				", participantLanguage='" + participantLanguage + '\'' +
				", isVideoCall=" + isVideoCall +
				", callState='" + callState + '\'' +
				", callStartTimeStamp=" + callStartTimeStamp +
				", callDuration=" + callDuration +
				'}';
	}
}
