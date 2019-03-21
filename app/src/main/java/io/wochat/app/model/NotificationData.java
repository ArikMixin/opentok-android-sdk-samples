package io.wochat.app.model;

import android.graphics.Bitmap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.User;

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
