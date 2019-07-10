package io.slatch.app.db.entity;

import android.arch.persistence.room.Embedded;

public class GroupMemberContact {

	@Embedded
	private GroupMember mGroupMember;

	@Embedded
	private Contact mContact;



	public GroupMember getGroupMember() {
		return mGroupMember;
	}

	public void setGroupMember(GroupMember groupMember) {
		mGroupMember = groupMember;
	}

	public Contact getContact() {
		return mContact;
	}

	public void setContact(Contact contact) {
		mContact = contact;
	}
}
