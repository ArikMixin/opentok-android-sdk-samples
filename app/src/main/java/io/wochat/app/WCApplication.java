package io.wochat.app;

import android.app.Application;

import io.wochat.app.db.WCDatabase;

public class WCApplication extends Application {

	private AppExecutors mAppExecutors;

	@Override
	public void onCreate() {
		super.onCreate();

		mAppExecutors = new AppExecutors();
	}

	public WCDatabase getDatabase() {
		return null;
		//return WCDatabase.getInstance(this, mAppExecutors);
	}

	public WCRepository getRepository() {
		return WCRepository.getInstance(this, getDatabase());
	}
}
