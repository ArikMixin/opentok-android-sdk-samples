package io.wochat.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.wochat.app.com.WochatApi;
import io.wochat.app.db.WCDatabase;
import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.dao.ContactDao;
import io.wochat.app.db.dao.ConversationDao;
import io.wochat.app.db.dao.MessageDao;
import io.wochat.app.db.dao.UserDao;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.ContactLocal;
import io.wochat.app.db.entity.ContactServer;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.ConversationAndItsMessages;
import io.wochat.app.db.entity.Message;
import io.wochat.app.db.entity.User;
import io.wochat.app.model.StateData;
import io.wochat.app.utils.ContactsUtil;
import io.wochat.app.utils.Utils;

/**
 * Repository handling the work with products and comments.
 */
public class WCRepository {

	public interface OnSaveMessageToDBListener{
		void OnSaved(boolean success, Message savedMessage);
	}

	public interface OnMarkMessagesAsReadListener{
		void OnAffectedMessages(List<Message> messageList);
	}


	public enum UserRegistrationState {
		user_reg_ok,
		user_sms_verification_ok,
		user_upload_profile_pic_ok,
		user_confirm_reg_ok
	}

	private static final String TAG = "WCRepository";
	private final AppExecutors mAppExecutors;
	private final WCDatabase mDatabase;
	private final WCSharedPreferences mSharedPreferences;
	private final WochatApi mWochatApi;
	private final ContentResolver mContentResolver;
	private final String mLocaleCountry;

	private MutableLiveData<StateData<UserRegistrationState>> mUserRegistrationState;
	private MutableLiveData<StateData<String>> mUserRegistrationResult;
	private MutableLiveData<StateData<String>> mUserVerificationResult;
	private MutableLiveData<StateData<String>> mUploadProfilePicResult;
	private MutableLiveData<StateData<String>> mUserConfirmRegistrationResult;
	private MutableLiveData<Boolean> mIsDuringRefreshContacts;
	private MutableLiveData<List<Message>> mMarkAsReadAffectedMessages;
	private Map<String, ContactLocal> mLocalContact;
	private Object mLocalContactSyncObject = new Object();


	private UserDao mUserDao;
	private ContactDao mContactDao;
	private ConversationDao mConversationDao;
	private MessageDao mMessageDao;
	private LiveData<List<User>> mAllUsers;
	private LiveData<User> mSelfUser;

    private static WCRepository mInstance;


    public interface APIResultListener{
    	void onApiResult(boolean isSuccess);
	}



    public static WCRepository getInstance(Application application, final WCDatabase database, AppExecutors appExecutors) {
        if (mInstance == null) {
            synchronized (WCRepository.class) {
                if (mInstance == null) {
					mInstance = new WCRepository(application, database, appExecutors);
                }
            }
        }
        return mInstance;
    }



    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public WCRepository(Application application, final WCDatabase database, AppExecutors appExecutors) {
		Log.e(TAG, "WCRepository constructor");
    	mDatabase = database;
    	mAppExecutors = appExecutors;
		mSharedPreferences = WCSharedPreferences.getInstance(application);
		String userId = mSharedPreferences.getUserId();
		String token = mSharedPreferences.getToken();
		mWochatApi = WochatApi.getInstance(application, userId, token);
		mContentResolver = application.getContentResolver();
		mLocaleCountry = Utils.getLocaleCountry(application);


		mUserRegistrationState = new MutableLiveData<>();
		mUserRegistrationResult = new MutableLiveData<>();
		mUserVerificationResult = new MutableLiveData<>();
		mUploadProfilePicResult = new MutableLiveData<>();
		mUserConfirmRegistrationResult = new MutableLiveData<>();
		mIsDuringRefreshContacts = new MutableLiveData<>();
		mIsDuringRefreshContacts.setValue(false);
		mMarkAsReadAffectedMessages = new MutableLiveData<>();
//        WordRoomDatabase db = WordRoomDatabase.getDatabase(application);
//        mWordDao = db.wordDao();
        //mAllWords = mWordDao.getAlphabetizedWords();
		mUserDao = mDatabase.userDao();
		mContactDao = mDatabase.contactDao();
		mConversationDao = mDatabase.conversationDao();
		mMessageDao = mDatabase.messageDao();
		mSelfUser = mUserDao.getFirstUser();

    }



