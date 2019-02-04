package io.wochat.app.db.entity;

import android.arch.persistence.room.Ignore;
import android.support.annotation.StringDef;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Ack {


	public static final String ACK_STATUS_PENDING = "PENDING";
	public static final String ACK_STATUS_SENT = "SENT";
	public static final String ACK_STATUS_RECEIVED = "RECEIVED";
	public static final String ACK_STATUS_READ = "READ";


	@StringDef({
		ACK_STATUS_PENDING,
		ACK_STATUS_SENT,
		ACK_STATUS_RECEIVED,
		ACK_STATUS_READ})

	@Retention(RetentionPolicy.SOURCE)
	public @interface ACK_STATUS {}




	@SerializedName("ack_status")
	@Expose
	@Ignore
	private @ACK_STATUS String ackStatus;


	@SerializedName("original_message_id")
	@Expose
	@Ignore
	private String originalMessageId;



	public Ack(@ACK_STATUS String ackStatus, String originalMessageId){
		this.ackStatus = ackStatus;
		this.originalMessageId = originalMessageId;
	}

	public Ack(){

	}

	public @ACK_STATUS String getAckStatus() {
		return ackStatus;
	}

	public void setAckStatus(@ACK_STATUS String ackStatus) {
		this.ackStatus = ackStatus;
	}

	public String getOriginalMessageId() {
		return originalMessageId;
	}

	public void setOriginalMessageId(String originalMessageId) {
		this.originalMessageId = originalMessageId;
	}
}
