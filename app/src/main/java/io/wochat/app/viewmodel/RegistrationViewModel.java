package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import java.util.List;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.User;
import io.wochat.app.model.StateData;

public class RegistrationViewModel extends AndroidViewModel {


	private LiveData<List<User>> mAllUsers;

	public enum RegistrationPhase {
		reg_phase_1_phone_num,
		reg_phase_2_sms_code,
		reg_phase_3_pic,
		reg_phase_4_finish
	}

	private static final String TAG = "RegistrationViewModel";

	private final WCRepository mRepository;
	private final MutableLiveData<StateData<WCRepository.UserRegistrationState>> mUserState;
	private final MutableLiveData<StateData<String>> mUploadProfilePicResult;
	private final MutableLiveData<StateData<String>> mUserConfirmRegistrationResult;
	private final MediatorLiveData<StateData<String>> mUserFinishRegistrationResult;
	private final LiveData<RegistrationPhase> mUserRegistrationPhase;
	private MutableLiveData<StateData<String>> mUserVerificationResult;
	private final MutableLiveData<StateData<String>> mUserRegistrationResult;

	public RegistrationViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
		mUserFinishRegistrationResult = new MediatorLiveData<>();


		mAllUsers = mRepository.getAllUsers();

		mUserState = mRepository.getUserRegistrationState();
		mUserVerificationResult = mRepository.getUserVerificationResult();
		mUserRegistrationResult = mRepository.getUserRegistrationResult();

		mRepository.retrieveLocalContacts();

//		mUserRegistrationPhase = Transformations.switchMap(mUserState, new Function<StateData<WCRepository.UserRegistrationState>, LiveData<RegistrationPhase>>() {
//			@Override
//			public LiveData<RegistrationPhase> apply(StateData<WCRepository.UserRegistrationState> input) {
//				return mUserRegistrationPhase;
//			}
//		});



		mUserRegistrationPhase = Transformations.map(mUserState, input -> {
			switch (input.getData()) {
				case user_reg_ok:
					return RegistrationPhase.reg_phase_2_sms_code;

				case user_sms_verification_ok:
					return RegistrationPhase.reg_phase_3_pic;

				case user_upload_profile_pic_ok:
					return RegistrationPhase.reg_phase_3_pic;

				case user_confirm_reg_ok:
					return RegistrationPhase.reg_phase_4_finish;

				default:
					return RegistrationPhase.reg_phase_1_phone_num;
			}
		});



		mUploadProfilePicResult = mRepository.getUploadProfilePicResult();
		mUserConfirmRegistrationResult = mRepository.getUserConfirmRegistrationResult();
		mUserFinishRegistrationResult.addSource(mUploadProfilePicResult, value -> mUserFinishRegistrationResult.setValue(value));
		mUserFinishRegistrationResult.addSource(mUserConfirmRegistrationResult, value -> mUserFinishRegistrationResult.setValue(value));
	}

	private LiveData<RegistrationPhase> getXXX(StateData<String> xxx) {

		return null;
	}


	public void userRegistration(String userTrimmedPhone, String userCountryCode) {
		mRepository.userRegistration(userTrimmedPhone, userCountryCode);
	}

	public MutableLiveData<StateData<String>> getUserRegistrationResult() {
		return mUserRegistrationResult;
	}


	public void userVerification(String code){
		mRepository.userVerification(code);
	}

	/*public void userVerification(String code){
		mRepository.userVerification(code, new WCRepository.APIResultListener() {
			@Override
			public void onApiResult(boolean isSuccess) {
				if (isSuccess)
					mUserRegistrationPhase.setValue(RegistrationPhase.reg_phase_3_pic);
			}
		});
	}*/

	public MutableLiveData<StateData<String>> getUserVerificationResult() {
		return mUserVerificationResult;
	}

	private MutableLiveData<StateData<String>> getUploadProfilePicResult() {
		return mRepository.getUploadProfilePicResult();
	}

	public String getUserCountryCode(){
		return mRepository.getUserCountryCode();
	}

	public boolean hasUserRegistrationData() {
		return mRepository.hasUserRegistrationData();
	}

	public MutableLiveData<StateData<String>> getUserFinishRegistrationResult() {
		return mUserFinishRegistrationResult;
	}


	public void userFinishRegistration(byte[] bytes, String userName){
		mRepository.userFinishRegistration(bytes, userName);
	}


	public LiveData<RegistrationPhase> getUserRegistrationPhase() {
		return mUserRegistrationPhase;
	}

//	public MutableLiveData<StateData<WCRepository.UserRegistrationState>> getUserRegistrationState() {
//		return mUserState;
//	}

	LiveData<List<User>> getAllWords() {
		return mAllUsers;
	}

	void insert(User user) {
		mRepository.insert(user);
	}
}
