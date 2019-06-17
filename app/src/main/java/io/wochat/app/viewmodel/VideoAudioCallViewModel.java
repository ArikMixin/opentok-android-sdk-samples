package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Call;
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


    public void translateText(String textToTranslate, String fromLang){
        mRepository.translate(textToTranslate, fromLang);
    }

    public void resetTranslatedText() {
         mRepository.resetTranslatedText();
    }

    public void addNewCall(Call call) {
        mRepository.addNewCall(call);
    }

//    public void updateCall(Integer callId, String callDuration, String callState) {
//        mRepository.updateCall(callId, callDuration, callState);
//    }

    public LiveData<VideoAudioCall> getSessionAndToken() {
        return mRepository.getSessionsAndToken();
    }

    public LiveData<String> getTranslatedText() {
        return mRepository.getTranslationFromPush2Talk();
    }

}
