package io.slatch.app.com;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.slatch.app.BuildConfig;
import io.slatch.app.utils.Utils;

public class WochatApi {

	private static final String BASE_URL = "https://api-dev.slatch.io/";
	//private static final String BASE_URL = "https://api.slatch.io/";

	private static final String TAG = "WochatApi";
	private final Context mContext;
	String mUserId = null;
	String mToken = null;



	public interface OnServerResponseListener{
		void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response);
	}

	public interface OnServerResponseArrayListener{
		void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONArray response);
	}

	private static WochatApi mInstance;

	public static WochatApi getInstance(Context context, String userId, String token) {
		if (mInstance == null) {
			synchronized (WochatApi.class) {
				if (mInstance == null) {
					mInstance = new WochatApi(context, userId, token);
				}
				else {
					mInstance.setUserId(userId);
					mInstance.setToken(token);
				}
			}
		}
		return mInstance;
	}

	private WochatApi(Context context, String userId, String token){
		super();
		mContext = context.getApplicationContext();
		mUserId = userId;
		mToken = token;

		VolleyLog.DEBUG = BuildConfig.DEBUG;

	}

	public void setUserId(String userId) {
		mUserId = userId;
	}

	public void setToken(String token) {
		mToken = token;
	}



	public void userRegistration(String countryCode, String phoneNumber, final OnServerResponseListener lsnr){

		Log.e(TAG, "API userRegistration - countryCode: " + countryCode + ", phoneNumber: " + phoneNumber);

		String token = FirebaseInstanceId.getInstance().getToken();
		String language = Locale.getDefault().getLanguage().toUpperCase(); // EN
		String languageLocale = Locale.getDefault().toString(); // en_US

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("phone_number", phoneNumber);
			jsonObject.put("apns_push_token", "");
			jsonObject.put("push_token", token);
			jsonObject.put("country_code", countryCode);
			jsonObject.put("language", language);
			jsonObject.put("language_locale", languageLocale);
			jsonObject.put("user_agent", "android");
			jsonObject.put("os", "android");
			jsonObject.put("app_version", BuildConfig.VERSION_NAME);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/register/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}
	//TokBox session id
	public void getCallSessionId(String sessionType,  final OnServerResponseListener lsnr){

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("session_type", sessionType);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "calls/sessions/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}

	public void getToken(String sessionId, String tokenRoleType,  final OnServerResponseListener lsnr){

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("session_id", sessionId);
			jsonObject.put("token_role_type",tokenRoleType);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "calls/tokens/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}

	public void getLatestVersion(OnServerResponseListener lsnr){
			String url = BASE_URL + "version/android/";
			sendRequestAndHandleResult(Request.Method.GET, url, null, lsnr);
	}

	public void userVerification(String userId, String code, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API userVerification - userId: " + userId + ", code: " + code);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("user_id", userId);
			jsonObject.put("verification_code", code);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/validate_code/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}



	public void userConfirmRegistration(String userName, String profilePicURL, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API userConfirmRegistration - userName: " + userName + ", profilePicURL: " + profilePicURL);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("user_name", userName);

			if (profilePicURL != null)
				jsonObject.put("profile_pic_url", profilePicURL);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/confirm_registration/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);


	}

	public void translate(String messageId, String fromLanguage , String toLanguage , String text, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API translate fromLanguage: " + fromLanguage + ", toLanguage: " + toLanguage + " , text: " + text + " , messageId: " + messageId);


		String url = BASE_URL + "/translate/";

		HashMap<String, String> params = new HashMap<>();
		params.put("id", messageId);
		params.put("from_language", fromLanguage);
		params.put("to_language", toLanguage);
		params.put("text", Uri.encode(text));

		sendGetAndHandleResult(url, params, lsnr);
	}

	public void translate2(String messageId1, String fromLanguage1 , String toLanguage1 , String text1,
						   String messageId2, String fromLanguage2 , String toLanguage2 , String text2,
						   final OnServerResponseListener lsnr) {

		Log.e(TAG, "API translate2 fromLanguage1: " + fromLanguage1 + ", toLanguage1: " + toLanguage1 + " , text1: " + text1 + " , messageId1: " + messageId1);
		Log.e(TAG, "API translate2 fromLanguage2: " + fromLanguage2 + ", toLanguage2: " + toLanguage2 + " , text2: " + text2 + " , messageId2: " + messageId2);


		String url = BASE_URL + "/translate/";

		HashMap<String, String> params1 = new HashMap<>();
		params1.put("id", messageId1);
		params1.put("from_language", fromLanguage1);
		params1.put("to_language", toLanguage1);
		params1.put("text", Uri.encode(text1));

		sendGetAndHandleResult(url, params1, (isSuccess1, errorLogic1, errorComm1, response1) -> {
			if (!isSuccess1){
				lsnr.OnServerResponse(isSuccess1, errorLogic1, errorComm1, response1);
				return;
			}

			HashMap<String, String> params2 = new HashMap<>();
			params2.put("id", messageId2);
			params2.put("from_language", fromLanguage2);
			params2.put("to_language", toLanguage2);
			params2.put("text", Uri.encode(text2));

			sendGetAndHandleResult(url, params2, (isSuccess2, errorLogic2, errorComm2, response2) -> {
				if (!isSuccess2) {
					lsnr.OnServerResponse(isSuccess2, errorLogic2, errorComm2, response2);
					return;
				}
				try {
					response1.put("message2", response2.getString("message"));
					lsnr.OnServerResponse(true, errorLogic2, errorComm2, response1);
				} catch (JSONException e) {
					e.printStackTrace();
					lsnr.OnServerResponse(false, e.getMessage(), errorComm2, response2);
				}

			});

		});
	}


	public void userGetContacts(String[] contactIdArray, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API userGetContacts - contactIdList: " + Utils.LogArray(Log.ERROR, TAG, contactIdArray));

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray(Arrays.asList(contactIdArray));
		try {
			jsonObject.put("contact_ids", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/contacts_list/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);


	}

	public void userGetStatus(String[] contactIdArray, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API userGetContacts - contactIdList : " + Utils.LogArray(Log.ERROR, TAG, contactIdArray));

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray(Arrays.asList(contactIdArray));
		try {
			jsonObject.put("user_ids", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/status_list/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);


	}

	public void updateGroupName(String groupId, String name, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API updateGroupName to: " + name);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("name", name);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/" + groupId + "/";
		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}

	public void updateGroupImage(String groupId, String imageUrl, String thumbUrl, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API updateGroupImage to: " + imageUrl);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("image_url", imageUrl);
			jsonObject.put("thumb_url", thumbUrl);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/" + groupId + "/";
		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}


	public void addContactsToGroup(String groupId, String[] contacts, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API addContactsToGroup - contactIdList : " + Utils.LogArray(Log.ERROR, TAG, contacts));

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray(Arrays.asList(contacts));
		try {
			jsonObject.put("participants", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/" + groupId + "/participants/";
		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}

	public void removeContactsFromGroup(String groupId, String[] contacts, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API removeContactsFromGroup - contactIdList : " + Utils.LogArray(Log.ERROR, TAG, contacts));

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray(Arrays.asList(contacts));
		try {
			jsonObject.put("participants", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/" + groupId + "/delete_participants/";

		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}

	public void makeAdminToGroup(String groupId, String contact, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API makeAdminToGroup - contact: " + contact);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("is_admin", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/" + groupId + "/participants/" + contact + "/";
		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}

	public void removeAdminFromGroup(String groupId, String contact, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API makeAdminToGroup - contact: " + contact);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("is_admin", false);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/" + groupId + "/participants/" + contact + "/";
		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}


	public void createNewGroup(String name, String imageUrl, String[] contacts, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API createGroup - contactIdList : " + Utils.LogArray(Log.ERROR, TAG, contacts));

		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray(Arrays.asList(contacts));
		try {
			jsonObject.put("name", name);
			jsonObject.put("description", "");
			if (imageUrl != null)
				jsonObject.put("image_url", imageUrl);
			else
				jsonObject.put("image_url", "");

			jsonObject.put("participants", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "group/";
		sendRequestAndHandleResult(Request.Method.POST, url, jsonObject, lsnr);
	}



	public void getGroupDetails(String groupId, final OnServerResponseListener lsnr) {
		Log.e(TAG, "API getGroupDetails groupId: " + groupId);
		String url = BASE_URL + "group/" + groupId + "/";
		sendRequestAndHandleResult(Request.Method.GET, url, null, lsnr);
	}


	public void getAllUserGroupsDetails(String userId, final OnServerResponseArrayListener lsnrArray) {
		Log.e(TAG, "API getAllUserGroupsDetails userId: " + userId);
		String url = BASE_URL + "/group/user/" + userId + "/";
		sendRequestAndHandleResult(Request.Method.GET, url, null, lsnrArray);
	}

	/*****************************************************************************************************************/

	public static final int UPLOAD_MIME_TYPE_IAMGE = 1;
	public static final int UPLOAD_MIME_TYPE_VIDEO = 2;
	public static final int UPLOAD_MIME_TYPE_GIF = 3;
	public static final int UPLOAD_MIME_TYPE_AUDIO = 4;

	public void dataUploadFile(final byte[] fileData, int mimeType, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API dataUploadFile");

		String url;
		if (mimeType == UPLOAD_MIME_TYPE_AUDIO)
			url = BASE_URL + "media/convert_audio/";
		else
			url = BASE_URL + "media/upload/";



		RequestQueue queue = Volley.newRequestQueue(mContext);

		VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
			@Override
			public void onResponse(NetworkResponse response) {

				try {
					String res = new String(response.data);
					JSONObject data = null;
					Log.e(TAG, "Response: " + res);
					JSONObject jsonRes = new JSONObject(res);
					boolean result = jsonRes.getBoolean("success");
					String error = jsonRes.getString("error");
					if (result)
						data = jsonRes.getJSONObject("data");
					lsnr.OnServerResponse(result, error, null, data);
				} catch (Exception e) {
					e.printStackTrace();
					lsnr.OnServerResponse(false, null, e, null);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
				Log.e(TAG, "Error: " + error.getMessage());
				lsnr.OnServerResponse(false, null, error, null);
			}
		}) {


			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params.put("User-Id", mUserId);
				params.put("Api-Token", mToken);
				return params;
			}

			@Override
			protected Map<String, DataPart> getByteData() {
				Map<String, DataPart> params = new HashMap<>();
				// file name could found file base or direct access from real path
				// for now just get bitmap data from ImageView

				switch (mimeType){
					case UPLOAD_MIME_TYPE_IAMGE:
						params.put("file", new DataPart("file.jpg", fileData, "image/jpeg"));
						break;
					case UPLOAD_MIME_TYPE_VIDEO:
						params.put("file", new DataPart("file.mp4", fileData, "video/mp4"));
						break;
					case UPLOAD_MIME_TYPE_AUDIO:
						//params.put("file", new DataPart("file.mp3", fileData, "audio/mp3"));
						params.put("file", new DataPart("file.amr", fileData, "audio/AMR"));

						break;
				}

				return params;
			}
		};

		queue.add(multipartRequest);

	}


	private void sendGetAndHandleResult(String url, HashMap<String, String> params, final OnServerResponseListener lsnr){
		HashMap<String, String> mParams = params;

		RequestQueue queue = Volley.newRequestQueue(mContext);

		if(mParams != null) {
			StringBuilder stringBuilder = new StringBuilder(url);
			Iterator<Map.Entry<String, String>> iterator = mParams.entrySet().iterator();
			int i = 1;
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				if (i == 1) {
					stringBuilder.append("?" + entry.getKey() + "=" + entry.getValue());
				} else {
					stringBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
				}
				iterator.remove(); // avoids a ConcurrentModificationException
				i++;
			}

			url = stringBuilder.toString();
		}

		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
				(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.e(TAG, "Response: " + response.toString());
						JSONObject data = null;
						if (response != null) {
							try {
								boolean result = response.getBoolean("success");
								String error = response.getString("error");
								if (result)
									data = response.getJSONObject("data");
								lsnr.OnServerResponse(result, error, null, data);
							} catch (JSONException e) {
								e.printStackTrace();
								lsnr.OnServerResponse(false, null, e, null);
							}

						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Error: " + error.getMessage());
						lsnr.OnServerResponse(false, null, error, null);

					}

				}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				if ((mUserId != null) && (mToken != null)) {
					Map<String, String> params = new HashMap<String, String>();
					params.put("User-Id", mUserId);
					params.put("Api-Token", mToken);
					return params;
				}
				else
					return super.getHeaders();
			}


			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return mParams;
			}
		};

		// Access the RequestQueue through your singleton class.
		queue.add(jsonObjectRequest);
	}

	public  void updateUserName (String userName, OnServerResponseListener lsnr) {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("user_name", userName);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/";

		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);

	}

	public  void updateUserStatus (String status, OnServerResponseListener lsnr) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("status", status);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/";

		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}

	public  void updateUserProfilePicUrl (String picUrl, OnServerResponseListener lsnr) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("profile_pic_url", picUrl);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/";

		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}

	public  void updateUserLanguage (String language, OnServerResponseListener lsnr) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("language", language);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/";
     	sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}

	public  void updateUserCountryCode (String countryCode, OnServerResponseListener lsnr) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("country_code", countryCode);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String url = BASE_URL + "user/";

		sendRequestAndHandleResult(Request.Method.PATCH, url, jsonObject, lsnr);
	}

	public void  getSupportedLanguages(String language, OnServerResponseListener lsnr) {

		String url = BASE_URL + "supported_languages/?language=" + language;

		sendRequestAndHandleResult(Request.Method.GET, url, null, lsnr);

	}

	public void  getLanguageLocale( OnServerResponseListener lsnr) {

		String url = BASE_URL + "user/language_locale";

		sendRequestAndHandleResult(Request.Method.GET, url, null, lsnr);

	}


	private void sendRequestAndHandleResult(int method, String url, JSONObject jsonObject, final OnServerResponseListener lsnrObject){
		sendRequestAndHandleResult(method, url, jsonObject, lsnrObject, null);
	}

	private void sendRequestAndHandleResult(int method, String url, JSONObject jsonObject, final OnServerResponseArrayListener lsnrArray){
		sendRequestAndHandleResult(method, url, jsonObject, null, lsnrArray);
	}

	private void sendRequestAndHandleResult(int method, String url, JSONObject jsonObject, final OnServerResponseListener lsnrObject, final OnServerResponseArrayListener lsnrArray){
		RequestQueue queue = Volley.newRequestQueue(mContext);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
				(method, url, jsonObject, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						JSONObject dataObject;
						JSONArray  dataArray;
						if (response != null) {
							try {
								boolean result = response.getBoolean("success");
								String error = response.getString("error");
								if (result && (!response.getString("data").equals(""))) {
									Object object = response.get("data");
									if (object instanceof JSONObject) {
											dataObject = (JSONObject)object;
											lsnrObject.OnServerResponse(result, error, null, dataObject);
									} else if (object instanceof JSONArray) {
											dataArray = (JSONArray)object;
											lsnrArray.OnServerResponse(result, error, null, dataArray);
									}
								} else { //If "data" column is null - make the response anyway
								    	lsnrObject.OnServerResponse(result, error, null, null);
								}
							} catch (JSONException e) {
								e.printStackTrace();

								if (lsnrObject != null)
									lsnrObject.OnServerResponse(false, null, e, null);

								if (lsnrArray != null)
									lsnrArray.OnServerResponse(false, null, e, null);
							}
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

						Log.e(TAG, "Error: " + error.getMessage());

						if (lsnrObject != null)
							lsnrObject.OnServerResponse(false, null, error, null);

						if (lsnrArray != null)
							lsnrArray.OnServerResponse(false, null, error, null);

					}

				}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				if ((mUserId != null) && (mToken != null)) {
					Map<String, String> params = new HashMap<String, String>();
					params.put("User-Id", mUserId);
					params.put("Api-Token", mToken);
					Log.e(TAG, "request params: User-Id:" + mUserId + " , Api-Token:"+mToken);
					return params;
				}
				else
					return super.getHeaders();
			}
		};

		// Access the RequestQueue through your singleton class.
		queue.add(jsonObjectRequest);
	}
}
