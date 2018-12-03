package io.wochat.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.wochat.app.com.WochatApi;
import io.wochat.app.db.WCDatabase;
import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.dao.WordDao;
import io.wochat.app.db.entity.Word;
import io.wochat.app.model.StateData;

/**
 * Repository handling the work with products and comments.
 */
public class WCRepository {

	private static final String TAG = "WCRepository";

	private final WCDatabase mDatabase;
	private final WCSharedPreferences mSharedPreferences;
	private final WochatApi mWochatApi;


	private MutableLiveData<StateData<String>> mUserRegistrationResult;
	//private MutableLiveData<String> mUserVerificationResult;
	private MutableLiveData<StateData<String>> mUserVerificationResult;


	private WordDao mWordDao;
    private LiveData<List<Word>> mAllWords;
    private static WCRepository mInstance;



    public static WCRepository getInstance(Application application, final WCDatabase database) {
        if (mInstance == null) {
            synchronized (WCRepository.class) {
                if (mInstance == null) {
					mInstance = new WCRepository(application, database);
                }
            }
        }
        return mInstance;
    }



    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public WCRepository(Application application, final WCDatabase database) {

    	mDatabase = database;
		mSharedPreferences = WCSharedPreferences.getInstance(application);
		mWochatApi = WochatApi.getInstance(application);


		mUserRegistrationResult = new MutableLiveData<>();
		mUserVerificationResult = new MutableLiveData<>();
//        WordRoomDatabase db = WordRoomDatabase.getDatabase(application);
//        mWordDao = db.wordDao();
//        mAllWords = mWordDao.getAlphabetizedWords();
    }



	public void userRegistration(String userTrimmedPhone, String userCountryCode) {
		mSharedPreferences.saveUserCountryCode(userCountryCode);
		mWochatApi.userRegistration(userCountryCode, userTrimmedPhone, new WochatApi.OnServerResponseListener() {

			@Override
			public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
				Log.e(TAG, "OnServerResponse userRegistration - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				if (isSuccess) {
					try {
						String userId = response.getString("user_id");
						mSharedPreferences.saveUserId(userId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUserRegistrationResult.setValue(new StateData<String>().success(null));
				}
				else if (errorLogic != null)
					mUserRegistrationResult.setValue(new StateData<String>().errorLogic(errorLogic));

				else if (errorComm != null)
					mUserRegistrationResult.setValue(new StateData<String>().errorComm(errorComm));
			}
		});


	}


	public void userVerification(String code) {
    	String userId = mSharedPreferences.getUserId();
		mWochatApi.userVerification(userId, code, new WochatApi.OnServerResponseListener() {
			@Override
			public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
				Log.e(TAG, "OnServerResponse userVerification - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				String token = null;
				String refreshToken = null;
				String xmppPwd = null;
				if (isSuccess) {
					try {
						token = response.getString("token");
						refreshToken = response.getString("refresh_token");
						xmppPwd = response.getString("xmpp_pwd");
						mSharedPreferences.saveUserRegistrationData(token, refreshToken, xmppPwd);
						Log.e(TAG, "OnServerResponse userVerification token: " + token + ", refresh_token: " + refreshToken + ", xmpp_pwd: " + xmppPwd);
						//mSharedPreferences.saveUserId(userId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUserVerificationResult.setValue(new StateData<String>().success(token));
				}
				else if (errorLogic != null)
					mUserVerificationResult.setValue(new StateData<String>().errorLogic(errorLogic));

				else if (errorComm != null)
					mUserVerificationResult.setValue(new StateData<String>().errorComm(errorComm));
			}
		});
	}

	public MutableLiveData<StateData<String>> getUserRegistrationResult(){
    	return mUserRegistrationResult;
	}

	public MutableLiveData<StateData<String>> getUserVerificationResult(){
		return mUserVerificationResult;
	}

	public String getUserCountryCode() {
    	return mSharedPreferences.getUserCountryCode();
	}

	public boolean hasUserRegistrationData() {
		return mSharedPreferences.hasUserRegistrationData();
	}

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Word>> getAllWords() {

    	return mAllWords;
    }

    // You must call this on a non-UI thread or your app will crash.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    public void insert(Word word) {
        new WCRepository.insertAsyncTask(mWordDao).execute(word);
    }



	private static class insertAsyncTask extends AsyncTask<Word, Void, Void> {

        private WordDao mAsyncTaskDao;

        insertAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Word... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
