package io.slatch.app.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@Entity(tableName = "notif_table")
public class Notif {

	@PrimaryKey
	@NonNull
    @SerializedName("message_id")
    @Expose
    private String messageId;

    /************************************************/

    @SerializedName("conversation_id")
    @Expose
    private String conversationId;

    /************************************************/

    @SerializedName("contact_id")
    @Expose
    private String contactId;

    /************************************************/

    @SerializedName("is_displayed")
    @Expose
    private boolean isDisplayed;

    /************************************************/

    @SerializedName("is_canceled")
    @Expose
    private boolean isCanceled;

    /************************************************/

	@SerializedName("timestamp")
	@Expose
	private Date timestamp;



    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }


    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public boolean isDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(boolean displayed) {
        isDisplayed = displayed;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


    @Override
    public String toString() {
		return new ToStringBuilder(this).
			append("messageId", messageId).
			append("conversationId", conversationId).
            append("contactId", contactId).
			append("timestamp", timestamp).
			append("isDisplayed", isDisplayed).
			append("isCanceled", isCanceled).
			toString();
    }

}
