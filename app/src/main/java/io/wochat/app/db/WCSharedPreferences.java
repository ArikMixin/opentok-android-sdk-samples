package io.wochat.app.db;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import io.wochat.app.utils.Utils;

public class WCSharedPreferences {

	private static final String SMS_CODE = "SMS_CODE";
	private static final String USER_ID = "USER_ID";
	private static final String USER_LANG = "USER_LANG";
	private static final String USER_COUNTRY_CODE = "USER_COUNTRY_CODE";
	private static final String USER_PHONE_NUM = "USER_PHONE_NUM";
	private static final String TOKEN = "TOKEN";
	private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
	private static final String XMPP_PWD = "XMPP_PWD";
	private static final String GLOBAL_SP = "GLOBAL";
	private static final String USER_PROFILE_PIC_URL = "USER_PROFILE_PIC_URL";
	private static final String USER_PROFILE_THUMB_URL = "USER_PROFILE_THUMB_URL";
	private static final String USER_PROFILE_PIC = "USER_PROFILE_PIC";


	private static WCSharedPreferences mInstance;
	private String mUserId;
	private String mUserLang;

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
		mSharedPreferences = context.getSharedPreferences(GLOBAL_SP, Context.MODE_PRIVATE);
	}

	private final SharedPreferences mSharedPreferences;

	public void saveSMSCode(String code){
		mSharedPreferences.edit().putString(SMS_CODE, code).commit();
	}

	public String getSMSCode(){
		return mSharedPreferences.getString(SMS_CODE, null);
	}

	public void saveUserId(String userId) {
		mUserId = userId;
		mSharedPreferences.edit().putString(USER_ID, userId).commit();
	}

	public void saveUserLanguage(String language) {
		if (language.equals("IW"))
			mUserLang = "HE";
		else
			mUserLang = language;

		mSharedPreferences.edit().putString(USER_LANG, language).commit();
	}

	public String getUserLang(){
		if (mUserLang == null) // cashing
			mUserLang = mSharedPreferences.getString(USER_LANG, "EN");
		if (mUserLang.equals("IW"))
			mUserLang = "HE";
		return mUserLang;
	}

	public String getUserId(){
		if (mUserId == null) // cashing
			mUserId = mSharedPreferences.getString(USER_ID, null);

		return mUserId;
	}

	public String getToken(){
		return mSharedPreferences.getString(TOKEN, null);
	}

	public void saveUserPhoneNumAndCountryCode(String phoneNum, String userCountryCode) {
		mSharedPreferences.edit().
			putString(USER_COUNTRY_CODE, userCountryCode).
			putString(USER_PHONE_NUM, phoneNum).
			commit();
	}

	public String getUserCountryCode(){
		return mSharedPreferences.getString(USER_COUNTRY_CODE, null);
	}

	public String getUserPhoneNum(){
		return mSharedPreferences.getString(USER_PHONE_NUM, null);
	}

	public String getXMPPPassword(){
		return mSharedPreferences.getString(XMPP_PWD, null);
	}

	public void saveUserRegistrationData(String token, String refreshToken, String xmppPwd) {
		mSharedPreferences.edit().
			putString(TOKEN, token).
			putString(REFRESH_TOKEN, refreshToken).
			putString(XMPP_PWD, xmppPwd).
			commit();
	}

	public boolean hasUserRegistrationData(){
		return mSharedPreferences.contains(TOKEN);
	}

	public void saveUserProfileImagesUrls(String imageUrl, String thumbUrl) {
		mSharedPreferences.edit().
			putString(USER_PROFILE_PIC_URL, imageUrl).
			putString(USER_PROFILE_THUMB_URL, thumbUrl).
			commit();
	}


	public void saveUserProfileImages(byte[] bytes) {
		String encodedString = Utils.byteArrayImageToString(bytes);
		mSharedPreferences.edit().
			putString(USER_PROFILE_PIC, encodedString).
			commit();
	}

	public byte[] getUserProfileImages() {
		String encodedString = mSharedPreferences.getString(USER_PROFILE_PIC, null);
		return Utils.stringToByteArrayImage(encodedString);
	}

}
