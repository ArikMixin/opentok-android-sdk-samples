package io.wochat.app;

import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;



import java.util.List;

import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.entity.Message;


public class WCService extends Service implements XMPPProvider.OnChatMessageListener {
	private static final String TAG = "WCService";
	public static final String TYPING_SIGNAL_ACTION = "TYPING_SIGNAL_ACTION";
	public static final String CONVERSATION_ID_EXTRA = "CONVERSATION_ID_EXTRA";
	public static final String IS_TYPING_EXTRA = "IS_TYPING_EXTRA";
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
	public void onNewIncomingMessage(String msg, String conversationId) {
		try {
			Message message = Message.fromJson(msg);

			if (message.getMessageType().equals(Message.MSG_TYPE_TYPING_SIGNAL)){
				broadcastTypingSignal(conversationId, message.isTyping());
				return;
			}

			boolean res = mRepository.handleIncomingMessage(message, new WCRepository.OnSaveMessageToDBListener() {
				@Override
				public void OnSaved(boolean success, Message savedMessage) {
					sendAckStatusForIncomingMessage(savedMessage, Message.ACK_STATUS_RECEIVED);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}




	@Override
	public void onNewOutgoingMessage(String msg, String conversationId) {
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
				registerReceiver(mAppObserverBR, filter);
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
		mXMPPProvider.sendStringMessage(message.toJson(), message.getParticipantId(), message.getConversationId());
	}

	private void sendAckStatusForIncomingMessage(Message message, @Message.ACK_STATUS String ackStatus) {
		Message msg = new Message(message.getSenderId(), mSelfUserId, message.getConversationId(), "", "EN");
		msg.setMessageType(Message.MSG_TYPE_ACKNOWLEDGMENT);
		msg.setAckStatus(ackStatus);
		msg.setOriginalMessageId(message.getMessageId());
		mXMPPProvider.sendStringMessage(msg.toJson(), msg.getParticipantId(), message.getConversationId());
	}

	public void sendTypingSignal(String participantId, String conversationId, boolean isTyping) {
		Message msg = new Message(participantId, mSelfUserId, conversationId, "", "EN");
		msg.setMessageType(Message.MSG_TYPE_TYPING_SIGNAL);
		msg.setIsTyping(isTyping);
		mXMPPProvider.sendStringMessage(msg.toJson(), participantId, conversationId);
	}



	public void sendAckStatusForIncomingMessages(List<Message> messages, String ackStatus) {
		if ((messages == null)||(messages.isEmpty()))
			return;

		for (Message message : messages) {
			sendAckStatusForIncomingMessage(message, ackStatus);
		}
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


	private void broadcastTypingSignal(String conversationId, boolean isTyping) {
		Intent intent = new Intent();
		intent.setAction(TYPING_SIGNAL_ACTION);
		intent.putExtra(CONVERSATION_ID_EXTRA, conversationId);
		intent.putExtra(IS_TYPING_EXTRA, isTyping);
		sendBroadcast(intent);
	}

}
