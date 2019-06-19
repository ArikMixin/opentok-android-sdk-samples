package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Call;

public class RecentCallsViewModel extends AndroidViewModel{
    private WCRepository mRepository;

    public RecentCallsViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((WCApplication) application).getRepository();
    }

    public void deleteCall(Integer callID) {
        mRepository.deleteCall(callID);
    }

    public LiveData<List<Call>> getCallsListLD(){
        return mRepository.getAllCallsLD();
    }
}
