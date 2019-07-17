package io.slatch.app.logic;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.BidiFormatter;
import android.util.Log;

import java.util.Map;

import io.slatch.app.R;
import io.slatch.app.WCApplication;
import io.slatch.app.WCRepository;
import io.slatch.app.db.WCSharedPreferences;
import io.slatch.app.db.entity.Contact;
import io.slatch.app.db.entity.ContactServer;
import io.slatch.app.db.entity.Message;
import io.slatch.app.model.NotificationData;
import io.slatch.app.ui.AudioVideoCall.CallActivity;
import io.slatch.app.ui.Consts;
import io.slatch.app.ui.MainActivity;
import io.slatch.app.ui.Messages.ConversationActivity;
import io.slatch.app.utils.Utils;

import static android.support.v4.text.TextDirectionHeuristicsCompat.LTR;

public class NotificationHelper {

	private static final String CHANNEL_ID = "woch";
	private static final String TAG = "NotificationHelper";
	private static final int NOTIFICATION_BUNDLED_BASE_ID = 1000;
	private static final int NOTIFICATION_ID = 2000;

	public static void handleNotificationFromPush(Application application, Map<String, String> dataMap){
		Log.d("arik3", "pushhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh: ");
		Log.d(TAG, "handleNotificationFromPush: ");
		if (dataMap == null)
			return;

		String notifString = dataMap.get("notification_body");
		String senderString = dataMap.get("sender_contact");

		Message message = Message.fromJson(notifString);
		Log.d("arik3", "message:" + message.getText() + " ,type: " + message.getMessageType() + " ,sander: " + message.getSenderId() + " , " + message.getSenderName());
		ContactServer contact = ContactServer.fromJson(senderString);
		WCRepository repo = ((WCApplication) application).getRepository();

		if(message.getRtcCode() != null && message.getRtcCode().equals(Message.RTC_CODE_OFFER)) {
			//Not need to fire notification in call - only open call activity
			openIncomingCall(application, repo, message);
			return;
		}

		if(message.getMessageType().equals(Message.MSG_TYPE_TEXT) &&
				!Utils.fixHebrew(message.getMessageLanguage()).equals
				(Utils.fixHebrew(WCSharedPreferences.getInstance(application).getUserLang()))){
						repo.translate(message.getText(), message.getMessageLanguage(), listener -> {
						message.setMessageText(listener);
						getContactFromPush(repo, application, message, contact);
				});
		}else{
						getContactFromPush(repo, application, message, contact);
		}

	}

	public static void handleNotificationIncomingMessage(Application application, Message message, Contact contact){

		WCRepository repo = ((WCApplication) application).getRepository();
		Log.d(TAG, "handleNotificationIncomingMessage: " + message.getText());

		//If this is a text massage (and also different languages) - translate first, and only then -> show notification
		if(message.getMessageType().equals(Message.MSG_TYPE_TEXT) &&
				!Utils.fixHebrew(message.getMessageLanguage()).equals
				(Utils.fixHebrew(WCSharedPreferences.getInstance(application).getUserLang()))){
					repo.translate(message.getText(), message.getMessageLanguage(), listener -> {
					message.setMessageText(listener);
					getContactIncomingMessage(repo, application , message , contact);
			});
		}else{
					getContactIncomingMessage(repo, application , message , contact);
		}
	}

	private static void getContactIncomingMessage(WCRepository repo, Application application, Message message, Contact contact){
		ContactServer contactServer = contact != null ? contact.getContactServer() : null;
		repo.getNotificationData(message, contactServer, data -> {
			if (data != null)
				showNotification(application.getApplicationContext(), data, false);
		});
	}

	private static void getContactFromPush(WCRepository repo, Application application, Message message, ContactServer contact){
		Log.d("arik3", "$$$$: ");

		if (message.isGroupEvent()){

			if(repo.getSelfUserId().equals(message.getActingUser())) // do not pop notification for self operations
				return;

			repo.getNotificationDataForGroupEvent(message, contact, data -> {
				if (data != null)
					showNotification(application.getApplicationContext(), data, true);
			});
		}
		else {
			repo.getNotificationData(message, contact, data -> {
				if (data != null)
					showNotification(application.getApplicationContext(), data, true);
			});
		}
	}

