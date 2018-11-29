package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.util.logging.Handler;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.com.WochatApi;

public class RegistrationViewModel extends AndroidViewModel {

	private static final String TAG = "RegistrationViewModel";

	private final WCRepository mRepository;

	private MutableLiveData<String> mUserRegistrationError;
	private MutableLiveData<String> mUserVerificationError;

	public RegistrationViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
		mUserRegistrationError = mRepository.getUserRegistrationError();
		mUserVerificationError = mRepository.getUserVerificationError();
	}



	public void userRegistration(String userTrimmedPhone, String userCountryCode) {
		mRepository.userRegistration(userTrimmedPhone, userCountryCode);
	}

	public MutableLiveData<String> getUserRegistrationResult() {
		return mUserRegistrationError;
	}


	public void userVerification(String code){
		mRepository.userVerification(code);
	}

	public MutableLiveData<String> getUserVerificationResult() {
		return mUserVerificationError;
	}
}