	public void userRegistration(String userTrimmedPhone, String userCountryCode) {
		mSharedPreferences.saveUserPhoneNumAndCountryCode(userTrimmedPhone, userCountryCode);
		mWochatApi.userRegistration(userCountryCode, userTrimmedPhone, new WochatApi.OnServerResponseListener() {

			@Override
			public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
				Log.e(TAG, "OnServerResponse userRegistration - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				if (isSuccess) {
					try {
						String userId = response.getString("user_id");
						mSharedPreferences.saveUserId(userId);
						mWochatApi.setUserId(userId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUserRegistrationResult.setValue(new StateData<String>().success(null));
					mUserRegistrationState.setValue(new StateData<UserRegistrationState>().success(UserRegistrationState.user_reg_ok));
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
						mWochatApi.setToken(token);
						Log.e(TAG, "OnServerResponse userVerification token: " + token + ", refresh_token: " + refreshToken + ", xmpp_pwd: " + xmppPwd);
						//mSharedPreferences.saveUserId(userId);

						initSyncContactsWithServer();

					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUserVerificationResult.setValue(new StateData<String>().success(token));
					mUserRegistrationState.setValue(new StateData<UserRegistrationState>().success(UserRegistrationState.user_sms_verification_ok));
				}
				else if (errorLogic != null)
					mUserVerificationResult.setValue(new StateData<String>().errorLogic(errorLogic));

				else if (errorComm != null)
					mUserVerificationResult.setValue(new StateData<String>().errorComm(errorComm));

			}
		});
	}



	public void userFinishRegistration(byte[] profilePicBytes, String userName){
		if (profilePicBytes == null)
			userConfirmRegistration(null, userName);
		else
    		userUploadProfilePic(profilePicBytes, userName);
	}

	private void userUploadProfilePic(byte[] bytes, final String userName) {
		mSharedPreferences.saveUserProfileImages(bytes);
		mWochatApi.dataUploadFile(bytes, new WochatApi.OnServerResponseListener() {
			@Override
			public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
				Log.e(TAG, "OnServerResponse userUploadProfilePic - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				if (isSuccess) {
					try {
						String imageUrl = response.getString("url");
						String thumbUrl = response.getString("thumb_url");
						mSharedPreferences.saveUserProfileImagesUrls(imageUrl, thumbUrl);
						userConfirmRegistration(imageUrl, userName);
						mUserRegistrationState.setValue(new StateData<UserRegistrationState>().success(UserRegistrationState.user_upload_profile_pic_ok));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mUploadProfilePicResult.setValue(new StateData<String>().success("upload ok"));
				}
				else if (errorLogic != null) {
					mUploadProfilePicResult.setValue(new StateData<String>().errorLogic(errorLogic));
				}

				else if (errorComm != null) {
					mUploadProfilePicResult.setValue(new StateData<String>().errorComm(errorComm));
				}
			}
		});
	}

	private void userConfirmRegistration(String profilePicUrl, String userName){
		mWochatApi.userConfirmRegistration(userName, profilePicUrl, new WochatApi.OnServerResponseListener() {
			@Override
			public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
				Log.e(TAG, "OnServerResponse userConfirmRegistration - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				if (isSuccess) {
					try {
						Gson gson = new Gson();
						User user = gson.fromJson(response.toString(), User.class);
						mSharedPreferences.saveUserLanguage(user.getLanguage());

						ContactServer contactServer = gson.fromJson(response.toString(), ContactServer.class);
						Contact contact = new Contact();
						contact.setContactId(contactServer.getUserId());
						contact.setContactLocal(new ContactLocal());
						contact.setContactServer(contactServer);
						contact.setHasServerData(true);
						insert(user, contact);


						Log.e(TAG, "user: " + user.toString());
						/**
						 * "user_id": "972541234567",
						 *     "user_name": "string",
						 *     "status": "Hey there i'm using WoChat!",
						 *     "country_code": "IL",
						 *     "language": "HE",
						 *     "language_locale": "en_US",
						 *     "profile_pic_url": "http://media.wochat.io/my_img.png",
						 *     "location": {
						 *       "long": 123456,
						 *       "lat": 123456
						 *     },
						 *     "gender": "m",
						 *     "birthdate": 1521636067,
						 *     "last_update_date": 1521646067,
						 *     "discoverable": true,
						 *     "badge": 2,
						 *     "os": "ios",
						 *     "app_version": "2.3.3",
						 *     "push_token": "string",
						 *     "apns_push_token": "string"
						 */

						mUserRegistrationState.setValue(new StateData<UserRegistrationState>().success(UserRegistrationState.user_confirm_reg_ok));

					} catch (Exception e) {
						e.printStackTrace();
					}
					mUserConfirmRegistrationResult.setValue(new StateData<String>().success("reg confirmation ok"));
				}
				else if (errorLogic != null)
					mUserConfirmRegistrationResult.setValue(new StateData<String>().errorLogic(errorLogic));

				else if (errorComm != null)
					mUserConfirmRegistrationResult.setValue(new StateData<String>().errorComm(errorComm));

			}
		});
	}


