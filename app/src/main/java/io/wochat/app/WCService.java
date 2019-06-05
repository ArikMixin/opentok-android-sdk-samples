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
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.Message;
import io.wochat.app.logic.NotificationHelper;
import io.wochat.app.ui.AudioVideoCall.CallActivity;
import io.wochat.app.ui.AudioVideoCall.IncomingCallActivity;
import io.wochat.app.ui.Consts;


public class WCService extends Service implements XMPPProvider.OnChatMessageListener {
	private static final String TAG = "WCService";

	public static final String TYPING_SIGNAL_ACTION = "TYPING_SIGNAL_ACTION";
	public static final String CONVERSATION_ID_EXTRA = "CONVERSATION_ID_EXTRA";

	public static final String PRESSENCE_ACTION = "PRESSENCE_ACTION";
	public static final String PRESSENCE_IS_AVAILABLE_EXTRA = "PRESSENCE_IS_AVAILABLE_EXTRA";
	public static final String PRESSENCE_CONTACT_ID_EXTRA = "PRESSENCE_CONTACT_ID_EXTRA";

	public static final String LAST_ONLINE_ACTION = "LAST_ONLINE_ACTION";
	public static final String LAST_ONLINE_TIME_EXTRA = "LAST_ONLINE_TIME_EXTRA";
	public static final String LAST_ONLINE_IS_AVAILABLE_EXTRA = "LAST_ONLINE_IS_AVAILABLE_EXTRA";
	public static final String LAST_ONLINE_CONTACT_ID_EXTRA = "LAST_ONLINE_CONTACT_ID_EXTRA";

	public static final String IS_TYPING_EXTRA = "IS_TYPING_EXTRA";

	private final IBinder mBinder = new WCBinder();
	private XMPPProvider mXMPPProvider;
	private WCRepository mRepository;
	private String mSelfUserId, mSelfUserLang;
	private AppObserverBR mAppObserverBR;
	private AppExecutors mAppExecutors;
	private String mCurrentConversationId;
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


	private boolean isMessageOfUserNotificationType(Message message){
		switch (message.getMessageType()){
			case Message.MSG_TYPE_AUDIO:
			case Message.MSG_TYPE_CONTACT:
			case Message.MSG_TYPE_GIF:
			case Message.MSG_TYPE_IMAGE:
			case Message.MSG_TYPE_LOCATION:
			case Message.MSG_TYPE_SPEECHABLE:
			case Message.MSG_TYPE_TEXT:
			case Message.MSG_TYPE_VIDEO:
				return true;
		}
		return false;
	}

