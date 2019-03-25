package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.model.SupportedLanguage;

public class SupportedLanguagesViewModel extends AndroidViewModel {

	WCRepository mRepository;

	public SupportedLanguagesViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
	}

	public MutableLiveData<List<SupportedLanguage>> getSupportedLanguages() {
		return mRepository.getSupportedLanguages();
	}

	public void loadLanguages(String deviceLanguageCode) {
		mRepository.loadLanguages(deviceLanguageCode);
	}
}
