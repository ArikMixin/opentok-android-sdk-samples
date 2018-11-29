package io.wochat.app.db;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WCSharedPreferences {


	private static WCSharedPreferences mInstance;
	public static WCSharedPreferences getInstance(Application application) {
		if (mInstance == null) {
			synchronized (WCSharedPreferences.class) {
				if (mInstance == null) {
					mInstance = new WCSharedPreferences(application);
				}
			}
		}
		return mInstance;
	}



	public WCSharedPreferences(Application application) {
		mSharedPreferences = application.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
	}

	private final SharedPreferences mSharedPreferences;

	public void saveSMSCode(String code){
		mSharedPreferences.edit().putString("SMS_CODE", code).commit();
	}

	public String getSMSCode(){
		return mSharedPreferences.getString("SMS_CODE", null);
	}

	public void saveUserId(String userId) {
		mSharedPreferences.edit().putString("USER_ID", userId).commit();
	}

	public String getUserId(){
		return mSharedPreferences.getString("USER_ID", null);
	}
}