	@Override
	public void onNewIncomingMessage(String msg, String conversationId) {
		try {
			Message message = Message.fromJson(msg);

			if (message.getMessageType().equals(Message.MSG_TYPE_TYPING_SIGNAL)){
					broadcastTypingSignal(conversationId, message.isTyping());
				return;
			}

			//WEB RTC Messages (ANSWER, TEXT, CLOSE, REJECTED, BUSY, UPDATE_SESSION)
			if (message.getRtcCode() != null && !message.getRtcCode().equals(Message.RTC_CODE_OFFER)){
				broadcastRTCcodeChanged(message.getRtcCode());
				return;
			}

			/*****************************************************************************************/
			// ios patches
			if (message.getDurationMili() == 0){
				message.setDurationMili(message.getDuration()*1000);
			}
			message.setConversationId(conversationId);
//			if (message.getTimestampMilli() == 0)
//				message.setTimestampMilli(message.getTimestamp()*1000);

			/*****************************************************************************************/

			boolean res = mRepository.handleIncomingMessage(message, new WCRepository.OnSaveMessageToDBListener() {
				@Override
				public void OnSaved(boolean success, Message savedMessage, Contact contact) {
					sendAckStatusForIncomingMessage(savedMessage, Message.ACK_STATUS_RECEIVED);
					if (isMessageOfUserNotificationType(savedMessage)){
						if (!message.getConversationId().equals(mCurrentConversationId)){
							NotificationHelper.handleNotificationIncomingMessage(getApplication(), savedMessage, contact);
						}
					}

					Log.d("arik_test", "OnSaved: " + message.getRtcCode());
					//Open IncomingCallActivity Activity if video call received
					if (message.getRtcCode() != null && message.getRtcCode().equals(Message.RTC_CODE_OFFER)){
							OpenIncomingCallActivity(message, contact);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	@Override
	public void onNewOutgoingMessage(String msg, String conversationId) {
		Message message = Message.fromJson(msg);
		switch (message.getMessageType()){
			case Message.MSG_TYPE_TEXT:
			case Message.MSG_TYPE_SPEECHABLE:
			case Message.MSG_TYPE_IMAGE:
			case Message.MSG_TYPE_AUDIO:
			case Message.MSG_TYPE_GIF:
			case Message.MSG_TYPE_VIDEO:
			case Message.MSG_TYPE_LOCATION:
				mRepository.updateAckStatusToSent(message);
				break;
			default:
				break;
		}
	}

	@Override
	public void onConnectionChange(boolean connected, boolean authenticated) {
		if(connected && authenticated) {
			((WCApplication) getApplication()).getAppExecutors().diskIO().execute(() -> {
				List<Conversation> convList = mRepository.getAllConversations();
				List<Message> msgs = mRepository.getOutgoingPendingMessages();
				mAppExecutors.networkIO().execute(() -> {
					subscribe(convList);
					if ((msgs != null) && (!msgs.isEmpty()))
						sendMessages(msgs);
				});

			});
		}
	}

	public boolean isXmppConnected(){
		return (mXMPPProvider != null)&& (mXMPPProvider.isConnectedAndAuthenticated());
	}

	@Override
	public void onPresenceChanged(boolean isAvailable, String contactId) {
		broadcastPresenceChange(isAvailable, contactId);
	}

	public void subscribe(List<Conversation> conversations) {
		for(Conversation conversation : conversations){
			subscribe(conversation.getParticipantId(), conversation.getParticipantName());
		}
	}

	public void setCurrentConversationId(String conversationId) {
		Log.e(TAG, "CurrentConversationId: " + conversationId);
		mCurrentConversationId = conversationId;
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
		mAppExecutors = ((WCApplication) getApplication()).getAppExecutors();
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
		mSelfUserLang = WCSharedPreferences.getInstance(this).getUserLang();

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
		if (mXMPPProvider != null)
			mXMPPProvider.disconnectAsync();
		unregisterReceiver(mAppObserverBR);
		super.onDestroy();
	}

	public void unSubscribeContact(String contactId){
		mXMPPProvider.unSubscribeContact(contactId);
	}

	public void subscribe(String contactId, String name){
		mXMPPProvider.subscribeContact(contactId, name);
	}

	public void getPresence(String contactId){
		mXMPPProvider.getPresence(contactId);
	}


	public void getLastOnline(String contactId){
		mAppExecutors.networkIO().execute(() -> {
			long time = mXMPPProvider.getLastOnline(contactId);
			boolean isPresence = mXMPPProvider.getPresence(contactId);
			getPresence(contactId);
			broadcastLastOnline(contactId, isPresence, time);
		});
	}

	public void getAllPresence(){
		mXMPPProvider.getAllPresence();
	}


	public void sendMessages(List<Message> messages){
		Log.e(TAG, "sendMessages count: " + messages.size());
		for (Message  message : messages) {
			if (message.isImage()){
				if ((message.getMediaUrl() != null) && (!message.getMediaUrl().isEmpty())) // make sure media was uploaded
					sendMessage(message);
			}
			else
				sendMessage(message);
		}
	}

	public void sendMessage(Message message){
		//message.setDuration(message.getDuration()/1000);
		Log.e(TAG, "sendMessage: " + message.toJson());
		mXMPPProvider.sendStringMessage(message.toJson(), message.getParticipantId(), message.getConversationId());
	}

	private void sendAckStatusForIncomingMessage(Message message, @Message.ACK_STATUS String ackStatus) {
		Message msg = new Message(message.getSenderId(), mSelfUserId, message.getConversationId(), "", "EN");
		msg.setMessageType(Message.MSG_TYPE_ACKNOWLEDGMENT);
		msg.setAckStatus(ackStatus);
		msg.setOriginalMessageId(message.getMessageId());
		mXMPPProvider.sendStringMessage(msg.toJson(), msg.getParticipantId(), message.getConversationId());
		if (ackStatus.equals(Message.ACK_STATUS_READ)){
			NotificationHelper.deleteNotification(this, message);
		}
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

	private void broadcastPresenceChange(boolean isAvailiable, String contactId) {
		Intent intent = new Intent();
		intent.setAction(PRESSENCE_ACTION);
		intent.putExtra(PRESSENCE_IS_AVAILABLE_EXTRA, isAvailiable);
		intent.putExtra(PRESSENCE_CONTACT_ID_EXTRA, contactId);
		sendBroadcast(intent);
	}

	private void broadcastLastOnline(String contactId, boolean isPresence, long lastOnlineTime) {
		mAppExecutors.mainThread().execute(() -> {
			Intent intent = new Intent();
			intent.setAction(LAST_ONLINE_ACTION);
			intent.putExtra(LAST_ONLINE_TIME_EXTRA, lastOnlineTime);
			intent.putExtra(LAST_ONLINE_IS_AVAILABLE_EXTRA, isPresence);
			intent.putExtra(LAST_ONLINE_CONTACT_ID_EXTRA, contactId);
			sendBroadcast(intent);
		});
	}

//	private void broadcastRTCcodeChanged(boolean rejectedFlag) {
//		Intent intent = new Intent();
//		if(rejectedFlag)
//			intent.setAction(Message.RTC_CODE_REJECTED);
//		else
//			intent.setAction(Message.RTC_CODE_BUSY);
//		sendBroadcast(intent);
//	}

	private void broadcastRTCcodeChanged(String mRtcCode) {
		Intent intent = new Intent();
			intent.setAction(mRtcCode);
		sendBroadcast(intent);
	}


	private void OpenIncomingCallActivity(Message message, Contact contact) {

		//If incoming activity open send BUSY back to sender
		if(IncomingCallActivity.activityActiveFlag) {
				Message message_busy = new Message(message.getParticipantId(), mSelfUserId, message.getConversationId(),
							message.getSessionID(), "", "",
						    Message.RTC_CODE_BUSY, message.getIsVideoRTC(), false);
				sendMessage(message_busy);
		}else {

				Intent intent = new Intent(this, CallActivity.class);
				intent.putExtra(Consts.INTENT_PARTICIPANT_ID, message.getParticipantId());
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, contact.getName());
				intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, contact.getLanguage());
				intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, contact.getAvatar());
				intent.putExtra(Consts.INTENT_SESSION_ID, message.getSessionID());
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, message.getId());
				//intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
				intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
				intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
				//intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
				intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, message.getIsVideoRTC());
		     	intent.putExtra(Consts.OUTGOING_CALL_FLAG, false);
			    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
		}
	}
}
