package io.slatch.app;

import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import io.slatch.app.db.WCSharedPreferences;
import io.slatch.app.db.entity.Call;
import io.slatch.app.db.entity.Contact;
import io.slatch.app.db.entity.GroupMember;
import io.slatch.app.db.entity.Message;
import io.slatch.app.logic.NotificationHelper;
import io.slatch.app.model.ConversationAndItsGroupMembers;
import io.slatch.app.ui.AudioVideoCall.CallActivity;
import io.slatch.app.ui.Consts;

public class WCService extends Service implements XMPPProvider.OnChatMessageListener {
	private static final String TAG = "WCService";

	public static final String TYPING_SIGNAL_ACTION = "TYPING_SIGNAL_ACTION";
	public static final String CONVERSATION_ID_EXTRA = "CONVERSATION_ID_EXTRA";
	public static final String PARTICIPANT_ID_EXTRA = "PARTICIPANT_ID_EXTRA";

	public static final String PRESSENCE_ACTION = "PRESSENCE_ACTION";
	public static final String PRESSENCE_IS_AVAILABLE_EXTRA = "PRESSENCE_IS_AVAILABLE_EXTRA";
	public static final String PRESSENCE_CONTACT_ID_EXTRA = "PRESSENCE_CONTACT_ID_EXTRA";

	public static final String LAST_ONLINE_ACTION = "LAST_ONLINE_ACTION";
	public static final String LAST_ONLINE_TIME_EXTRA = "LAST_ONLINE_TIME_EXTRA";
	public static final String LAST_ONLINE_IS_AVAILABLE_EXTRA = "LAST_ONLINE_IS_AVAILABLE_EXTRA";
	public static final String LAST_ONLINE_CONTACT_ID_EXTRA = "LAST_ONLINE_CONTACT_ID_EXTRA";

	public static final String IS_TYPING_EXTRA = "IS_TYPING_EXTRA";

	public static final String IS_RECORDING = "IS_RECORDING";
	public static final String RTC_MESSAGE = "RTC_MESSAGE";
	public static final String RTC_MESSAGE_LANGUAGE = "MESSAGE LANGUAGE";
	public static final String CALL_EVENT = "CALL_EVENT";

	private final IBinder mBinder = new WCBinder();
	private XMPPProvider mXMPPProvider;
	private WCRepository mRepository;
	private Contact contact;
	private Call call;
	private boolean isVideo;
	private String mSelfUserId, mSelfUserLang;
	private AppObserverBR mAppObserverBR;
	private AppExecutors mAppExecutors;
	private String mCurrentConversationId;
	private String pass;
//	private WCDatabase mDatabase;
//	private ConversationDao mConversationDao;
//	private MessageDao mMessageDao;

