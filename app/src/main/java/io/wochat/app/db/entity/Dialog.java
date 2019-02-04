package io.wochat.app.db.entity;

import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;
import java.util.Date;

/*
 * Created by troy379 on 04.04.17.
 */
public class Dialog implements IDialog<Message> {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private ArrayList<Contact> contacts;
    //private Message lastMessage;
    private Date lastMessageCreatedDate;
    private String lastMessageText;
    private String lastMessageId;

    private int unreadCount;

    public Dialog(String id,
				  String name,
				  String photo,
                  ArrayList<Contact> contacts,
				  String lastMessageId,
				  Date lastMessageCreatedDate,
				  String lastMessageText,
				  int unreadCount) {

        this.id = id;
        this.dialogName = name;
        this.dialogPhoto = photo;
        this.contacts = contacts;
        this.lastMessageCreatedDate = lastMessageCreatedDate;
        this.lastMessageText = lastMessageText;
        this.lastMessageId = lastMessageId;
        this.unreadCount = unreadCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

//    @Override
//    public ArrayList<Contact> getContacts() {
//        return contacts;
//    }

    @Override
    public Date getLastMessageCreatedDate() {
        return lastMessageCreatedDate;
    }

    @Override
    public String getLastMessageText() {
        return lastMessageText;
    }

    @Override
    public String getLastMessageId() {
        return lastMessageId;
    }

	@Override
	public boolean isGroup() {
		return (contacts != null)&& (contacts.size() > 1);
	}

	@Override
    public void setLastMessageCreatedDate(Date date) {
        lastMessageCreatedDate = date;

    }

    @Override
    public void setLastMessageText(String text) {
        lastMessageText = text;

    }


    @Override
    public int getUnreadMessagesCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
