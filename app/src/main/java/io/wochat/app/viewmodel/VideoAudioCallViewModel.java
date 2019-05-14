package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.model.VideoAudioCall;
import io.wochat.app.ui.AudioVideoCall.IncomingCallActivity;
import io.wochat.app.ui.AudioVideoCall.OutGoingCallActivity;

public class VideoAudioCallViewModel extends AndroidViewModel {
    private WCRepository mRepository;

    public VideoAudioCallViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((WCApplication) application).getRepository();
    }

    public void createSessionsAndToken(OutGoingCallActivity outGoingCallActivity, String sessionType){
            mRepository.createSession(outGoingCallActivity, sessionType);
    }

    public void createTokenInExistingSession(IncomingCallActivity outGoingCallActivity, String sessionId, String tokenRoleType){
        mRepository.createToken(outGoingCallActivity, sessionId , tokenRoleType);
    }

    public MutableLiveData<VideoAudioCall> getSessionAndToken() {
       return mRepository.getSessionsAndToken();
    }
}
