package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.model.StateData;

public class RegistrationViewModel extends AndroidViewModel {

	private static final String TAG = "RegistrationViewModel";

	private final WCRepository mRepository;

//	private MutableLiveData<String> mUserRegistrationResult;
//	private MutableLiveData<String> mUserVerificationResult;

	public RegistrationViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
//		mUserRegistrationResult = mRepository.getUserRegistrationResult();
//		mUserVerificationResult = mRepository.getUserVerificationResult();
	}



	public void userRegistration(String userTrimmedPhone, String userCountryCode) {
		mRepository.userRegistration(userTrimmedPhone, userCountryCode);
	}

	public MutableLiveData<StateData<String>> getUserRegistrationResult() {
		//return mUserRegistrationResult;
		return mRepository.getUserRegistrationResult();
	}


	public void userVerification(String code){
		mRepository.userVerification(code);
	}

	public MutableLiveData<StateData<String>> getUserVerificationResult() {
		//return mUserVerificationResult;
		return mRepository.getUserVerificationResult();
	}
}
