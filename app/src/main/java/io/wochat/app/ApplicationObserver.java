package io.wochat.app;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ApplicationObserver implements LifecycleObserver {

	public static final String APP_OBSERVER_ACTION = "com.hitshot.application_observer";
	public static final String APP_OBSERVER_EXTRA = "LifecycleEvent";

	private final Context mContext;

	ApplicationObserver(Context context) {
		mContext = context;
	}



	@OnLifecycleEvent(Lifecycle.Event.ON_START)
	void onForeground() {
		sendBroadcast(Lifecycle.Event.ON_START);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
	void onBackground() {
		sendBroadcast(Lifecycle.Event.ON_STOP);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
	void onDestroy() {
		sendBroadcast(Lifecycle.Event.ON_DESTROY);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
	void onCreate() {
		sendBroadcast(Lifecycle.Event.ON_CREATE);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
	void onPause() {
		sendBroadcast(Lifecycle.Event.ON_PAUSE);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
	void onResume() {
		sendBroadcast(Lifecycle.Event.ON_RESUME);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_ANY)
	void onAny() {
		sendBroadcast(Lifecycle.Event.ON_ANY);
	}



	private void sendBroadcast(Lifecycle.Event event){
		Intent intent = new Intent();
		intent.setAction(APP_OBSERVER_ACTION);
		intent.putExtra(APP_OBSERVER_EXTRA, event);
		mContext.sendBroadcast(intent);
	}
}