	private static void showNotification(Context context, NotificationData data, boolean isPush){
		Log.d("arik3", "fire push ");

		Log.e(TAG, "showNotification: " + data.body + ", from push: " + isPush);

		//createNotificationChannel(context);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

		//********************************************************************
		//https://developer.android.com/training/notify-user/navigation
		//********************************************************************
		
		Intent intent = new Intent(context, ConversationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, data.conversationId);

		if(data.contact != null) { // not group event
			intent.putExtra(Consts.INTENT_PARTICIPANT_ID, data.contact.getId());
			intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, data.contact.getDisplayName());
			intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, data.contact.getLanguage());
			intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, data.contact.getAvatar());
			intent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, data.contact.toJson());
		}

		intent.putExtra(Consts.INTENT_SELF_ID, data.selfUser.getUserId());
		intent.putExtra(Consts.INTENT_SELF_LANG, data.selfUser.getLanguage());
		intent.putExtra(Consts.INTENT_SELF_NAME, data.selfUser.getUserName());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, data.selfUser.getProfilePicUrl());

		intent.putExtra(Consts.INTENT_CLICK_FROM_NOTIFICATION, true);
		intent.putExtra(Consts.INTENT_IS_FROM_PUSH_NOTIFICATION, isPush);



		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		String title;
		if(data.conversation.isGroup()){
			if (Utils.isNotNullAndNotEmpty(data.title)) {
				title = data.title + "@" + data.conversation.getGroupName();
				title = BidiFormatter.getInstance().unicodeWrap(title, LTR, true);
			}
			else
				title = null;
		}
		else {
			title = data.title;
		}


		/****************************************************************************************************/
		//String cid = createNotificationChannel1(context);
		NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, getChanggelId())
//			.setContentTitle("Group Summary")
//			.setContentText("This is the group summary")
			.setContentText(data.body)
			.setSmallIcon(R.drawable.ic_notif)
			.setGroupSummary(true)
			.setGroup(data.conversationId)
			.setAutoCancel(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			//.setDefaults(Notification.DEFAULT_ALL)
			.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
			.setContentIntent(pendingIntent)
			.setFullScreenIntent(pendingIntent, true);
		if (title != null)
			summaryBuilder.setContentTitle(title);
		if (data.largeIcon != null)
			summaryBuilder.setLargeIcon(data.largeIcon);

		//notificationManager.notify(data.conversationId, NOTIFICATION_BUNDLED_BASE_ID, summaryBuilder.build());

		/****************************************************************************************************/
//		NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
//		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//		inboxStyle.setBigContentTitle(data.title);
//		inboxStyle.addLine("a b c d e");
//		inboxStyle.addLine("gfsdg sfdg dsfg dfsg sdfg fds ");
//		inboxStyle.addLine("hi there how the fucj r u ");
//		summaryBuilder.setStyle(inboxStyle);
//		notificationManager.notify(data.conversationId, NOTIFICATION_BUNDLED_BASE_ID, summaryBuilder.build());


		/******************************************************************************************************/



		/****************************************************************************************************/
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChanggelId());
		GlobalNotificationBuilder.setNotificationCompatBuilderInstance(builder);

		builder
			.setStyle(new NotificationCompat.BigTextStyle().bigText(data.body))
			.setContentText(data.body)
			.setSmallIcon(R.drawable.ic_notif)
			.setDefaults(NotificationCompat.DEFAULT_ALL)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			//.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
			.setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.colorPrimary))
			//.setSubText("3")
			.setCategory(Notification.CATEGORY_MESSAGE)
			.setVisibility(Notification.VISIBILITY_PUBLIC)
			.setGroup(data.conversationId)
			.addPerson(data.contactName)
			.setAutoCancel(true)
			//.setFullScreenIntent(pendingIntent, true)
			.setContentIntent(pendingIntent);

		if (title != null) {
			builder.setContentTitle(title);
			builder.setTicker(title);
		}


		if (data.largeIcon != null)
			builder.setLargeIcon(data.largeIcon);

		Notification notification = builder.build();

		//notificationManager.notify(data.messageId, NOTIFICATION_ID, notification);
		notificationManager.notify(data.conversationId, NOTIFICATION_ID, notification);

		//generateMessagingStyleNotification(context);
		/****************************************************************************************************/

	}

	public static void showMissedCallNotification(Application application, Message message, Contact contact, boolean isVideo) {
		String callType;
		if (isVideo)
			callType = "Video";
		else
			callType = "Audio";

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(application.getApplicationContext());
		Intent intent = new Intent(application.getApplicationContext(), CallActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, message.getSenderId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, contact.getName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, contact.getLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, contact.getAvatar());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, contact.getId());
		intent.putExtra(Consts.INTENT_SELF_ID, WCSharedPreferences.getInstance(application.getApplicationContext()).getUserId());
		intent.putExtra(Consts.INTENT_SELF_LANG, WCSharedPreferences.getInstance(application.getApplicationContext()).getUserLang());
		intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, isVideo);
		intent.putExtra(Consts.OUTGOING_CALL_FLAG, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // FLAG_ACTIVITY_NEW_TASK
		intent.addCategory(Intent.CATEGORY_HOME);

		/****************************************************************************************************/
		NotificationCompat.Builder builder = new NotificationCompat.Builder(application.getApplicationContext(), getChanggelId());
		GlobalNotificationBuilder.setNotificationCompatBuilderInstance(builder);
		builder
				.setContentTitle("Missed " + callType + " Call" )
				.setContentText("from " + contact.getName())
				.setSmallIcon(R.drawable.ic_notif)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL)
				.setColor(ContextCompat.getColor(application.getApplicationContext().getApplicationContext(), R.color.colorPrimary))
				.setCategory(Notification.CATEGORY_MESSAGE)
				.setVisibility(Notification.VISIBILITY_PUBLIC)
				.setContentIntent(PendingIntent.getActivity(application.getApplicationContext(), 0, intent, 0));
		Notification notification = builder.build();
		notificationManager.notify( message.getId(),NOTIFICATION_ID, notification);
	}
