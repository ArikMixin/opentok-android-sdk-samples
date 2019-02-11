package io.wochat.app.com;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import io.wochat.app.BuildConfig;

public class WochatApi {

	private static final String BASE_URL = "https://api-dev.wochat.io/";
	//private static final String BASE_URL = "https://api.wochat.io/";

	private static final String TAG = "WochatApi";
	private final Context mContext;
	String mUserId = null;
	String mToken = null;



	public interface OnServerResponseListener{
		void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response);
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


	public void userGetContacts(String[] contactIdArray, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API userGetContacts - contactIdList count: " + contactIdArray.length);

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

		Log.e(TAG, "API userGetContacts - contactIdList count: " + contactIdArray.length);

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


	public void dataUploadFile(final byte[] fileData, final OnServerResponseListener lsnr) {

		Log.e(TAG, "API dataUploadFile");

		String url = BASE_URL + "media/upload/";

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
				params.put("file", new DataPart("file.jpg", fileData, "image/jpeg"));

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



	private void sendRequestAndHandleResult(int method, String url, JSONObject jsonObject, final OnServerResponseListener lsnr){
		RequestQueue queue = Volley.newRequestQueue(mContext);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
			(method, url, jsonObject, new Response.Listener<JSONObject>() {


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
		};

		// Access the RequestQueue through your singleton class.
		queue.add(jsonObjectRequest);
	}



}