	public MutableLiveData<StateData<UserRegistrationState>> getUserRegistrationState(){
		return mUserRegistrationState;
	}

	public MutableLiveData<StateData<String>> getUserRegistrationResult(){
    	return mUserRegistrationResult;
	}

	public MutableLiveData<StateData<String>> getUserVerificationResult(){
		return mUserVerificationResult;
	}

	public MutableLiveData<StateData<String>> getUploadProfilePicResult(){
		return mUploadProfilePicResult;
	}

	public MutableLiveData<StateData<String>> getUserConfirmRegistrationResult(){
		return mUserConfirmRegistrationResult;
	}

	public String getUserCountryCode() {
    	return mSharedPreferences.getUserCountryCode();
	}

	public boolean hasUserRegistrationData() {
		return mSharedPreferences.hasUserRegistrationData();
	}



	public LiveData<List<User>> getAllUsers() {
		return mAllUsers;
	}

	public LiveData<User> getSelfUser() {
		return mSelfUser;
	}




    public void insert(User user) {
		mAppExecutors.diskIO().execute(() -> {
			mDatabase.userDao().deleteAll();
			mDatabase.userDao().insert(user);
		});
	}

	public void insert(User user, Contact contact) {
		mAppExecutors.diskIO().execute(() -> {
			mDatabase.userDao().deleteAll();
			mDatabase.userDao().insert(user);
			mDatabase.contactDao().insert(contact);
		});
	}


	private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {

		private UserDao mAsyncTaskDao;

		InsertUserAsyncTask(UserDao dao) {
			mAsyncTaskDao = dao;
		}

		@Override
		protected Void doInBackground(final User... params) {
			mAsyncTaskDao.deleteAll();
			mAsyncTaskDao.insert(params[0]);
			return null;
		}
	}

	public LiveData<List<Contact>> getAllContacts() {
		return mContactDao.getContacts();
	}

	public LiveData<List<Contact>> getServerContacts() {
		return mContactDao.getServerContacts();
	}

	public MutableLiveData<Boolean> getIsDuringRefreshContacts() {
    	return mIsDuringRefreshContacts;
	}




	public LiveData<Contact> getContact(String id) {
		return mContactDao.getContactLD(id);
	}


	private Contact[] updateContactsWithLocals(Map<String, ContactServer> contactServers, Map<String, ContactLocal> localContacts){
		Contact[] contacts = new Contact[localContacts.size()];
		int i=0;
		for (ContactLocal contactLocal: localContacts.values()) {
			String id = contactLocal.getPhoneNumStripped();
			ContactServer contactServer = contactServers.get(id);
			Contact contact = new Contact();
			contact.setContactId(id);
			contact.setContactLocal(contactLocal);
			contact.setContactServer(contactServer);
			contact.setHasServerData(contactServer != null);
			contacts[i++] = contact;
		}

		return contacts;
	}
	
