package io.wochat.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
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
import io.wochat.app.logic.SMSReceiver;

/**
 * Repository handling the work with products and comments.
 */
public class WCRepository {

	private static final String TAG = "WCRepository";

	private final WCDatabase mDatabase;
	private final WCSharedPreferences mSharedPreferences;
	private final WochatApi mWochatApi;


	private MutableLiveData<String> mUserRegistrationError;
	private MutableLiveData<String> mUserVerificationError;


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


		mUserRegistrationError = new MutableLiveData<>();
		mUserVerificationError = new MutableLiveData<>();

//        WordRoomDatabase db = WordRoomDatabase.getDatabase(application);
//        mWordDao = db.wordDao();
//        mAllWords = mWordDao.getAlphabetizedWords();
    }



	public void userRegistration(String userTrimmedPhone, String userCountryCode) {

		mWochatApi.userRegistration(userCountryCode, userTrimmedPhone, new WochatApi.OnServerResponseListener() {
			@Override
			public void OnServerResponse(boolean isSuccess, String error, JSONObject response) {
				Log.e(TAG, "OnServerResponse userRegistration - isSuccess: " + isSuccess + ", error: " + error + ", response: " + response);
				if (isSuccess) {
					try {
						String userId = response.getString("user_id");
						mSharedPreferences.saveUserId(userId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUserRegistrationError.setValue(null);
				}
				else
					mUserRegistrationError.setValue(error);
			}
		});


	}


	public void userVerification(String code) {
    	String userId = mSharedPreferences.getUserId();
		mWochatApi.userVerification(userId, code, new WochatApi.OnServerResponseListener() {
			@Override
			public void OnServerResponse(boolean isSuccess, String error, JSONObject response) {
				Log.e(TAG, "OnServerResponse userVerification - isSuccess: " + isSuccess + ", error: " + error + ", response: " + response);
				if (isSuccess) {
					try {
						String token = response.getString("token");
						String refresh_token = response.getString("refresh_token");
						String xmpp_pwd = response.getString("xmpp_pwd");
						Log.e(TAG, "OnServerResponse userVerification token: " + token + ", refresh_token: " + refresh_token + ", xmpp_pwd: " + xmpp_pwd);
						//mSharedPreferences.saveUserId(userId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUserVerificationError.setValue(null);
				}
				else
					mUserVerificationError.setValue(error);
			}
		});
	}

	public MutableLiveData<String> getUserRegistrationError(){
    	return mUserRegistrationError;
	}

	public MutableLiveData<String> getUserVerificationError(){
		return mUserVerificationError;
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
