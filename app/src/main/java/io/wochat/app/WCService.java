package io.wochat.app;

import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.List;

import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.entity.Ack;
import io.wochat.app.db.entity.Message;


public class WCService extends Service implements XMPPProvider.OnChatMessageListener {
	private static final String TAG = "WCService";
	private final IBinder mBinder = new WCBinder();
	private XMPPProvider mXMPPProvider;
	private WCRepository mRepository;
	private String mSelfUserId;
	private AppObserverBR mAppObserverBR;
//	private WCDatabase mDatabase;
//	private ConversationDao mConversationDao;
//	private MessageDao mMessageDao;

	public WCService() {

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}


	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "WC Service onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onNewIncomingMessage(String msg) {
		Message message = Message.fromJson(msg);
		boolean res = mRepository.handleIncomingMessage(message, new WCRepository.OnSaveMessageToDBListener() {
			@Override
			public void OnSaved(boolean success, Message savedMessage) {
				sendAckStatusForIncomingMessage(savedMessage, Ack.ACK_STATUS_RECEIVED);
			}
		});

	}


	@Override
	public void onNewOutgoingMessage(String msg) {
		Message message = Message.fromJson(msg);
		mRepository.updateAckStatusToSent(message.getMessageId());
	}


	public class WCBinder extends Binder {
		public WCService getService() {
			Log.e(TAG, "WC Service Binder getService");
			init(); // in case was not init
			return WCService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "WC Service onCreate");
		init();
		if (mAppObserverBR == null) {
			mAppObserverBR = new AppObserverBR();
			IntentFilter filter = new IntentFilter();
			filter.addAction(ApplicationObserver.APP_OBSERVER_ACTION);
			try {
				registerReceiver(new AppObserverBR(), filter);
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}


	private void init() {
		Log.e(TAG, "init");
		if ((mSelfUserId != null)&& (mXMPPProvider != null)) {
			Log.e(TAG, "init - already init");
			return;
		}


		mSelfUserId = WCSharedPreferences.getInstance(this).getUserId();

		if (mSelfUserId == null) {
			Log.e(TAG, "init - no self user id");
			return;
		}

		String pass = WCSharedPreferences.getInstance(this).getXMPPPassword();
		mXMPPProvider = ((WCApplication)getApplication()).getXMPPProvider();
		mRepository = ((WCApplication)getApplication()).getRepository();

		mXMPPProvider.setOnChatMessageListener(this);
		Log.e(TAG, "initAsync called");
		mXMPPProvider.initAsync(mSelfUserId, pass);


	}


	@Override
	public void onDestroy() {
		mXMPPProvider.disconnectAsync();
		super.onDestroy();
	}



	public void sendMessage(Message message){
		mXMPPProvider.sendStringMessage(message.toJson(), message.getParticipantId());
	}

	private void sendAckStatusForIncomingMessage(Message message, @Ack.ACK_STATUS String ackStatus) {
		Message msg = new Message(message.getSenderId(), mSelfUserId, message.getConversationId(), "");
		msg.setMessageType(Message.MSG_TYPE_ACKNOWLEDGMENT);
		List<Ack> aks = new ArrayList<>();
		Ack ack = new Ack(ackStatus, message.getMessageId());
		aks.add(ack);
		msg.setAckList(aks);
		mXMPPProvider.sendStringMessage(msg.toJson(), msg.getParticipantId());
	}

	public void sendAckStatusForIncomingMessages(List<Message> messages, String ackStatus) {
		if ((messages == null)||(messages.isEmpty()))
			return;

		List<Ack> aks = new ArrayList<>();
		Message msg = new Message(messages.get(0).getSenderId(), mSelfUserId, messages.get(0).getConversationId(), "");
		msg.setMessageType(Message.MSG_TYPE_ACKNOWLEDGMENT);
		for (Message message : messages) {
			Ack ack = new Ack(ackStatus, message.getMessageId());
			aks.add(ack);
		}
		msg.setAckList(aks);
		mXMPPProvider.sendStringMessage(msg.toJson(), msg.getParticipantId());
	}


	private class AppObserverBR extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ApplicationObserver.APP_OBSERVER_ACTION)) {
				Lifecycle.Event event = (Lifecycle.Event) intent.getSerializableExtra(ApplicationObserver.APP_OBSERVER_EXTRA);
				//Log.e(TAG, "onReceive: " + event);
				switch (event) {

					case ON_CREATE:
						break;
					case ON_START:
						if (mXMPPProvider!= null)
							mXMPPProvider.connectAndLoginAsync();
						break;
					case ON_RESUME:
						break;
					case ON_PAUSE:
						break;
					case ON_STOP:
						if (mXMPPProvider!= null)
							mXMPPProvider.disconnectAsync();
						break;
					case ON_DESTROY:
						break;
					case ON_ANY:
						break;
				}
			}
		}
	}

}
