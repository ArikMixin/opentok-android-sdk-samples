package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.model.VideoAudioCall;
import io.wochat.app.ui.AudioVideoCall.CallActivity;

public class VideoAudioCallViewModel extends AndroidViewModel {
    private WCRepository mRepository;

    public VideoAudioCallViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((WCApplication) application).getRepository();
    }

    public void createSessionsAndToken(CallActivity callActivity, String sessionType){
            mRepository.createSession(callActivity, sessionType);
    }

    public void createTokenInExistingSession(CallActivity callActivity, String sessionId, String tokenRoleType){
        mRepository.createToken(callActivity, sessionId , tokenRoleType);
    }

    public MutableLiveData<VideoAudioCall> getSessionAndToken() {
       return mRepository.getSessionsAndToken();
    }
}
