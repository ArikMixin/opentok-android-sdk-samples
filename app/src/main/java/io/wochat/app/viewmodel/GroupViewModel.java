package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.model.StateData;


public class GroupViewModel extends AndroidViewModel {

	private final WCRepository mRepository;


	public GroupViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
	}


	public MutableLiveData<StateData<String>> uploadImage(byte[] bytes){
		return mRepository.uploadImage(bytes);
	}

	public void createNewGroup(String groupName, byte[] bytes, List<Contact> contactList){
		mRepository.createNewGroup(groupName, bytes, contactList);
	}


}
