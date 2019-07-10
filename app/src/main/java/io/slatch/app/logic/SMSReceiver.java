package io.slatch.app.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = "SMSReceiver";

	public SMSReceiver(@NonNull OnSmsReceivedListener listener){
		this.mOnSmsReceivedListener = listener;
	}
	private OnSmsReceivedListener mOnSmsReceivedListener;
	public interface OnSmsReceivedListener {
		void onSmsReceived(String code);
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		final Bundle bundle = intent.getExtras();
		try {
			if (bundle != null) {
				Object[] pdusObj = (Object[]) bundle.get("pdus");
				if (pdusObj != null) {
					for (Object aPdusObj : pdusObj) {
						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
						String senderAddress = currentMessage.getDisplayOriginatingAddress();
						String message = currentMessage.getDisplayMessageBody();

						Log.e(TAG, "SMS: " + message + ", Sender: " + senderAddress);

						// TODO: make sure the SMS is from the intended the sender

						// get the verification code from the SMS
						String verificationCode = getVerificationCode(message);
						mOnSmsReceivedListener.onSmsReceived(verificationCode);
						if (verificationCode != null) {
							Log.v(TAG, "Verification Code: " + verificationCode);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private String getVerificationCode(String message) {
		if (!message.contains("Wochat"))
			return null;

		Pattern pattern = Pattern.compile("([\\d]{6})");
		Matcher m = pattern.matcher(message);
		if(m.find()){
			return m.group(1);
		}
		return null;
	}

}
