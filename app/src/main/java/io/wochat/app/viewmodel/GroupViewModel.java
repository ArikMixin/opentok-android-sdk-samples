package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.List;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.GroupMemberContact;
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
		mRepository.createNewGroup(groupName, bytes, contactList, getApplication().getResources());
	}

	public void updateGroupImage(String groupId, byte[] bytes, Resources resources){
		mRepository.updateGroupImage(groupId, bytes, resources);
	}

	public void updateGroupName(String groupId, String newName, Resources resources){
		mRepository.updateGroupName(groupId, newName, resources);
	}

	public MutableLiveData<StateData<Conversation>> getCreateGroupResult(){
		return mRepository.getCreateGroupResult();
	}

	public LiveData<List<GroupMember>> getMembersLD(String groupId){
		return mRepository.getMembersLD(groupId);
	}


	public LiveData<List<GroupMemberContact>> getMembersContact(String groupId){
		return mRepository.getMembersContact(groupId);
	}

	public void removeAdmin(String groupId, String memberId){
		mRepository.removeAdmin(groupId, memberId);
	}

	public void makeAdmin(String groupId, String memberId){
		mRepository.makeAdmin(groupId, memberId);
	}

	public void removeMember(String groupId, String memberId){
		mRepository.removeMember(groupId, memberId);
	}

	public void addMembers(String groupId, String[] memberIds){
		mRepository.addMembers(groupId, memberIds);
	}

	public void getGroupDetailsAndInsertToDB(String groupId, Resources resources){
		mRepository.getGroupDetailsAndInsertToDB(groupId, resources);
	}

}
