package io.slatch.app;

import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;

import io.slatch.app.com.WochatApi;
import io.slatch.app.db.WCDatabase;
import io.slatch.app.logic.NotificationHelper;

public class WCApplication extends Application {

	private AppExecutors mAppExecutors;


	@Override
	public void onCreate() {
		super.onCreate();

		mAppExecutors = new AppExecutors();

		NotificationHelper.createNotificationChannel1(this);

		ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationObserver(this));

		try {
			startService(new Intent(this, WCService.class));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (BuildConfig.DEBUG) {
			WochatApi.BASE_URL = "https://api-dev.slatch.io/";
			XMPPProvider.XMPP_DOMAIN = "ejabberd-dev.slatch.io";
			XMPPProvider.XMPP_PORT = 5223;
		}else {
			WochatApi.BASE_URL = "https://api.slatch.io/";
			XMPPProvider.XMPP_DOMAIN = "ejabberd.slatch.io";
			XMPPProvider.XMPP_PORT = 443;
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
