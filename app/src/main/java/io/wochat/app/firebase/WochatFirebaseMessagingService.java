package io.wochat.app.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.wochat.app.logic.NotificationHelper;

public class WochatFirebaseMessagingService extends FirebaseMessagingService {

	private static final String TAG = "MessagingService";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// ...

		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: " + remoteMessage.getFrom());







		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());




			NotificationHelper.handleNotificationFromPush(getApplication(), remoteMessage.getData());

			if (/* Check if data needs to be processed by long running job */ true) {
				// For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
				//scheduleJob();
			} else {
				// Handle message within 10 seconds
				//handleNow();
			}

		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
			Log.d(TAG, "Message Notification title: " + remoteMessage.getNotification().getTitle());
			Log.d(TAG, "Message Notification icon: " + remoteMessage.getNotification().getIcon());
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}

	@Override
	public void onNewToken(String s) {
		super.onNewToken(s);
		Log.e("NEW_TOKEN","token: " + s);
	}


}
