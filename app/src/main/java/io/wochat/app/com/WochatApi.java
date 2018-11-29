package io.wochat.app.com;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import io.wochat.app.BuildConfig;

public class WochatApi {

	private static final String BASE_URL = "https://api-dev.wochat.io/";

	private static final String TAG = "WochatApi";
	private final Context mContext;



	public interface OnServerResponseListener{
		void OnServerResponse(boolean isSuccess, String error, JSONObject response);
	}

	private static WochatApi mInstance;

	public static WochatApi getInstance(Context context) {
		if (mInstance == null) {
			synchronized (WochatApi.class) {
				if (mInstance == null) {
					mInstance = new WochatApi(context);
				}
			}
		}
		return mInstance;
	}

	private WochatApi(Context context){
		super();
		mContext = context.getApplicationContext();
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
		RequestQueue queue = Volley.newRequestQueue(mContext);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
			(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
					Log.e(TAG, "Response: " + response.toString());
					if (response != null) {
						try {
							boolean result = response.getBoolean("success");
							String error = response.getString("error");
							JSONObject data = response.getJSONObject("data");
							lsnr.OnServerResponse(result, error, data);
						} catch (JSONException e) {
							e.printStackTrace();
							lsnr.OnServerResponse(false, e.getMessage(), null);
						}

					}
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e(TAG, "Error: " + error.getMessage());
					lsnr.OnServerResponse(false, error.getMessage(), null);

				}
			});

		// Access the RequestQueue through your singleton class.
		queue.add(jsonObjectRequest);

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
		RequestQueue queue = Volley.newRequestQueue(mContext);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
			(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
					Log.e(TAG, "Response: " + response.toString());
					if (response != null) {
						try {
							boolean result = response.getBoolean("success");
							String error = response.getString("error");
							JSONObject data = response.getJSONObject("data");
							lsnr.OnServerResponse(result, error, data);
						} catch (JSONException e) {
							e.printStackTrace();
							lsnr.OnServerResponse(false, e.getMessage(), null);
						}

					}
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e(TAG, "Error: " + error.getMessage());
					lsnr.OnServerResponse(false, error.getMessage(), null);

				}
			});

		// Access the RequestQueue through your singleton class.
		queue.add(jsonObjectRequest);
	}


}
