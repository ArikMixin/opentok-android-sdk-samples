package io.slatch.app.model;

import android.graphics.Bitmap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.slatch.app.db.entity.Contact;
import io.slatch.app.db.entity.Conversation;
import io.slatch.app.db.entity.User;

public class NotificationData {
	public String contactName;
	public String title;
	public String body;
	public Bitmap largeIcon;
	public String conversationId;
	public String messageId;
	public Contact contact;
	public User selfUser;
	public Conversation conversation;


	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("messageId", messageId).
			append("contactName", contactName).
			append("title", title).
			append("body", body).
			append("conversationId", conversationId).
			toString();
	}
}