	private void updateContactsWithLocals(Contact[] contacts, Map<String, ContactLocal> localContacts){

    	for (int i=0; i<contacts.length; i++){
			String id = contacts[i].getId();
			ContactLocal local = localContacts.get(id);
    		contacts[i].setContactLocal(local);
		}

//		ContactLocal[] contactLocalArray = localContacts.values().toArray(new ContactLocal[0]);
//		new UpdateContactsWithLocalsAsyncTask(mDatabase).execute(contactLocalArray);
	}

//	private static class UpdateContactsWithLocalsAsyncTask extends AsyncTask<ContactLocal[], Void, Void> {
//
//		private WCDatabase mDatabase;
//
//		UpdateContactsWithLocalsAsyncTask(WCDatabase database) {
//			mDatabase = database;
//		}
//
//		@Override
//		protected Void doInBackground(final ContactLocal[]... params) {
//
//			WCDatabase.updateContactsWithLocals(mDatabase, params[0]);
//			return null;
//		}
//	}


	public void insertContactAndUpdateConversationContactData(Contact contact) {
		mAppExecutors.diskIO().execute(() -> {
			mDatabase.runInTransaction(() -> {
				mContactDao.insert(contact);
				mConversationDao.updateConversationWithContactData(contact.getId(), contact.getName(), contact.getLanguage(), contact.getAvatar());
			});
		});

	}


	public void insert(Contact[] contacts) {
		new InsertContactsAsyncTask(mDatabase).execute(contacts);
	}

	private class InsertContactsAsyncTask extends AsyncTask<Contact[], Void, Void> {

		private WCDatabase mDatabase;

		InsertContactsAsyncTask(WCDatabase database) {
			mDatabase = database;
		}

		@Override
		protected Void doInBackground(final Contact[]... params) {

			WCDatabase.insertContacts(mDatabase, params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mIsDuringRefreshContacts.setValue(false);
		}
	}

	public void insert(ContactLocal[] contactsLocals) {
		new InsertContactsLocalsAsyncTask(mDatabase).execute(contactsLocals);
	}

	private static class InsertContactsLocalsAsyncTask extends AsyncTask<ContactLocal[], Void, Void> {

		private WCDatabase mDatabase;

		InsertContactsLocalsAsyncTask(WCDatabase database) {
			mDatabase = database;
		}

		@Override
		protected Void doInBackground(final ContactLocal[]... params) {
			WCDatabase.insertContactsLocal(mDatabase, params[0]);
			return null;
		}
	}


//	private static class InsertContactsLocalsAsyncTask extends AsyncTask<ContactLocal[], Void, Void> {
//
//		private ContactLocalDao mAsyncTaskDao;
//
//		InsertContactsLocalsAsyncTask(ContactLocalDao dao) {
//			mAsyncTaskDao = dao;
//		}
//
//		@Override
//		protected Void doInBackground(final ContactLocal[]... params) {
//			mAsyncTaskDao.insert(params[0]);
//			return null;
//		}
//	}



	public void syncContactsLocalAndServer(){
		mLocalContact = null;
		mIsDuringRefreshContacts.setValue(true);
		retrieveLocalContacts();
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				initSyncContactsWithServer();
			}
		}, 20000);
	}

	public void retrieveLocalContacts(){
		Log.e(TAG, "retrieveLocalContacts");
		RetrieveLocalContactsAsyncTask asyncTask = new RetrieveLocalContactsAsyncTask();
		asyncTask.executeOnExecutor(mAppExecutors.diskIO());
		//asyncTask.execute();

	}

	private void initSyncContactsWithServer() {
    	Log.e(TAG, "initSyncContactsWithServer called");
		synchronized(mLocalContactSyncObject){
			if (mLocalContact != null) {
				syncContactsWithServer(mLocalContact);
			}
			else {
				Log.e(TAG, "initSyncContactsWithServer, Local Contact empty, wait 10 sec...");
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						initSyncContactsWithServer();
					}
				}, 10000);
			}
		}


	}


	public void syncContactsWithServer(Map<String, ContactLocal> localContacts){
		Log.e(TAG, "syncContactsWithServer called");
		String[] contactArray = new String[localContacts.size()];
		int i=0;

		for(String contact : localContacts.keySet()){
			contactArray[i++] = contact;
		}
		mWochatApi.userGetContacts(contactArray, new WochatApi.OnServerResponseListener() {
			@Override
			public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
				if (isSuccess) {
					JSONArray jsonContactArray = null;
					try {
						jsonContactArray = response.getJSONArray("contacts");
						Map<String, ContactServer> contactServersMap = new HashMap<>();
						int j = 0;
						for (int i = 0; i < jsonContactArray.length(); i++) {
							JSONObject jsonContactObj = jsonContactArray.getJSONObject(i);
							Gson gson = new Gson();
							ContactServer contactServer = gson.fromJson(jsonContactObj.toString(), ContactServer.class);
							contactServersMap.put(contactServer.getUserId(), contactServer);
						}

						synchronized(mLocalContactSyncObject) {
							Contact[] contacts = updateContactsWithLocals(contactServersMap, mLocalContact);
							insert(contacts);
						}

					} catch (JSONException e) {
						e.printStackTrace();
						mIsDuringRefreshContacts.setValue(false);
					}
				}
				else
					mIsDuringRefreshContacts.setValue(false);

			}
		});

	}


	private class RetrieveLocalContactsAsyncTask extends AsyncTask<Void, Void, Map<String, ContactLocal>> {


		@Override
		protected Map<String, ContactLocal> doInBackground(final Void... params) {
			ContactsUtil contactsUtil = new ContactsUtil();
			Map<String, ContactLocal> map = contactsUtil.readContacts(mContentResolver, mLocaleCountry);
			return map;
		}

		@Override
		protected void onPostExecute(Map<String, ContactLocal> localContacts) {
			ContactLocal[] contactLocalArray = localContacts.values().toArray(new ContactLocal[0]);
			insert(contactLocalArray);

			synchronized(mLocalContactSyncObject) {
				mLocalContact = localContacts;
			}

		}
	}

