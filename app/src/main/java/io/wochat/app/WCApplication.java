package io.wochat.app;

import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;

import io.wochat.app.db.WCDatabase;

public class WCApplication extends Application {

	private AppExecutors mAppExecutors;

	@Override
	public void onCreate() {
		super.onCreate();

		mAppExecutors = new AppExecutors();
		ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationObserver(this));

		try {
			startService(new Intent(this, WCService.class));
		} catch (Exception e) {
			e.printStackTrace();
		}


	}



	public AppExecutors getAppExecutors(){
		return mAppExecutors;
	}
	public WCDatabase getDatabase() {
		//return null;
		return WCDatabase.getInstance(this, mAppExecutors);
	}

	public WCRepository getRepository() {
		return WCRepository.getInstance(this, getDatabase(), mAppExecutors);
	}

	public XMPPProvider getXMPPProvider() {
		return XMPPProvider.getInstance(this, mAppExecutors);
	}
}
