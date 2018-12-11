package io.wochat.app.db;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WCSharedPreferences {


	private static WCSharedPreferences mInstance;
	public static WCSharedPreferences getInstance(Context context) {
		if (mInstance == null) {
			synchronized (WCSharedPreferences.class) {
				if (mInstance == null) {
					mInstance = new WCSharedPreferences(context);
				}
			}
		}
		return mInstance;
	}



	public WCSharedPreferences(Context context) {
		mSharedPreferences = context.getSharedPreferences("GLOBAL", Context.MODE_PRIVATE);
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

	public String getToken(){
		return mSharedPreferences.getString("TOKEN", null);
	}

	public void saveUserCountryCode(String userCountryCode) {
		mSharedPreferences.edit().putString("USER_COUNTRY_CODE", userCountryCode).commit();
	}

	public String getUserCountryCode(){
		return mSharedPreferences.getString("USER_COUNTRY_CODE", null);
	}


	public void saveUserRegistrationData(String token, String refreshToken, String xmppPwd) {
		mSharedPreferences.edit().
			putString("TOKEN", token).
			putString("REFRESH_TOKEN", refreshToken).
			putString("XMPP_PWD", xmppPwd).
			commit();
	}

	public boolean hasUserRegistrationData(){
		return mSharedPreferences.contains("TOKEN");
	}

}