//	public LiveData<List<ContactInvitation>> getContactInvitations() {
//		return mContactDao.getContactInvitations();
//	}
//

public ConversationAndItsMessages getConversationAndMessagesSorted(String conversationId){
		ConversationAndItsMessages caim = new ConversationAndItsMessages();
		caim.setConversation(mConversationDao.getConversation(conversationId));
		caim.setMessages(mMessageDao.getMessagesForConversation(conversationId));
		return caim;
	}

	public LiveData<List<Message>> getMessagesLD(String conversationId){
		return mMessageDao.getMessagesForConversationLD(conversationId);
	}

	public LiveData<Conversation> getConversationLD(String conversationId){
		return mConversationDao.getConversationLD(conversationId);
	}

	public LiveData<List<Conversation>> getConversationListLD(){
		return mConversationDao.getConversationListLD();
	}

	public Conversation getConversation(String conversationId){
		return mConversationDao.getConversation(conversationId);
	}


	public boolean hasConversation(String conversationId){
    	return mConversationDao.hasConversation(conversationId);
	}

	public void addNewConversation(Conversation conversation){
		mAppExecutors.diskIO().execute(() -> mConversationDao.insert(conversation));
	}

	public LiveData<List<Message>> getUnreadMessagesConversation(String conversationId) {
		return mMessageDao.getUnreadMessagesConversationLD(conversationId);
	}

	public LiveData<Integer> getUnreadConversationNum(){
		return mConversationDao.getUnreadConversationCount();
	}


	public void markAllMessagesAsRead(String conversationId){
		mAppExecutors.diskIO().execute(() -> {
			try {
				Log.e(TAG, "markAllMessagesAsRead, conversationId: " + conversationId);
				mMessageDao.markAllMessagesAsRead(conversationId);
				mConversationDao.markAllMessagesAsRead(conversationId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public boolean handleIncomingMessage(Message message, final OnSaveMessageToDBListener listener) {
    	if (message == null)
    		return false;

    	mAppExecutors.diskIO().execute(() -> {
			boolean res = true;
			message.setParticipantId(message.getSenderId());
			message.setRecipients(new String[]{mSharedPreferences.getUserId()});
			String convId = Conversation.getConversationId(message.getSenderId(), message.getRecipients()[0]);
			message.setConversationId(convId);
			switch (message.getMessageType()){
				case Message.MSG_TYPE_TEXT:
					message.setAckStatus(Message.ACK_STATUS_RECEIVED);
					res = handleIncomingMessageText(message);
					listener.OnSaved(res, message);
					break;
				case Message.MSG_TYPE_ACKNOWLEDGMENT:
					res = handleAcknowledgmentMessage(message);
					break;
				case Message.MSG_TYPE_TYPING_SIGNAL:
					break;
				case Message.MSG_TYPE_IMAGE:
					break;
				case Message.MSG_TYPE_GIF:
					break;
			}


		});
    	return true;
	}

	private boolean handleAcknowledgmentMessage(Message message) {
		mDatabase.runInTransaction(() -> {
			switch (message.getAckStatus()) {
				case Message.ACK_STATUS_SENT:
					mMessageDao.updateAckStatusToSent(message.getOriginalMessageId());
					break;
				case Message.ACK_STATUS_RECEIVED:
					mMessageDao.updateAckStatusToReceived(message.getOriginalMessageId());
					break;
				case Message.ACK_STATUS_READ:
					mMessageDao.updateAckStatusToRead(message.getOriginalMessageId());
					break;
			}
		});
		return true;
	}

	private boolean handleIncomingMessageText(Message message){
    	boolean res;
		try {
			String conversationId = message.getConversationId();
			if(mConversationDao.hasConversation(conversationId)) { // has conversation (and contact)
				res = insertMessageAndUpdateConversation(message);
			}
			else {
				String participantId = message.getSenderId();
				String selfId = mSharedPreferences.getUserId();
				if(mContactDao.hasContact(participantId)){  // has contact, no conversation
					Contact contact = mContactDao.getContact(participantId);
					Conversation conversation = new Conversation(participantId, selfId);
					conversation.setParticipantName(contact.getName());
					conversation.setParticipantLanguage(contact.getLanguage());
					conversation.setParticipantProfilePicUrl(contact.getAvatar());
					mConversationDao.insert(conversation);
					res = insertMessageAndUpdateConversation(message);
				}
				else { // no contact, no conversation
					Contact contact = new Contact(participantId);
					mContactDao.insert(contact);
					getContactFromServer(participantId);
					Conversation conversation = new Conversation(participantId, selfId);
					mConversationDao.insert(conversation);
					res = insertMessageAndUpdateConversation(message);
				}
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void getContactFromServer(String participantId) {
    	String[] contactArray = new String[1];
		contactArray[0] = participantId;
		mWochatApi.userGetContacts(contactArray, (isSuccess, errorLogic, errorComm, response) -> {
			if (isSuccess) {
				JSONArray jsonContactArray = null;
				try {
					jsonContactArray = response.getJSONArray("contacts");
					if (jsonContactArray.length() > 0) {
						JSONObject jsonContactObj = jsonContactArray.getJSONObject(0);
						Gson gson = new Gson();
						ContactServer contactServer = gson.fromJson(jsonContactObj.toString(), ContactServer.class);
						Contact contact = new Contact(contactServer);
						insertContactAndUpdateConversationContactData(contact);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private boolean insertMessageAndUpdateConversation(Message message){
		try {
			mMessageDao.insert(message);
			int unreadMessagesCount = mMessageDao.getUnreadMessagesCountConversation(message.getConversationId());
			mConversationDao.updateIncoming(
				message.getConversationId(),
				message.getMessageId(),
				message.getTimestamp(),
				message.getMessageText(),
				message.getSenderId(),
				message.getAckStatus(),
				unreadMessagesCount);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


//	public void updateInvited(String contactId) {
//		new updateInvitedAsyncTask(mDatabase).execute(contactId);
//	}
//
//
//
//	private static class updateInvitedAsyncTask extends AsyncTask<String, Void, Void> {
//
//		private WCDatabase mDatabase;
//
//		updateInvitedAsyncTask(WCDatabase database) {
//			mDatabase = database;
//		}
//
//		@Override
//		protected Void doInBackground(final String... params) {
//			ContactInvitation ci = new ContactInvitation(params[0]);
//			WCDatabase.insertContactInvitation(mDatabase, ci);
//			return null;
//		}
//	}
public void updateAckStatusToSent(String messageId){
    	mAppExecutors.diskIO().execute(new Runnable() {
			@Override
			public void run() {
				mMessageDao.updateAckStatusToSent(messageId);
			}
		});

	}

	public void addNewOutgoingMessage(Message message) {
		mAppExecutors.diskIO().execute(new Runnable() {
			@Override
			public void run() {
				mMessageDao.insert(message);
				mConversationDao.updateOutgoing(
					message.getConversationId(),
					message.getMessageId(),
					message.getTimestamp(),
					message.getMessageText(),
					message.getSenderId(),
					message.getAckStatus());
			}
		});
	}
}
