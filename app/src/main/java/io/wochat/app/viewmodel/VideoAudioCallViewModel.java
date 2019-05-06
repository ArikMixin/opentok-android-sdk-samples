package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.model.StateData;

public class VideoAudioCallViewModel extends AndroidViewModel {

    private WCRepository mRepository;

    public VideoAudioCallViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((WCApplication) application).getRepository();
    }

    public void createSessionsAndToken(String sessionType){
         mRepository.createSession(sessionType);
    }

    public LiveData<StateData<String>> getVideoSessionResult() {
        return mRepository.getVideoSessionResult();
    }

}
