package io.wochat.app.logic;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
		repo.getNotificationData(message, contact, data -> {
			if (data != null)
				showNotification(application.getApplicationContext(), data);
		});
	}

	public static void handleNotificationIncomingMessage(Application application, Message message, Contact contact){
		WCRepository repo = ((WCApplication) application).getRepository();
		repo.getNotificationData(message, contact.getContactServer(), data -> {
			if (data != null)
				showNotification(application.getApplicationContext(), data);
		});
	}

	private static void showNotification(Context context, NotificationData data){

		createNotificationChannel(context);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

		//********************************************************************
		//https://developer.android.com/training/notify-user/navigation
		//********************************************************************
		
		Intent intent = new Intent(context, ConversationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, data.conversationId);

		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, data.contact.getId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, data.contact.getDisplayName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, data.contact.getLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, data.contact.getAvatar());
		intent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, data.contact.toJson());

		intent.putExtra(Consts.INTENT_SELF_ID, data.selfUser.getUserId());
		intent.putExtra(Consts.INTENT_SELF_LANG, data.selfUser.getLanguage());
		intent.putExtra(Consts.INTENT_SELF_NAME, data.selfUser.getUserName());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, data.selfUser.getProfilePicUrl());

		intent.putExtra(Consts.INTENT_CLICK_FROM_NOTIFICATION, true);



		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// prior Nougat



		/****************************************************************************************************/

		NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
//			.setContentTitle("Group Summary")
//			.setContentText("This is the group summary")
			.setContentTitle(data.title)
			.setContentText(data.body)
			.setSmallIcon(R.drawable.ic_notif)
			.setGroupSummary(true)
			.setGroup(data.conversationId)
			.setContentIntent(pendingIntent);
		if (data.largeIcon != null)
			summaryBuilder.setLargeIcon(data.largeIcon);

		notificationManager.notify(data.conversationId, NOTIFICATION_BUNDLED_BASE_ID, summaryBuilder.build());

		/****************************************************************************************************/
//		NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
//		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//		inboxStyle.setBigContentTitle(data.title);
//		inboxStyle.addLine("a b c d e");
//		inboxStyle.addLine("gfsdg sfdg dsfg dfsg sdfg fds ");
//		inboxStyle.addLine("hi there how the fucj r u ");
//		summaryBuilder.setStyle(inboxStyle);
//		notificationManager.notify(data.conversationId, NOTIFICATION_BUNDLED_BASE_ID, summaryBuilder.build());


		/****************************************************************************************************/
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
			.setContentTitle(data.title)
			.setContentText(data.body)
			.setSmallIcon(R.drawable.ic_notif)
			.setStyle(new NotificationCompat.BigTextStyle().bigText(data.body))
			.setGroup(data.conversationId)
			.setContentIntent(pendingIntent);

		if (data.largeIcon != null)
			builder.setLargeIcon(data.largeIcon);

		notificationManager.notify(data.messageId, NOTIFICATION_ID, builder.build());
		/****************************************************************************************************/





//		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//			.setSmallIcon(R.drawable.ic_notif)
//			.setContentTitle(data.title)
//			.setContentText(data.body)
//			.setLargeIcon(data.largeIcon)
//			.setGroup(data.conversationId)
//			.setGroupSummary(true)
//			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
//			.setContentIntent(pendingIntent)
//			.setAutoCancel(true);


	}

	private static void createNotificationChannel(Context context) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "wochat channel";
			String description = "wochat channel desc";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

	public static void deleteNotification(Context context,  Message message) {
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.cancel(message.getId(), NOTIFICATION_ID);
		notificationManager.cancel(message.getConversationId(), NOTIFICATION_BUNDLED_BASE_ID);
	}
}