	public WCService() { }

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "WC Service bind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e("WCService", "WC Service onUnbind");
		if(pass == null)
		  		 pass = WCSharedPreferences.getInstance(this).getXMPPPassword();
			mXMPPProvider.initAsync(mSelfUserId, pass);
			init();
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
			case Message.MSG_TYPE_GROUP_EVENT:
				return true;
		}
		return false;
	}

	@Override
	public void onNewIncomingMessage(String msg, String conversationId) {
		try {
			Message message = Message.fromJson(msg);
			Log.d(TAG, message.getMessageType());
			//Log.d(TAG, "JSON: " +message.toJson());

			if (message.getMessageType().equals(Message.MSG_TYPE_TYPING_SIGNAL)){
				broadcastTypingSignal(conversationId, message.getSenderId(), message.isTyping());
				return;
			}

			//Open IncomingCallActivity Activity if video call received
			if (message.getRtcCode() != null && message.getRtcCode().equals(Message.RTC_CODE_OFFER)){

				//Ignore "OFFER" msg if more than 30 seconds
				if(System.currentTimeMillis() - message.getTimestampMilli() > 30)
					return;

				openIncomingCallActivity(message);
				return;
			}

			//WEB RTC Messages (ANSWER, TEXT, CLOSE, REJECTED, BUSY, UPDATE_SESSION)
			if (message.getRtcCode() != null && !message.getRtcCode().equals(Message.RTC_CODE_OFFER)){
						broadcastWebRTC(message.getRtcCode(), message.getMessage(),message.getMessageLanguage());
				return;
			}

			//Video Audio - CallEvent
			if (message.getEventCode() != null && (message.getEventCode().equals(Message.EVENT_CODE_MISSED_VIDEO_CALL) ||
													message.getEventCode().equals(Message.EVENT_CODE_MISSED_VOICE_CALL))){
					broadcastCallEvent(message);
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

			boolean res = mRepository.handleIncomingMessage(message, getResources(), new WCRepository.OnSaveMessageToDBListener() {
				@Override
				public void OnSaved(boolean success, Message savedMessage, Contact contact) {
					if (success) {
						sendAckStatusForIncomingMessage(savedMessage, Message.ACK_STATUS_RECEIVED);
						if (isMessageOfUserNotificationType(savedMessage)) {
							if (!message.getConversationId().equals(mCurrentConversationId)) { // If the user not in a conversation	 chat - fire notification
								Log.d("NotificationHelper", "**************************************");
								NotificationHelper.handleNotificationIncomingMessage(getApplication(), savedMessage, contact);
							}
						}
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
				List<ConversationAndItsGroupMembers> convList = mRepository.getAllConversationsAndItsMembers();
				List<Message> msgs = mRepository.getOutgoingPendingMessages();
				mAppExecutors.networkIO().execute(() -> {
					subscribe(convList);
					if ((msgs != null) && (!msgs.isEmpty())) {
						sendMessages(msgs);
					}
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

	public void subscribe(List<ConversationAndItsGroupMembers> conversations) {
		for(ConversationAndItsGroupMembers cgm : conversations){
			subscribe(cgm);
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

		 pass = WCSharedPreferences.getInstance(this).getXMPPPassword();
		mXMPPProvider = ((WCApplication)getApplication()).getXMPPProvider();
		mRepository = ((WCApplication)getApplication()).getRepository();

		mXMPPProvider.setOnChatMessageListener(this);
		Log.e(TAG, "initAsync called");
		mXMPPProvider.initAsync(mSelfUserId, pass);





	}


	@Override
	public void onDestroy() {
		if (mAppObserverBR != null) {
			try {
				unregisterReceiver(mAppObserverBR);
			}
			catch (Exception e) {
			}
			finally {
				mAppObserverBR = null;
			}

		}
		if (mXMPPProvider != null)
			mXMPPProvider.disconnectAsync();
		if(mAppObserverBR != null)
				unregisterReceiver(mAppObserverBR);
		super.onDestroy();
	}

	public void unSubscribeContact(String contactId){
		mXMPPProvider.unSubscribeContact(contactId);
	}

	public void subscribe(ConversationAndItsGroupMembers convAndMembers){
		if (convAndMembers.getConversation().isGroup()) {
			for (GroupMember groupMember : convAndMembers.getGroupMembers()) {
				mXMPPProvider.subscribeContact(groupMember.getUserId(), groupMember.getUserName());
			}
		}
		else {
			mXMPPProvider.subscribeContact(convAndMembers.getConversation().getParticipantId(), convAndMembers.getConversation().getParticipantName());
		}
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
		if (message.isGroupMessage() && (message.getParticipantId() == null)){
			String[] ids = message.getRecipients();
			List<String> participantIds = new ArrayList<>();
			for (int i=0; i<ids.length ; i++){
				participantIds.add(ids[i]);
			}
			sendGroupMessageWithIds(message, participantIds, message.getSenderId());
		}
		else
			mXMPPProvider.sendStringMessage(message.toJson(), message.getParticipantId(), message.getConversationId());
	}

	public void sendGroupMessageWithIds(Message message, List<String> participantIds, String selfId) {
		for (String id : participantIds){
			if(id.equals(selfId)) {
				participantIds.remove(id);
				break;
			}
		}
		Log.e(TAG, "sendGroupMessageWithIds count: " + participantIds.size());
		mXMPPProvider.sendGroupStringMessage(message.toJson(), participantIds, message.getConversationId());
	}


	public void sendGroupMessage(Message message, List<GroupMember> groupMembers, String selfId) {
		Log.e(TAG, "sendGroupMessage count: " + groupMembers.size());
		List<String> participantIds = new ArrayList<>();
		for (GroupMember groupMember : groupMembers){
			if(!groupMember.getUserId().equals(selfId))
				participantIds.add(groupMember.getUserId());
		}
		mXMPPProvider.sendGroupStringMessage(message.toJson(), participantIds, message.getConversationId());
	}

	public void sendGroupMessagesWithIds(List<Message> messages, List<String> participantIds, String selfId){
		for (String id : participantIds){
			if(id.equals(selfId)) {
				participantIds.remove(id);
				break;
			}
		}

		Log.e(TAG, "sendGroupMessagesWithIds count: " + messages.size());
		for (Message  message : messages) {
			if (message.isImage()){
				if ((message.getMediaUrl() != null) && (!message.getMediaUrl().isEmpty())) // make sure media was uploaded
					mXMPPProvider.sendGroupStringMessage(message.toJson(), participantIds, message.getConversationId());
			}
			else
				mXMPPProvider.sendGroupStringMessage(message.toJson(), participantIds, message.getConversationId());
		}
	}

	public void sendGroupMessages(List<Message> messages, String selfId){
		List<String> participantIds = new ArrayList<>();
		String[] recipients = messages.get(0).getRecipients();
		for (int i=0; i<recipients.length; i++){
			if(!recipients[i].equals(selfId))
				participantIds.add(recipients[i]);
		}

		Log.e(TAG, "sendGroupMessages count: " + messages.size());
		for (Message  message : messages) {
			if (message.isImage()){
				if ((message.getMediaUrl() != null) && (!message.getMediaUrl().isEmpty())) // make sure media was uploaded
					mXMPPProvider.sendGroupStringMessage(message.toJson(), participantIds, message.getConversationId());
			}
			else
				mXMPPProvider.sendGroupStringMessage(message.toJson(), participantIds, message.getConversationId());
		}
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

	public void sendTypingSignalForGroup(String conversationId, boolean isTyping, List<GroupMember> groupMembers, String selfId) {
		Message msg = new Message(null, mSelfUserId, conversationId, "", "EN");
		msg.setMessageType(Message.MSG_TYPE_TYPING_SIGNAL);
		msg.setIsTyping(isTyping);

		List<String> participantIds = new ArrayList<>();
		for (GroupMember groupMember : groupMembers){
			if(!groupMember.getUserId().equals(selfId))
				participantIds.add(groupMember.getUserId());
		}
		mXMPPProvider.sendGroupStringMessage(msg.toJson(), participantIds, conversationId);
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


	private void broadcastTypingSignal(String conversationId, String senderId, boolean isTyping) {
		Intent intent = new Intent();
		intent.setAction(TYPING_SIGNAL_ACTION);
		intent.putExtra(CONVERSATION_ID_EXTRA, conversationId);
		intent.putExtra(PARTICIPANT_ID_EXTRA, senderId);
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

//	private void broadcastWebRTC(boolean rejectedFlag) {
//		Intent intent = new Intent();
//		if(rejectedFlag)
//			intent.setAction(Message.RTC_CODE_REJECTED);
//		else
//			intent.setAction(Message.RTC_CODE_BUSY);
//		sendBroadcast(intent);
//	}

	private void broadcastWebRTC(String mRtcCode, String message, String messageLanguage) {
		Intent intent = new Intent();
			intent.setAction(mRtcCode);
//		if(mRtcCode.equals(Message.RTC_CODE_UPDATE_SESSION))
//						intent.putExtra(IS_RECORDING, isRecording);
		if(mRtcCode.equals(Message.RTC_CODE_TEXT)) {
						intent.putExtra(RTC_MESSAGE, message);
		}				intent.putExtra(RTC_MESSAGE_LANGUAGE, messageLanguage);

		sendBroadcast(intent);
	}

	private void broadcastCallEvent(Message message) {

		if(message.getEventCode().equals(Message.EVENT_CODE_MISSED_VIDEO_CALL))
				isVideo = true;
		else
				isVideo = false;

		//if call activity open - close;
		if(CallActivity.activityActiveFlag) {
				Intent intent = new Intent();
				intent.setAction(CALL_EVENT);
				sendBroadcast(intent);
		}

        //Update resent call;
		 contact = mRepository.getParticipantContact(message.getSenderId());
		 call = new Call(message.getSenderId(), contact.getName(), contact.getAvatar(), contact.getLanguage(),
								isVideo, CallActivity.CALL_MISSED, message.getTimestampMilli(),0);
		 mRepository.addNewCall(call);

		//Open missed notification in any case
		NotificationHelper.showMissedCallNotification(getApplication(), message, contact, isVideo);

		//Show notification about
		Message message_temp = Message.CreateMissedCall(mSelfUserId, message.getSenderId(), message.getConversationId(), isVideo);
		mRepository.addNewOutgoingMessageInIOThread(message_temp);
	}


	private void openIncomingCallActivity(Message message) {
		if(CallActivity.activityActiveFlag && message.getSenderId().equals(CallActivity.mParticipantId) )
			return;
			//If incoming activity open send BUSY back to sender
		else if(CallActivity.activityActiveFlag) {
				Message message_busy = new Message(message.getSenderId(), mSelfUserId, message.getConversationId(),
							message.getSessionID(), "", "",
						    Message.RTC_CODE_BUSY, message.getIsVideoRTC());
				sendMessage(message_busy);
		}else {
			contact = mRepository.getParticipantContact(message.getSenderId());
			Intent intent = new Intent(this, CallActivity.class);
				intent.putExtra(Consts.INTENT_PARTICIPANT_ID, message.getSenderId());
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, contact.getName());
				intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, contact.getLanguage());
				intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, contact.getAvatar());
				intent.putExtra(Consts.INTENT_SESSION_ID, message.getSessionID());
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, message.getId());
				intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
				intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
				intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, message.getIsVideoRTC());
		     	intent.putExtra(Consts.OUTGOING_CALL_FLAG, false);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				this.startActivity(intent);
		}
	}
}
