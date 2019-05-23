package io.wochat.app.logic;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.BidiFormatter;
import android.util.Log;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.wochat.app.R;
import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.ContactServer;
import io.wochat.app.db.entity.Message;
import io.wochat.app.model.NotificationData;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.MainActivity;
import io.wochat.app.ui.Messages.ConversationActivity;
import io.wochat.app.ui.SplashActivity;

import static android.support.v4.text.TextDirectionHeuristicsCompat.LTR;

public class NotificationHelper {

	private static final String CHANNEL_ID = "woch";
	private static final String TAG = "NotificationHelper";
	private static final int NOTIFICATION_BUNDLED_BASE_ID = 1000;
	private static final int NOTIFICATION_ID = 2000;


	public static void handleNotificationFromPush(Application application, Map<String, String> dataMap){
		if (dataMap == null)
			return;

		String notifString = dataMap.get("notification_body");
		String senderString = dataMap.get("sender_contact");

		Message message = Message.fromJson(notifString);
		ContactServer contact = ContactServer.fromJson(senderString);
		WCRepository repo = ((WCApplication) application).getRepository();
		if (message.isGroupEvent()){

			if(repo.getSelfUserId().equals(message.getActingUser())) // do not pop notification for self operations
				return;

			repo.getNotificationDataForGroupEvent(message, contact, data -> {
				if (data != null)
					showNotification(application.getApplicationContext(), data);
			});
		}
		else {
			repo.getNotificationData(message, contact, data -> {
				if (data != null)
					showNotification(application.getApplicationContext(), data);
			});
		}
	}

	public static void handleNotificationIncomingMessage(Application application, Message message, Contact contact){
		WCRepository repo = ((WCApplication) application).getRepository();
		repo.getNotificationData(message, contact.getContactServer(), data -> {
			if (data != null)
				showNotification(application.getApplicationContext(), data);
		});
	}

	private static void showNotification(Context context, NotificationData data){

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



		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		String title;
		if(data.conversation.isGroup()){
			title = data.title + "@" + data.conversation.getGroupName();
			title = BidiFormatter.getInstance().unicodeWrap(title, LTR, true);
		}
		else {
			title = data.title;
		}


		/****************************************************************************************************/
		//String cid = createNotificationChannel1(context);
		NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, getChanggelId())
//			.setContentTitle("Group Summary")
//			.setContentText("This is the group summary")
			.setContentTitle(title)
			.setContentText(data.body)
			.setSmallIcon(R.drawable.ic_notif)
			.setGroupSummary(true)
			.setGroup(data.conversationId)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			//.setDefaults(Notification.DEFAULT_ALL)
			.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
			.setContentIntent(pendingIntent)
			.setFullScreenIntent(pendingIntent, true);
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
			.setContentTitle(title)
			.setContentText(data.body)
			.setTicker(title)
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
			//.setFullScreenIntent(pendingIntent, true)
			.setContentIntent(pendingIntent);


		if (data.largeIcon != null)
			builder.setLargeIcon(data.largeIcon);

		Notification notification = builder.build();

		//notificationManager.notify(data.messageId, NOTIFICATION_ID, notification);
		notificationManager.notify(data.conversationId, NOTIFICATION_ID, notification);

		//generateMessagingStyleNotification(context);
		/****************************************************************************************************/





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





















}