/*
	private static String createNotificationChannel(Context context) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "wochat notification channel";
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription("wochat notification preferences");
			channel.enableLights(true);
			channel.setLightColor(Color.BLUE);
			channel.setShowBadge(false);
			channel.enableVibration(true);
			channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this

			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			//NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
			return CHANNEL_ID;
		}
		else
			return null;
	}*/

	public static void deleteNotification(Context context,  Message message) {
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//		notificationManager.cancel(message.getId(), NOTIFICATION_ID);
//		notificationManager.cancel(message.getConversationId(), NOTIFICATION_BUNDLED_BASE_ID);
		notificationManager.cancel(message.getConversationId(), NOTIFICATION_ID);
	}







	/*^%*&^%&^%*&^%(&*%(&^%(&*^*)(^&(*&%(&*^%(*&^%^%&%^&%(*&^%*&(^%*&^%^&%&*(%&*^%*&^%(*&%(&*^%*%*(*/

	private static void generateMessagingStyleNotification(Context context) {

		String notificationChannelId = createNotificationChannel1(context);


		Intent notifyIntent = new Intent(context, MainActivity.class);



		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(notifyIntent);
		// Gets a PendingIntent containing the entire back stack
		PendingIntent mainPendingIntent =
			PendingIntent.getActivity(
				context,
				0,
				notifyIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
			);






		NotificationCompat.Builder notificationCompatBuilder =
			new NotificationCompat.Builder(context.getApplicationContext(), notificationChannelId);

		GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

		notificationCompatBuilder
			.setStyle(new NotificationCompat.BigTextStyle().bigText("fdsasdf sdaf sdaf dsaf sdf sda fsda f "))
			.setContentTitle("contentTitle")
			.setContentText("getContentText")
			.setSmallIcon(R.drawable.ic_action_call)
			.setLargeIcon(BitmapFactory.decodeResource(
				context.getResources(),
				R.drawable.ic_action_add_group))
			.setContentIntent(mainPendingIntent)
			.setDefaults(NotificationCompat.DEFAULT_ALL)
			.setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.colorPrimary))
			.setSubText("3")
			.setCategory(Notification.CATEGORY_MESSAGE)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setVisibility(Notification.VISIBILITY_PUBLIC);
			notificationCompatBuilder.addPerson("moshe");

		Notification notification = notificationCompatBuilder.build();
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	public static String getChanggelId(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			return CHANNEL_ID;
		else
			return null;
	}


	public static String createNotificationChannel1(Context context) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			//String channelId = "chid";
			CharSequence channelName = "Notifications";
			String channelDescription = "Notification Preferences";
			int channelImportance = NotificationManager.IMPORTANCE_HIGH;
			boolean channelEnableVibrate = true;
			int channelLockscreenVisibility = Notification.VISIBILITY_PUBLIC;

			NotificationChannel notificationChannel =
				new NotificationChannel(CHANNEL_ID, channelName, channelImportance);

			notificationChannel.setDescription(channelDescription);
			notificationChannel.enableVibration(channelEnableVibrate);
			notificationChannel.setLockscreenVisibility(channelLockscreenVisibility);

			NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(notificationChannel);

			return CHANNEL_ID;
		} else {
			// Returns null for pre-O (26) devices.
			return null;
		}
	}

	public static void  openIncomingCall(Application application, WCRepository repo, Message message){
		Log.d("NotificationHelper", "!!!!!!!!!!!!!!!!!!!!!!");
		//If incoming activity open send BUSY back to sender
		if(CallActivity.activityActiveFlag && message.getSenderId().equals(CallActivity.mParticipantId))
			return;
//		else if(CallActivity.activityActiveFlag) {
////			Message message_busy = new Message(message.getSenderId(),
////					WCSharedPreferences.getInstance(application.getApplicationContext()).getUserId(),
////					message.getConversationId(),
////					message.getSessionID(), "", "",
////					Message.RTC_CODE_BUSY, message.getIsVideoRTC(), false);
////			sendMessage(message_busy);
//		}
			Contact contact = repo.getParticipantContact(message.getSenderId());
			Intent intent = new Intent(application, CallActivity.class);
			intent.putExtra(Consts.INTENT_PARTICIPANT_ID, message.getSenderId());
			intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, contact.getName());
			intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, contact.getLanguage());
			intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, contact.getAvatar());
			intent.putExtra(Consts.INTENT_SESSION_ID, message.getSessionID());
			intent.putExtra(Consts.INTENT_CONVERSATION_ID, message.getId());
			intent.putExtra(Consts.INTENT_SELF_ID,  WCSharedPreferences.getInstance(application.getApplicationContext()).getUserId());
			intent.putExtra(Consts.INTENT_SELF_LANG, WCSharedPreferences.getInstance(application.getApplicationContext()).getUserLang());
			intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, message.getIsVideoRTC());
			intent.putExtra(Consts.OUTGOING_CALL_FLAG, false);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			application.startActivity(intent);
	}

}
