package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.User;
import io.wochat.app.model.StateData;

public class UserViewModel extends AndroidViewModel {

	private final WCRepository mRepository;
	private final MutableLiveData<StateData<String>> mUserProfileEditResult;

	public UserViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
		mUserProfileEditResult = mRepository.getUserProfileEditResult();
	}

	public LiveData<User> getSelfUser() {
		return mRepository.getSelfUser();
	}

	public void updateUserName (String name) {
        mRepository.updateUserName(name);
	}

	public void updateUserStatus(String status) {
		mRepository.updateUserStatus(status);
	}

	public void uploadUpdatedProfilePic(byte[] profilePicByte) {
		mRepository.uploadUpdatedProfilePic(profilePicByte);
	}

	public void updateUserLanguage(String languageCode) {
		mRepository.updateUserLanguage(languageCode);
	}

	public void updateUserCountryCode(String countryCode) {
		mRepository.updateUserCountryCode(countryCode);
	}

	public MutableLiveData<StateData<String>> getUserProfileEditResult() {
		return mUserProfileEditResult;
	}
}
