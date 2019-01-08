package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.ContactInvitation;

public class ContactViewModel extends AndroidViewModel {

	private final WCRepository mRepository;


	public ContactViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
	}

	public LiveData<Contact> getContact(String id) {
		return mRepository.getContact(id);
	}

	public LiveData<List<Contact>> getAllContacts() {
		return mRepository.getAllContacts();
	}

	public LiveData<List<ContactInvitation>> getContactInvitations() {
		return mRepository.getContactInvitations();
	}

	public void sycContacts(){
		mRepository.syncContactsLocalAndServer();
	}


	public void updateInvited(String contactId){
		mRepository.updateInvited(contactId);
	}

}
