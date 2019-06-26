package io.wochat.app.model;

import com.stfalcon.chatkit.commons.models.IContact;

import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Conversation;

public class ContactOrGroup implements IContact {
	private Contact mContact;
	private Conversation mConversation;

	public boolean isContact(){
		return mContact != null;
	}

	public boolean isGroup(){
		return mConversation != null;
	}


	public Contact getContact() {
		return mContact;
	}

	public void setContact(Contact contact) {
		mContact = contact;
		mConversation = null;
	}

	public Conversation getConversation() {
		return mConversation;
	}

	public void setConversation(Conversation conversation) {
		mConversation = conversation;
		mContact = null;
	}

	@Override
	public String getId() {
		if (isContact())
			return mContact.getId();
		else
			return mConversation.getId();

	}

	@Override
	public String getName() {
		return getDisplayName();
	}

	@Override
	public String getDisplayName(){
		if (isContact())
			return mContact.getDisplayName();
		else
			return mConversation.getDialogName();
	}

	@Override
	public String getAvatar(){
		if (isContact())
			return mContact.getAvatar();
		else
			return mConversation.getDialogPhoto();
	}

	@Override
	public String getStatus() {
		if (isContact())
			return mContact.getStatus();
		else
			return "";
	}

	public String getLanguage(){
		if (isContact())
			return mContact.getLanguage();
		else
			return null;
	}
	public String getInitials(){
		if (isContact())
			return mContact.getInitials();
		else
			return Contact.getInitialsFromName(getDisplayName());

	}

}
