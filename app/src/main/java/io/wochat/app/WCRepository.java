package io.wochat.app;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.wochat.app.com.WochatApi;
import io.wochat.app.db.WCDatabase;
import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.dao.ContactDao;
import io.wochat.app.db.dao.ConversationDao;
import io.wochat.app.db.dao.GroupDao;
import io.wochat.app.db.dao.MessageDao;
import io.wochat.app.db.dao.NotifDao;
import io.wochat.app.db.dao.UserDao;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.ContactLocal;
import io.wochat.app.db.entity.ContactServer;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.ConversationAndItsMessages;
import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.GroupMemberMessage;
import io.wochat.app.db.entity.Message;
import io.wochat.app.db.entity.Notif;
import io.wochat.app.model.ConversationAndItsGroupMembers;
import io.wochat.app.db.entity.GroupMemberContact;
import io.wochat.app.model.SupportedLanguage;
import io.wochat.app.db.entity.User;
import io.wochat.app.model.NotificationData;
import io.wochat.app.model.StateData;
import io.wochat.app.utils.ContactsUtil;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.utils.SpeechToTextUtil;
import io.wochat.app.utils.TextToSpeechUtil;
import io.wochat.app.utils.Utils;


/**
 * Repository handling the work with products and comments.
 */
public class WCRepository {



	public interface OnSaveMessageToDBListener {
		void OnSaved(boolean success, Message savedMessage, Contact participant);
	}

	public interface OnMarkMessagesAsReadListener {
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
	private MutableLiveData<StateData<Message>> mUploadImageResult;
	private MutableLiveData<StateData<Conversation>> mCreateGroupResult;
	private MutableLiveData<StateData<String>> mUploadOnlyImageResult;
	private MutableLiveData<Boolean> mIsDuringRefreshContacts;
	private MutableLiveData<List<Message>> mMarkAsReadAffectedMessages;
	private Map<String, ContactLocal> mLocalContact;
	private Object mLocalContactSyncObject = new Object();
	private MutableLiveData<List<SupportedLanguage>> mSupportLanguages;
	private MutableLiveData<StateData<Void>> mUserProfileEditResult;


	private UserDao mUserDao;
	private ContactDao mContactDao;
	private GroupDao mGroupDao;
	private ConversationDao mConversationDao;
	private MessageDao mMessageDao;
	private NotifDao mNotifDao;
	private LiveData<List<User>> mAllUsers;
	private LiveData<User> mSelfUser;

	private static WCRepository mInstance;


	public interface APIResultListener {
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
		mUploadImageResult = new MutableLiveData<>();
		mCreateGroupResult = new MutableLiveData<>();
		mUploadOnlyImageResult = new MutableLiveData<>();
		mUserConfirmRegistrationResult = new MutableLiveData<>();
		mIsDuringRefreshContacts = new MutableLiveData<>();
		mIsDuringRefreshContacts.setValue(false);
		mMarkAsReadAffectedMessages = new MutableLiveData<>();
		mSupportLanguages = new MutableLiveData<>();
		mUserProfileEditResult = new MutableLiveData<>();
//        WordRoomDatabase db = WordRoomDatabase.getDatabase(application);
//        mWordDao = db.wordDao();
		//mAllWords = mWordDao.getAlphabetizedWords();
		mUserDao = mDatabase.userDao();
		mContactDao = mDatabase.contactDao();
		mGroupDao = mDatabase.groupDao();

		mConversationDao = mDatabase.conversationDao();
		mMessageDao = mDatabase.messageDao();
		mNotifDao = mDatabase.notifDao();
		mSelfUser = mUserDao.getFirstUser();


		TextToSpeechUtil ttsu = TextToSpeechUtil.getInstance();
		ttsu.setTextToSpeechInitListener(new TextToSpeechUtil.TextToSpeechInitListener() {
			@Override
			public void onTextToSpeechInitOK() {
				Log.e("TextToSpeechUtil", "onTextToSpeechInitOK");
			}

			@Override
			public void onTextToSpeechInitFAIL() {
				Log.e("TextToSpeechUtil", "onTextToSpeechInitFAIL");
			}
		});
		ttsu.init(application, mSharedPreferences.getUserLang());


		SpeechToTextUtil speechToTextUtil =  SpeechToTextUtil.getInstance();
		speechToTextUtil.init(application, application.getPackageName(), mSharedPreferences.getUserLang());
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


	public void userFinishRegistration(byte[] profilePicBytes, String userName) {
		if (profilePicBytes == null)
			userConfirmRegistration(null, userName);
		else
			userUploadProfilePic(profilePicBytes, userName);
	}

	private void userUploadProfilePic(byte[] bytes, final String userName) {
		mSharedPreferences.saveUserProfileImages(bytes);
		mWochatApi.dataUploadFile(bytes, mWochatApi.UPLOAD_MIME_TYPE_IAMGE, new WochatApi.OnServerResponseListener() {
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

	private void userConfirmRegistration(String profilePicUrl, String userName) {
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


	public MutableLiveData<StateData<UserRegistrationState>> getUserRegistrationState() {
		return mUserRegistrationState;
	}

	public MutableLiveData<StateData<String>> getUserRegistrationResult() {
		return mUserRegistrationResult;
	}

	public MutableLiveData<StateData<String>> getUserVerificationResult() {
		return mUserVerificationResult;
	}

	public MutableLiveData<StateData<String>> getUploadProfilePicResult() {
		return mUploadProfilePicResult;
	}

	public MutableLiveData<StateData<Message>> getUploadImageResult() {
		return mUploadImageResult;
	}

	public MutableLiveData<StateData<String>> getUploadOnlyImageResult() {
		return mUploadOnlyImageResult;
	}

	public MutableLiveData<StateData<Conversation>> getCreateGroupResult() {
		return mCreateGroupResult;
	}


	public MutableLiveData<StateData<String>> getUserConfirmRegistrationResult() {
		return mUserConfirmRegistrationResult;
	}

//	public String getUserCountryCode() {
//		return mSharedPreferences.getUserCountryCode();
//	}

	public boolean hasUserRegistrationData() {
		return mSharedPreferences.hasUserRegistrationData();
	}


	public LiveData<List<User>> getAllUsers() {
		return mAllUsers;
	}

	public LiveData<User> getSelfUser() {
		return mSelfUser;
	}

	public String getSelfUserId() {
		return mSharedPreferences.getUserId();
	}


	public void uploadAudio(Message message, byte[] mediaFileBytes) {
		mAppExecutors.networkIO().execute(() -> {

			mWochatApi.dataUploadFile(mediaFileBytes, mWochatApi.UPLOAD_MIME_TYPE_AUDIO, new WochatApi.OnServerResponseListener() {
				@Override
				public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
					Log.e(TAG, "OnServerResponse uploadImage - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
					if (isSuccess) {
						try {
							String mediaUrl = response.getString("sound_url");
							message.setMediaUrl(mediaUrl);
							updateMessageOnly(message);
							mUploadImageResult.setValue(new StateData<Message>().success(message));
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
					else if (errorLogic != null) {
						mUploadImageResult.setValue(new StateData<Message>().errorLogic(errorLogic));
					}

					else if (errorComm != null) {
						mUploadImageResult.setValue(new StateData<Message>().errorComm(errorComm));
					}
				}
			});

		});
	}

	public void uploadVideo(Message message, byte[] mediaFileBytes) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.dataUploadFile(mediaFileBytes, mWochatApi.UPLOAD_MIME_TYPE_VIDEO, new WochatApi.OnServerResponseListener() {
				@Override
				public void OnServerResponse(boolean isSuccess, String errorLogic, Throwable errorComm, JSONObject response) {
					Log.e(TAG, "OnServerResponse uploadImage - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
					if (isSuccess) {
						try {
							String mediaUrl = response.getString("url");
							String mediaThumbUrl = response.getString("thumb_url");
							message.setMediaThumbnailUrl(mediaThumbUrl);
							message.setMediaUrl(mediaUrl);
							Picasso.get().load(mediaThumbUrl).fetch(new Callback() { // pre load it
								@Override
								public void onSuccess() {
									updateMessageOnly(message);
									mUploadImageResult.setValue(new StateData<Message>().success(message));
								}

								@Override
								public void onError(Exception e) {
									mUploadImageResult.setValue(new StateData<Message>().errorComm(e));
								}
							});
//							updateMessageOnly(message);
//							mUploadImageResult.setValue(new StateData<Message>().success(message));
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
					else if (errorLogic != null) {
						mUploadImageResult.setValue(new StateData<Message>().errorLogic(errorLogic));
					}

					else if (errorComm != null) {
						mUploadImageResult.setValue(new StateData<Message>().errorComm(errorComm));
					}
				}
			});

		});
	}


	public void uploadImage(Message message, byte[] bytes) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.dataUploadFile(bytes, mWochatApi.UPLOAD_MIME_TYPE_IAMGE, (isSuccess, errorLogic, errorComm, response) -> {
				Log.e(TAG, "OnServerResponse uploadImage - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				if (isSuccess) {
					try {
						String imageUrl = response.getString("url");
						String imageThumbUrl = response.getString("thumb_url");
						message.setMediaThumbnailUrl(imageThumbUrl);
						message.setMediaUrl(imageUrl);
						Picasso.get().load(imageThumbUrl).fetch(new Callback() { // pre load it
							@Override
							public void onSuccess() {
								updateMessageOnly(message);
								mUploadImageResult.setValue(new StateData<Message>().success(message));
							}

							@Override
							public void onError(Exception e) {
								mUploadImageResult.setValue(new StateData<Message>().errorComm(e));
							}
						});
//							updateMessageOnly(message);
//							mUploadImageResult.setValue(new StateData<Message>().success(message));
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
				else if (errorLogic != null) {
					mUploadImageResult.setValue(new StateData<Message>().errorLogic(errorLogic));
				}

				else if (errorComm != null) {
					mUploadImageResult.setValue(new StateData<Message>().errorComm(errorComm));
				}
			});

		});
	}



	private ConversationAndItsGroupMembers handleGroupResult(JSONObject response, Resources resources){
		try {
			String name = response.getString("name");
			String description = response.getString("description");
			String image_url = response.getString("image_url");
			String id = response.getString("id");
			int created_date = response.getInt("created_date");
			String created_by = response.getString("created_by");
			JSONArray participantsArray = response.getJSONArray("participants");
			ArrayList<GroupMember> gml = new ArrayList<>();
			Gson gson = new Gson();

			GroupMember.initMemberColorIndex();

			String selfId = mSharedPreferences.getUserId();
			boolean isSelfInGroup = false;

			for (int j=0; j< participantsArray.length(); j++){
				String p = participantsArray.getString(j);
				GroupMember gm = gson.fromJson(p, GroupMember.class);
				gm.setGroupId(id);
				gm.setColor(GroupMember.getNextMemberColor(resources));
				gml.add(gm);
				if(gm.getUserId().equals(selfId))
					isSelfInGroup = true;

			}

			Conversation conversation = new Conversation(id, name, description, image_url, created_by, created_date);
			conversation.setSelfInGroup(isSelfInGroup);

			Log.e(TAG, "gml: " + gml.toString());

			ConversationAndItsGroupMembers cgm = new ConversationAndItsGroupMembers();
			cgm.setConversation(conversation);
			cgm.setGroupMembers(gml);
			return cgm;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

	}

	public void createNewGroup(String groupName, byte[] bytes, List<Contact> contactList, Resources resources){
		mAppExecutors.networkIO().execute(() -> {

			String[] contactArray = new String[contactList.size()];
			int i=0;
			for (Contact contact : contactList){
				contactArray[i++] = contact.getContactId();
			}
			if (bytes != null) {
				mWochatApi.dataUploadFile(bytes, mWochatApi.UPLOAD_MIME_TYPE_IAMGE, (isSuccess, errorLogic, errorComm, response) -> {
					if (isSuccess) {
						try {
							String imageUrl = response.getString("url");
							mWochatApi.createNewGroup(groupName, imageUrl, contactArray, (isSuccess1, errorLogic1, errorComm1, response1) -> {
								if (isSuccess1) {
									ConversationAndItsGroupMembers cgm = handleGroupResult(response1, resources);
									mAppExecutors.diskIO().execute(() -> {
										mConversationDao.insert(cgm.getConversation());
										mGroupDao.insert(cgm.getGroupMembers());
										Message groupCreatedMessage = Message.getGroupCreatedMessageSelf(
											cgm.getConversation().getConversationId(),
											cgm.getConversation().getGroupName(),
											mSharedPreferences.getUserId());
										mMessageDao.insert(groupCreatedMessage);
										mAppExecutors.mainThread().execute(() -> {
											mCreateGroupResult.setValue(new StateData<Conversation>().success(cgm.getConversation()));
										});
									});
									Log.e(TAG, "gml: " + cgm.getGroupMembers().toString());
									Log.e(TAG, "createNewGroup: " + response1.toString());
								}
								else if (errorLogic1 != null) {
									mCreateGroupResult.setValue(new StateData<Conversation>().errorLogic(errorLogic1));
								}

								else if (errorComm1 != null) {
									mCreateGroupResult.setValue(new StateData<Conversation>().errorComm(errorComm1));
								}

							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else if (errorLogic != null) {
						mCreateGroupResult.setValue(new StateData<Conversation>().errorLogic(errorLogic));
					}

					else if (errorComm != null) {
						mCreateGroupResult.setValue(new StateData<Conversation>().errorComm(errorComm));
					}
				});
			}
			else {// no pic
				mWochatApi.createNewGroup(groupName, null, contactArray, (isSuccess1, errorLogic1, errorComm1, response1) -> {
					if (isSuccess1) {
						ConversationAndItsGroupMembers cgm = handleGroupResult(response1, resources);
						mAppExecutors.diskIO().execute(() -> {
							mConversationDao.insert(cgm.getConversation());
							mGroupDao.insert(cgm.getGroupMembers());
							Message groupCreatedMessage = Message.getGroupCreatedMessageSelf(
								cgm.getConversation().getConversationId(),
								cgm.getConversation().getGroupName(),
								mSharedPreferences.getUserId());
							mMessageDao.insert(groupCreatedMessage);

							mAppExecutors.mainThread().execute(() -> {
								mCreateGroupResult.setValue(new StateData<Conversation>().success(cgm.getConversation()));
							});
						});
						Log.e(TAG, "gml: " + cgm.getGroupMembers().toString());
						Log.e(TAG, "createNewGroup: " + response1.toString());
					}
					else if (errorLogic1 != null) {
						mCreateGroupResult.setValue(new StateData<Conversation>().errorLogic(errorLogic1));
					}

					else if (errorComm1 != null) {
						mCreateGroupResult.setValue(new StateData<Conversation>().errorComm(errorComm1));
					}

				});

			}


		});
	}

	public void updateGroupName(String groupId, String newName, Resources resources){
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.updateGroupName(groupId, newName, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					mAppExecutors.diskIO().execute(() -> {
						mConversationDao.updateGroupName(groupId, newName);
					});
				}
			});
		});
	}
	public void updateGroupImage(String groupId, byte[] bytes, Resources resources){
		mAppExecutors.networkIO().execute(() -> {

			mWochatApi.dataUploadFile(bytes, mWochatApi.UPLOAD_MIME_TYPE_IAMGE, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					try {
						String imageUrl = response.getString("url");
						String imageThumbUrl = response.getString("thumb_url");
						mWochatApi.updateGroupImage(groupId, imageUrl, imageThumbUrl, (isSuccess1, errorLogic1, errorComm1, response1) -> {
							if (isSuccess1){
								//ConversationAndItsGroupMembers cgm = handleGroupResult(response1, resources);
								mAppExecutors.diskIO().execute(() -> {
									mConversationDao.updateGroupImage(groupId, imageUrl);
								});
							}
							else if (errorLogic != null) {
							}

							else if (errorComm != null) {
							}

						});
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});



		});
	}

	public MutableLiveData<StateData<String>> uploadImage(byte[] bytes) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.dataUploadFile(bytes, mWochatApi.UPLOAD_MIME_TYPE_IAMGE, (isSuccess, errorLogic, errorComm, response) -> {
				Log.e(TAG, "OnServerResponse uploadImage - isSuccess: " + isSuccess + ", error: " + errorLogic + ", response: " + response);
				if (isSuccess) {
					try {
						String imageUrl = response.getString("url");
						String imageThumbUrl = response.getString("thumb_url");
						Picasso.get().load(imageThumbUrl).fetch(new Callback() { // pre load it
							@Override
							public void onSuccess() {
								mUploadOnlyImageResult.setValue(new StateData<String>().success(imageUrl));
							}

							@Override
							public void onError(Exception e) {
								mUploadOnlyImageResult.setValue(new StateData<String>().errorComm(e));
							}
						});

					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
				else if (errorLogic != null) {
					mUploadOnlyImageResult.setValue(new StateData<String>().errorLogic(errorLogic));
				}

				else if (errorComm != null) {
					mUploadOnlyImageResult.setValue(new StateData<String>().errorComm(errorComm));
				}
			});

		});
		return mUploadOnlyImageResult;
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

	public LiveData<List<Contact>> getServerContactsWithoutSelf() {
		return mContactDao.getServerContactsWithoutSelf(mSharedPreferences.getUserId());
	}

	public MutableLiveData<Boolean> getIsDuringRefreshContacts() {
		return mIsDuringRefreshContacts;
	}


	public LiveData<Contact> getContact(String id) {
		return mContactDao.getContactLD(id);
	}


	public LiveData<Contact> refreshContact(String participantId) {
		getContactFromServer(participantId);
		return mContactDao.getContactLD(participantId);
	}



	private Contact[] updateContactsWithLocals(Map<String, ContactServer> contactServers, Map<String, ContactLocal> localContacts) {
		Contact[] contacts = new Contact[localContacts.size()];
		int i = 0;
		for (ContactLocal contactLocal : localContacts.values()) {
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

	private void updateContactsWithLocals(Contact[] contacts, Map<String, ContactLocal> localContacts) {

		for (int i = 0; i < contacts.length; i++) {
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


	public void insertContactAndUpdateConversationContactData(ContactServer contactServer) {
		mAppExecutors.diskIO().execute(() -> {
			mDatabase.runInTransaction(() -> {
				Contact contact;
				if (mContactDao.hasContact(contactServer.getUserId())) {
					contact = mContactDao.getContact(contactServer.getUserId());
					contact.setContactServer(contactServer);
					mContactDao.updateForce(contact);
				}
				else {
					contact = new Contact(contactServer);
					mContactDao.insert(contact);
				}
				mConversationDao.updateConversationWithContactData(contact.getId(), contact.getName(), contact.getLanguage(), contact.getAvatar());
			});
		});
	}

	public void insertContacts(ContactServer[] contactsServer) {
		mAppExecutors.diskIO().execute(() -> {
			Contact contact;
			for (int i=0; i<contactsServer.length; i++) {
				if (mContactDao.hasContact(contactsServer[i].getUserId())) {
					contact = mContactDao.getContact(contactsServer[i].getUserId());
					contact.setContactServer(contactsServer[i]);
					mContactDao.updateForce(contact);
				}
				else {
					contact = new Contact(contactsServer[i]);
					mContactDao.insert(contact);
				}

				// update redundant fields
				mConversationDao.updateConversationWithContactData(contact.getId(), contact.getName(), contact.getLanguage(), contact.getAvatar());
				mMessageDao.updateMessagesActingUserWithContactName(contact.getId(), contact.getName());
				mMessageDao.updateMessagesOtherUserWithContactName(contact.getId(), contact.getName());
			}
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


	public void syncContactsLocalAndServer() {
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

	public void retrieveLocalContacts() {
		Log.e(TAG, "retrieveLocalContacts");
		RetrieveLocalContactsAsyncTask asyncTask = new RetrieveLocalContactsAsyncTask();
		asyncTask.executeOnExecutor(mAppExecutors.diskIO());
		//asyncTask.execute();

	}

	private void initSyncContactsWithServer() {
		Log.e(TAG, "initSyncContactsWithServer called");
		synchronized (mLocalContactSyncObject) {
			if (mLocalContact != null) {
				syncContactsWithServer();
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



	public void syncContactsWithServer() {
		Log.e(TAG, "syncContactsWithServer called");

		String[] contactArray = new String[mLocalContact.size()];
		int i = 0;

		for (String contact : mLocalContact.keySet()) {
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

						synchronized (mLocalContactSyncObject) {
							mAppExecutors.diskIO().execute(() -> {
								Contact[] contacts = updateContactsWithLocals(contactServersMap, mLocalContact);
								insert(contacts);
								UpdateContactLocalsToDB(mLocalContact);
								UpdateContactServerToDB(contactServersMap);
								updateConversationWithContactData(contactServersMap);
							});

//							updateConversationWithContactData(contactServersMap);
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

	private void UpdateContactServerToDB(Map<String, ContactServer> contactServersMap) {
		Log.e(TAG, "UpdateContactServerToDB - start");
		for (ContactServer contactServer : contactServersMap.values()) {
			mContactDao.update(contactServer.getUserId(),
				contactServer.getUserName(),
				contactServer.getStatus(),
				contactServer.getCountryCode(),
				contactServer.getLanguage(),
				contactServer.getProfilePicUrl(),
				contactServer.getLocation(),
				contactServer.getGender(),
				contactServer.getBirthdate(),
				contactServer.getLastUpdateDate(),
				contactServer.getDiscoverable(),
				contactServer.getOs(),
				contactServer.getLanguageLocale(),
				contactServer.getAppVersion());
		}
		Log.e(TAG, "UpdateContactServerToDB - end");

	}

	private void UpdateContactLocalsToDB(Map<String, ContactLocal> localContact) {
		Log.e(TAG, "UpdateContactLocalsToDB - start");
		for(ContactLocal contactLocal : localContact.values()){
			Contact contact = mContactDao.getContact(contactLocal.getPhoneNumStripped());
			if (contact != null)
				mContactDao.updateLocalData(contactLocal.getDisplayName(), contactLocal.getOSId(), contactLocal.getPhoneNumIso(), contactLocal.getPhoneNumStripped());
		}
		Log.e(TAG, "UpdateContactLocalsToDB - end");

	}


	public void syncContactsWithServerOld() {
		Log.e(TAG, "syncContactsWithServer called");

		String[] contactArray = new String[mLocalContact.size()];
		int i = 0;

		for (String contact : mLocalContact.keySet()) {
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

						synchronized (mLocalContactSyncObject) {
							Contact[] contacts = updateContactsWithLocals(contactServersMap, mLocalContact);
							insert(contacts);
							updateConversationWithContactData(contactServersMap);
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

	private void updateConversationWithContactData(Map<String, ContactServer> contactServersMap) {
//		mAppExecutors.diskIO().execute(() -> {
		Log.e(TAG, "updateConversationWithContactData - start");
		for(ContactServer contactServer : contactServersMap.values()){
			mConversationDao.updateParticipantData(contactServer.getUserId(), contactServer.getProfilePicUrl(), contactServer.getLanguage());
		}
		Log.e(TAG, "updateConversationWithContactData - end");
		//});
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

			synchronized (mLocalContactSyncObject) {
				mLocalContact = localContacts;
			}

		}
	}

//	public LiveData<List<ContactInvitation>> getContactInvitations() {
//		return mContactDao.getContactInvitations();
//	}
//

	public ConversationAndItsMessages getConversationAndMessagesSorted(String conversationId) {
		ConversationAndItsMessages caim = new ConversationAndItsMessages();
		caim.setConversation(mConversationDao.getConversation(conversationId));
		caim.setMessages(mMessageDao.getMessagesForConversation(conversationId));
		if (caim.getConversation().isGroup()) {
			List<GroupMember> members = mGroupDao.getMembers(conversationId);
			caim.setGroupMembers(members);
		}
		return caim;
	}

	public LiveData<List<Message>> getMessagesLD(String conversationId) {
		return mMessageDao.getMessagesForConversationLD(conversationId);
	}

	public LiveData<Conversation> getConversationLD(String conversationId) {
		return mConversationDao.getConversationLD(conversationId);
	}

	public LiveData<List<Conversation>> getConversationListLD() {
		return mConversationDao.getConversationListLD();
	}

	public Conversation getConversation(String conversationId) {
		return mConversationDao.getConversation(conversationId);
	}

	public List<ConversationAndItsGroupMembers> getAllConversationsAndItsMembers() {
		List<ConversationAndItsGroupMembers> res = new ArrayList<>();
		List<Conversation> convs = mConversationDao.getAllConversations();
		for(Conversation conversation : convs){
			ConversationAndItsGroupMembers caigm = new ConversationAndItsGroupMembers();
			caigm.setConversation(conversation);
			if(conversation.isGroup()){
				List<GroupMember> fml = mGroupDao.getMembers(conversation.getId());
				caigm.setGroupMembers(fml);
			}
			else
				caigm.setGroupMembers(null);

			res.add(caigm);
		}
		return res;
	}


	public boolean hasConversation(String conversationId) {
		return mConversationDao.hasConversation(conversationId);
	}

	public void addNewConversation(Conversation conversation) {
		mAppExecutors.diskIO().execute(() -> mConversationDao.insert(conversation));
	}

	public LiveData<List<Message>> getUnreadMessagesConversation(String conversationId) {
		return mMessageDao.getUnreadMessagesConversationLD(conversationId);
	}

	public LiveData<Integer> getUnreadConversationNum() {
		return mConversationDao.getUnreadConversationCount();
	}


	public void markAllMessagesAsRead(String conversationId) {
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

	public boolean handleIncomingMessage(Message message, Resources resources, final OnSaveMessageToDBListener listener) {
		if (message == null)
			return false;

		mAppExecutors.diskIO().execute(() -> {

			if (message.getMessageType().equals(Message.MSG_TYPE_GROUP_EVENT)) {
				handleGroupEvent(message, resources);
				listener.OnSaved(true, message, null);
				return;
			}

			Contact contact;
			String participantId = message.getSenderId();

			if (!mConversationDao.hasConversation(message.getConversationId())) {

				String selfId = mSharedPreferences.getUserId();

				if (mContactDao.hasContact(participantId)) {  // has contact, no conversation
					contact = mContactDao.getContact(participantId);
					Conversation conversation = new Conversation(message.getConversationId(), participantId, selfId);
					conversation.setParticipantName(contact.getName());
					conversation.setParticipantLanguage(contact.getLanguage());
					conversation.setParticipantProfilePicUrl(contact.getAvatar());
					message.setSenderName(contact.getName());
					if(message.isGroupMessage()){
						int color = mGroupDao.getMemberColor(message.getConversationId(), message.getSenderId());
						message.setSenderColor(color);
						conversation.setGroup(true);
						getGroupDetailsAndInsertToDB(conversation.getId(), resources);
					}
					mConversationDao.insert(conversation);
				}
				else { // no contact, no conversation
					contact = new Contact(participantId);
					mContactDao.insert(contact);
					getContactFromServer(participantId);
					Conversation conversation = new Conversation(message.getConversationId(), participantId, selfId);
					if(message.isGroupMessage()){
						int color = mGroupDao.getMemberColor(message.getConversationId(), message.getSenderId());
						message.setSenderColor(color);
						conversation.setGroup(true);
						getGroupDetailsAndInsertToDB(conversation.getId(), resources);
					}
					mConversationDao.insert(conversation);
				}
			}
			else {
				contact = mContactDao.getContact(participantId);
				if (contact == null){
					contact = new Contact(participantId);
					mContactDao.insert(contact);
					getContactFromServer(participantId);
				}
				if(message.isGroupMessage()) {
					int color = mGroupDao.getMemberColor(message.getConversationId(), message.getSenderId());
					message.setSenderColor(color);
				}
			}


			boolean res = true;
			message.setParticipantId(message.getSenderId());
			message.setRecipients(new String[]{mSharedPreferences.getUserId()});
			message.setSenderName(contact.getDisplayName());
			//String convId = Conversation.getConversationId(message.getSenderId(), message.getRecipients()[0]);
			//message.setConversationId(message.getConversationId());
			switch (message.getMessageType()) {
				case Message.MSG_TYPE_TEXT:
					message.setAckStatus(Message.ACK_STATUS_RECEIVED);
					res = handleIncomingMessageText(message);
					listener.OnSaved(res, message, contact);
					break;
				case Message.MSG_TYPE_SPEECHABLE:
					message.setAckStatus(Message.ACK_STATUS_RECEIVED);
					res = handleIncomingMessageText(message);
					listener.OnSaved(res, message, contact);
					break;
				case Message.MSG_TYPE_ACKNOWLEDGMENT:
					res = handleAcknowledgmentMessage(message);
					break;
				case Message.MSG_TYPE_TYPING_SIGNAL:
					break;
				case Message.MSG_TYPE_IMAGE:
					message.setAckStatus(Message.ACK_STATUS_RECEIVED);
					res = handleIncomingMessageImage(message);
					listener.OnSaved(res, message, contact);
					break;
				case Message.MSG_TYPE_VIDEO:
					message.setAckStatus(Message.ACK_STATUS_RECEIVED);
					res = handleIncomingMessageImage(message);
					listener.OnSaved(res, message, contact);
					break;
				case Message.MSG_TYPE_AUDIO:
					message.setAckStatus(Message.ACK_STATUS_RECEIVED);
					res = handleIncomingMessageImage(message);
					listener.OnSaved(res, message, contact);
					break;
				case Message.MSG_TYPE_GIF:
					break;
			}


		});
		return true;
	}


	private void handleGroupEvent(Message message, Resources resources){
		String groupId;
		String memberId;

		if (!mContactDao.hasContact(message.getSenderId())){
			Contact contact = new Contact(message.getSenderId());
			mContactDao.insert(contact);
		}

		if (!mConversationDao.hasConversation(message.getConversationId())) {
			Conversation conversation = new Conversation();
			conversation.setGroup(true);
			conversation.setParticipantId(null);
			conversation.setConversationId(message.getConversationId());
			conversation.setParticipantName(null);
			conversation.setParticipantLanguage(null);
			conversation.setGroupName(message.getGroupName());
			mConversationDao.insert(conversation);
			//getGroupDetailsAndInsertToDB(conversation.getId(), resources);
		}


		// handle missing users
		List<String> list = new ArrayList<>();
		Contact contact;
		String actingUser = message.getActingUser();
		String otherUser = message.getOtherUser();
		String selfId = mSharedPreferences.getUserId();

		if (Utils.isNotNullAndNotEmpty(actingUser)) {
			if (actingUser.equals(selfId)) {
				message.setActingUserName("You");
			}
			else if (mContactDao.hasContact(actingUser)) {
				contact = mContactDao.getContact(actingUser);
				message.setActingUserName(contact.getName());
			}
			else {
				contact = new Contact(actingUser);
				mContactDao.insert(contact);
				list.add(actingUser);
				message.setActingUserName(actingUser);
			}
		}

		if (Utils.isNotNullAndNotEmpty(otherUser)) {
			if (otherUser.equals(selfId)) {
				message.setOtherUserName("You");
			}
			else if (mContactDao.hasContact(otherUser)) {
				contact = mContactDao.getContact(otherUser);
				message.setOtherUserName(contact.getName());
			}
			else {
				contact = new Contact(otherUser);
				mContactDao.insert(contact);
				list.add(otherUser);
				message.setOtherUserName(otherUser);
			}
		}

		if (list.size()>0)
			getContactsFromServer(list.toArray(new String[0]));

		message.setShouldBeDisplayed(true);

		boolean conversationHasMessages = mConversationDao.hasMessages(message.getGroupId());

		mMessageDao.insert(message);

		switch (message.getEventCode()){

//			**************   inner implementation in EVENT_CODE_USER_ADDED  **********
//			case Message.EVENT_CODE_GROUP_CREATED:
//				groupId = message.getGroupId();
//				getGroupDetailsAndInsertToDB(groupId, resources);
//				break;
//			**************************************************************************

			case Message.EVENT_CODE_USER_ADDED:
				if (!conversationHasMessages) { // add Group Created msg only for new conversation, not for re-added
					Message groupCreatedMessage = Message.getGroupCreatedMessage(message);
					mMessageDao.insert(groupCreatedMessage);
				}
				groupId = message.getGroupId();
				mGroupDao.joinBackGroup(groupId);
				getGroupDetailsAndInsertToDB(groupId, resources);
				break;


			case Message.EVENT_CODE_USER_REMOVED:
				groupId = message.getGroupId();
				getGroupDetailsAndInsertToDB(groupId, resources);
				break;

			case Message.EVENT_CODE_USER_LEFT:
				groupId = message.getGroupId();
				getGroupDetailsAndInsertToDB(groupId, resources);
				break;

			case Message.EVENT_CODE_MADE_ADMIN:
				memberId = message.getOtherUser();
				groupId = message.getGroupId();
				mGroupDao.makeAdmin(groupId, memberId);
				break;

			case Message.EVENT_CODE_REMOVED_ADMIN:
				memberId = message.getOtherUser();
				groupId = message.getGroupId();
				mGroupDao.removeAdmin(groupId, memberId);
				break;

			case Message.EVENT_CODE_ICON_CHANGED:
				groupId = message.getGroupId();
				getGroupDetailsAndInsertToDB(groupId, resources);
				break;

			case Message.EVENT_CODE_NAME_CHANGED:
				groupId = message.getGroupId();
				String name = message.getGroupName();
				mConversationDao.updateGroupName(groupId, name);
				break;
		}

	}

	private boolean handleAcknowledgmentMessage(Message message) {
		mDatabase.runInTransaction(() -> {
			switch (message.getAckStatus()) {
				case Message.ACK_STATUS_SENT:
				case Message.ACK_STATUS_RECEIVED:
				case Message.ACK_STATUS_READ:
					Message originalMessage = mMessageDao.getMessageObj(message.getOriginalMessageId());
					if (originalMessage == null)
						return;
					if (originalMessage.isGroupMessage()){
						GroupMemberMessage gmm = new GroupMemberMessage();
						gmm.setAckStatus(message.getAckStatus());
						gmm.setGroupId(originalMessage.getConversationId());
						gmm.setMessageId(message.getOriginalMessageId());
						gmm.setUserId(message.getSenderId());
						mGroupDao.insert(gmm);
						List<String> ld = mGroupDao.getMembersMessagesAckStatus(message.getOriginalMessageId());
						Collections.sort(ld, Message.getAckStatusComperator());
						@Message.ACK_STATUS String ackStatus = ld.get(0);
						mMessageDao.updateAckStatus(message.getOriginalMessageId(), ackStatus);
						mConversationDao.updateLastMessageAck(message.getConversationId(), message.getOriginalMessageId(), ackStatus);
					}
					else {
						mMessageDao.updateAckStatus(message.getOriginalMessageId(), message.getAckStatus());
						mConversationDao.updateLastMessageAck(message.getConversationId(), message.getOriginalMessageId(), message.getAckStatus());
					}
					break;
			}
		});
		return true;
	}

	private boolean handleIncomingMessageText(Message message) {
		boolean res;
		String selfLang = mSharedPreferences.getUserLang();
		boolean needTranslation1 = (!message.getMessageLanguage().equals(selfLang));
		boolean needTranslationMagic = message.isMagic();

		try {
			String conversationId = message.getConversationId();
			if (mConversationDao.hasConversation(conversationId)) { // has conversation (and contact)
				res = insertMessageAndUpdateConversation(message, needTranslation1, needTranslationMagic);
			}
			else {
				String participantId = message.getSenderId();
				String selfId = mSharedPreferences.getUserId();
				if (mContactDao.hasContact(participantId)) {  // has contact, no conversation
					Contact contact = mContactDao.getContact(participantId);
					Conversation conversation = new Conversation(participantId, selfId);
					conversation.setParticipantName(contact.getName());
					conversation.setParticipantLanguage(contact.getLanguage());
					conversation.setParticipantProfilePicUrl(contact.getAvatar());
					mConversationDao.insert(conversation);
					res = insertMessageAndUpdateConversation(message, needTranslation1, needTranslationMagic);
				}
				else { // no contact, no conversation
					Contact contact = new Contact(participantId);
					mContactDao.insert(contact);
					getContactFromServer(participantId);
					Conversation conversation = new Conversation(participantId, selfId);
					mConversationDao.insert(conversation);
					res = insertMessageAndUpdateConversation(message, needTranslation1, needTranslationMagic);
				}
			}

			if ((needTranslation1) || (needTranslationMagic)) {
				translate(message, true);
			}

			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private interface TranslationResultListener{
		void onTranslationResult(Message message);
	}

	private void translate(Message message, boolean isIncoming, final TranslationResultListener listener) {
		mAppExecutors.networkIO().execute(() -> {
			Log.e("GIL", "translate: " + message.toJson());
			boolean needTranslation1, needTranslationMagic;
			String selfLang = mSharedPreferences.getUserLang();
			String fromLanguage;
			String toLanguage;
			if (isIncoming) {
				fromLanguage = message.getMessageLanguage();
				toLanguage = selfLang;
			}
			else {
				fromLanguage = message.getMessageLanguage();
				toLanguage = message.getTranslatedLanguage();
			}

			needTranslation1 = (!fromLanguage.equals(toLanguage));//gil
			needTranslationMagic = message.isMagic();

			if (needTranslation1 && needTranslationMagic) {  // translate to 2 language, regular and magic
				mWochatApi.translate2(message.getMessageId(), fromLanguage, toLanguage, message.getMessageText(),
					message.getMessageId(), fromLanguage, message.getForceTranslatedLanguage(), message.getText(),
					(isSuccess, errorLogic, errorComm, response) -> {
						if ((isSuccess) && (response != null)) {
							Log.e(TAG, "translate res: " + response.toString());
							try {
								String translatedText1 = response.getString("message");
								message.setTranslatedText(translatedText1);
								String translatedText2 = response.getString("message2");
								message.setForceTranslatedText(translatedText2);

								message.displayMessageAfterTranslation();
								Log.e("GIL", "translate result: " + message.toJson());
								listener.onTranslationResult(message);

							} catch (JSONException e) {
								e.printStackTrace();
								Log.e("GIL", "translate result error: " + message.toJson());
								listener.onTranslationResult(message);
							}

						}
						else {
							Log.e(TAG, "translate res: error");
							Log.e("GIL", "translate result: " + message.toJson());
							listener.onTranslationResult(message);
						}
					});
			}

			else if (needTranslation1) {  // regular translation only
				mWochatApi.translate(message.getMessageId(), fromLanguage, toLanguage, message.getText(),
					(isSuccess, errorLogic, errorComm, response) -> {
						if ((isSuccess) && (response != null)) {
							Log.e(TAG, "translate res: " + response.toString());
							try {
								String translatedText = response.getString("message");
								message.setTranslatedText(translatedText);
								message.displayMessageAfterTranslation();
								Log.e("GIL", "translate result: " + message.toJson());
								listener.onTranslationResult(message);

							} catch (JSONException e) {
								e.printStackTrace();
								Log.e("GIL", "translate result: " + message.toJson());
								listener.onTranslationResult(message);
							}

						}
						else {
							Log.e(TAG, "translate res: error");
							Log.e("GIL", "translate result: " + message.toJson());
							listener.onTranslationResult(message);
						}
					});
			}


			else {  // for magic only
				mWochatApi.translate(message.getMessageId(), fromLanguage, message.getForceTranslatedLanguage(), message.getText(),
					(isSuccess, errorLogic, errorComm, response) -> {
						if ((isSuccess) && (response != null)) {
							Log.e(TAG, "translate res: " + response.toString());
							try {
								String translatedText = response.getString("message");
								message.setForceTranslatedText(translatedText);
								message.displayMessageAfterTranslation();
								Log.e("GIL", "translate result: " + message.toJson());
								listener.onTranslationResult(message);

							} catch (JSONException e) {
								e.printStackTrace();
								Log.e("GIL", "translate result: " + message.toJson());
								listener.onTranslationResult(message);
							}

						}
						else {
							Log.e(TAG, "translate res: error");
							Log.e("GIL", "translate result: " + message.toJson());
							listener.onTranslationResult(message);
						}
					});
			}



		});

	}


	private void translate(Message message, boolean isIncoming) {
		mAppExecutors.networkIO().execute(() -> {
			boolean needTranslation1, needTranslationMagic;
			String selfLang = mSharedPreferences.getUserLang();
			String fromLanguage;
			String toLanguage;
			if (isIncoming) {
				fromLanguage = message.getMessageLanguage();
				toLanguage = selfLang;
			}
			else {
				fromLanguage = message.getMessageLanguage();
				toLanguage = message.getTranslatedLanguage();
			}

			needTranslation1 = (!fromLanguage.equals(toLanguage));
			needTranslationMagic = message.isMagic();

			if (needTranslation1 && needTranslationMagic) {  // translate to 2 language, regular and magic
				mWochatApi.translate2(message.getMessageId(), fromLanguage, toLanguage, message.getMessageText(),
					message.getMessageId(), fromLanguage, message.getForceTranslatedLanguage(), message.getText(),
					(isSuccess, errorLogic, errorComm, response) -> {
						if ((isSuccess) && (response != null)) {
							Log.e(TAG, "translate res: " + response.toString());
							try {
								String translatedText1 = response.getString("message");
								message.setTranslatedText(translatedText1);
								String translatedText2 = response.getString("message2");
								message.setForceTranslatedText(translatedText2);

								if (isIncoming)
									message.setTranslatedLanguage(selfLang);

								message.displayMessageAfterTranslation();

								if (isIncoming)
									updateMessageAndConversationForIncoming(message);
								else
									updateMessageTextOnly(message);


							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
						else
							Log.e(TAG, "translate res: error");
					});
			}

			else if (needTranslation1) {  // regular translation only
				mWochatApi.translate(message.getMessageId(), fromLanguage, toLanguage, message.getText(),
					(isSuccess, errorLogic, errorComm, response) -> {
						if ((isSuccess) && (response != null)) {
							Log.e(TAG, "translate res: " + response.toString());
							try {
								String translatedText = response.getString("message");
								message.setTranslatedText(translatedText);

								if (isIncoming)
									message.setTranslatedLanguage(selfLang);

								message.displayMessageAfterTranslation();

								if (isIncoming)
									updateMessageAndConversationForIncoming(message);
								else
									updateMessageTextOnly(message);


							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
						else
							Log.e(TAG, "translate res: error");
					});
			}


			else {  // for magic only
				mWochatApi.translate(message.getMessageId(), fromLanguage, message.getForceTranslatedLanguage(), message.getText(),
					(isSuccess, errorLogic, errorComm, response) -> {
						if ((isSuccess) && (response != null)) {
							Log.e(TAG, "translate res: " + response.toString());
							try {
								String translatedText = response.getString("message");
								message.setForceTranslatedText(translatedText);

								if (isIncoming)
									message.setTranslatedLanguage(selfLang);

								message.displayMessageAfterTranslation();

								if (isIncoming)
									updateMessageAndConversationForIncoming(message);
								else
									updateMessageTextOnly(message);


							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
						else
							Log.e(TAG, "translate res: error");
					});
			}



		});

	}

	private boolean handleIncomingMessageImage(Message message) {
		boolean res;
		//String selfLang = mSharedPreferences.getUserLang();
		//boolean needTranslation = (!message.getMessageLanguage().equals(selfLang));
		//boolean needTranslation = false;

		try {
			String conversationId = message.getConversationId();
			if (mConversationDao.hasConversation(conversationId)) { // has conversation (and contact)
				res = insertMessageAndUpdateConversation(message, false, false);
			}
			else {
				String participantId = message.getSenderId();
				String selfId = mSharedPreferences.getUserId();
				if (mContactDao.hasContact(participantId)) {  // has contact, no conversation
					Contact contact = mContactDao.getContact(participantId);
					Conversation conversation = new Conversation(participantId, selfId);
					conversation.setParticipantName(contact.getName());
					conversation.setParticipantLanguage(contact.getLanguage());
					conversation.setParticipantProfilePicUrl(contact.getAvatar());
					mConversationDao.insert(conversation);
					res = insertMessageAndUpdateConversation(message, false, false);
				}
				else { // no contact, no conversation
					Contact contact = new Contact(participantId);
					mContactDao.insert(contact);
					getContactFromServer(participantId);
					Conversation conversation = new Conversation(participantId, selfId);
					mConversationDao.insert(conversation);
					res = insertMessageAndUpdateConversation(message, false, false);
				}
			}


			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void updateMessageAndConversationForIncoming(Message message) {
		mAppExecutors.diskIO().execute(() -> {
			mMessageDao.update(message);
			if (message.isMagic())
				mConversationDao.updateIncomingText(message.getConversationId(), message.getForceTranslatedText());
			else
				mConversationDao.updateIncomingText(message.getConversationId(), message.getTranslatedText());
		});

	}

	private void updateMessageTextOnly(Message message) {
		mAppExecutors.diskIO().execute(() -> {
			mMessageDao.updateMessageTranslatedText(message.getMessageId(), message.getTranslatedText(), message.getForceTranslatedText());
		});

	}

	private void updateMessageOnly(Message message) {
		mAppExecutors.diskIO().execute(() -> {
			mMessageDao.update(message);
		});

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
						insertContactAndUpdateConversationContactData(contactServer);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

	}


	private void getContactsFromServer(String[] participantIds) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.userGetContacts(participantIds, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess) {
					JSONArray jsonContactArray = null;
					try {
						jsonContactArray = response.getJSONArray("contacts");
						ContactServer[] contactServerArray = new ContactServer[jsonContactArray.length()];
						for (int i=0; i<jsonContactArray.length(); i++) {
							JSONObject jsonContactObj = jsonContactArray.getJSONObject(i);
							Gson gson = new Gson();
							ContactServer contactServer = gson.fromJson(jsonContactObj.toString(), ContactServer.class);
							contactServerArray[i] = contactServer;
						}
						insertContacts(contactServerArray);

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		});

	}

	private boolean insertMessageAndUpdateConversation(Message message, boolean needTranslation1, boolean needTranslationMagic) {
		try {
			message.setShouldBeDisplayed(!needTranslation1);

			String messageText;
			if (needTranslationMagic)
				messageText = message.getForceTranslatedText();
			else if (needTranslation1)
				//messageText = "";
				messageText = message.getMessageText();
			else
				messageText = message.getMessageText();

			mMessageDao.insert(message);

			int unreadMessagesCount = mMessageDao.getUnreadMessagesCountConversation(message.getConversationId());
			mConversationDao.updateIncoming(
				message.getConversationId(),
				message.getMessageId(),
				message.getTimestampMilli(),
				messageText,
				message.getSenderId(),
				message.getSenderName(),
				message.getAckStatus(),
				message.getMessageType(),
				message.getDurationMili(),
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
	public void updateAckStatusToSent(Message message) {
		mAppExecutors.diskIO().execute(() -> {
			String seldId = mSharedPreferences.getUserId();
			if (message.isGroupMessage()){
				Log.e(TAG, "updateAckStatusToSent for group message: " + message.toJson());
				List<GroupMember> members = mGroupDao.getMembers(message.getConversationId());
				for (GroupMember groupMember : members) {
					if (!groupMember.getUserId().equals(seldId)) {
						GroupMemberMessage gmm = new GroupMemberMessage();
						gmm.setAckStatus(Message.ACK_STATUS_SENT);
						gmm.setGroupId(message.getConversationId());
						gmm.setMessageId(message.getMessageId());
						gmm.setUserId(groupMember.getUserId());
						Log.e(TAG, "updateAckStatusToSent GroupMemberMessage: " + gmm.toString());
						mGroupDao.insert(gmm);
					}
				}

			}
			else {
				Log.e(TAG, "updateAckStatusToSent: " + message.toJson());
				mMessageDao.updateAckStatusToSent(message.getMessageId());
				mConversationDao.updateLastMessageAck(message.getConversationId(), message.getMessageId(), Message.ACK_STATUS_SENT);
			}
		});

	}

	public LiveData<Message> getMessage(String messageId) {
		return mMessageDao.getMessage(messageId);
	}


	public void addNewOutgoingMessageInIOThread(Message message) {
		mAppExecutors.diskIO().execute(() -> {
			addNewOutgoingMessage(message);
		});
	}

	private void insertMessageAndConversation(Message message){
		Log.e("GIL", "insertMessageAndConversation: " + message.toJson());
		mMessageDao.insert(message);
		mConversationDao.updateOutgoing(
			message.getConversationId(),
			message.getMessageId(),
			message.getTimestampMilli(),
			message.getMessageText(),
			message.getSenderId(),
			message.getSenderName(),
			message.getAckStatus(),
			message.getMessageType(),
			message.getDurationMili());
	}

	public void addNewOutgoingMessage(Message message) {
		Log.e("GIL", "addNewOutgoingMessage: " + message.toJson());
		Log.e(TAG, "addNewOutgoingMessage: " + message.getMessageType() + " , id: " + message.getId());

		try {

			if (!mConversationDao.hasConversation(message.getConversationId())) {
				Conversation conversation = new Conversation(message.getParticipantId(), message.getSenderId());
				Contact contact = mContactDao.getContact(message.getParticipantId());
				conversation.setParticipantName(contact.getName());
				conversation.setParticipantLanguage(contact.getLanguage());
				conversation.setParticipantProfilePicUrl(contact.getAvatar());
				mConversationDao.insert(conversation);
			}

			message.showOriginalMessage();
			message.setShouldBeDisplayed(true);

			if(message.isGroupMessage()){
				message.setSenderName("You");
			}

			if (message.getMessageType().equals(Message.MSG_TYPE_TEXT)) {

				String selfLang = mSharedPreferences.getUserLang();
				boolean needTranslation1 = (message.getTranslatedLanguage() != null)&&(!message.getTranslatedLanguage().equals(selfLang));
				boolean needTranslationMagic = message.isMagic();

				if (needTranslation1 || needTranslationMagic) {
					translate(message, false, messageTranslated -> {
						mAppExecutors.diskIO().execute(() -> {
							insertMessageAndConversation(messageTranslated);
						});
					});
				}
				else {
					insertMessageAndConversation(message);
				}

			}
			else {
				insertMessageAndConversation(message);
			}

			if (message.getMessageType().equals(Message.MSG_TYPE_SPEECHABLE)) {

				String selfLang = mSharedPreferences.getUserLang();
				boolean needTranslation1 = (message.getTranslatedLanguage() != null)&&(!message.getTranslatedLanguage().equals(selfLang));
				boolean needTranslationMagic = message.isMagic();

				if (needTranslation1 || needTranslationMagic)
					translate(message, false);

			}
			else if (message.getMessageType().equals(Message.MSG_TYPE_IMAGE)) {
				if (message.getMediaLocalUri() != null) {
					byte[] bytes = ImagePickerUtil.getImageBytes(mContentResolver, Uri.parse(message.getMediaLocalUri()));
					uploadImage(message, bytes);
				}
			}
			else if (message.getMessageType().equals(Message.MSG_TYPE_VIDEO)) {

				if (message.getMediaLocalUri() != null) {
					File mediaFile = new File(new URI(message.getMediaLocalUri()));
					byte[] mediaFileBytes = Files.toByteArray(mediaFile);
					uploadVideo(message, mediaFileBytes);
				}
			}
			else if (message.getMessageType().equals(Message.MSG_TYPE_AUDIO)) {
				if (message.getMediaLocalUri() != null) {
					File mediaFile = new File(new URI(message.getMediaLocalUri()));
					byte[] mediaFileBytes = Files.toByteArray(mediaFile);
					uploadAudio(message, mediaFileBytes);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "error: message: " + message.toString());
		}

	}


	public void deleteMessages(List<Message> messages) {
		mAppExecutors.diskIO().execute(() -> {

			String conversationId = messages.get(0).getConversationId();
			mMessageDao.deleteMessages(messages);
			Message lastMessage = mMessageDao.getLastMessagesForConversation(conversationId);
			if (lastMessage != null) {
				boolean isOutgoing = lastMessage.getSenderId().equals(mSharedPreferences.getUserId());
				if (isOutgoing) {
					mConversationDao.updateOutgoing(
						conversationId,
						lastMessage.getMessageId(),
						lastMessage.getTimestampMilli(),
						lastMessage.getMessageText(),
						lastMessage.getSenderId(),
						lastMessage.getSenderName(),
						lastMessage.getAckStatus(),
						lastMessage.getMessageType(),
						lastMessage.getDurationMili());
				}
				else {
					mConversationDao.updateIncoming(
						conversationId,
						lastMessage.getMessageId(),
						lastMessage.getTimestampMilli(),
						lastMessage.getText(),
						lastMessage.getSenderId(),
						lastMessage.getSenderName(),
						lastMessage.getAckStatus(),
						lastMessage.getMessageType(),
						lastMessage.getDurationMili(),
						0);
				}
			}
		});

	}


	public void forwardMessagesToContacts(String[] contacts, ArrayList<Message> messages) {
		mAppExecutors.diskIO().execute(() -> {

			Collections.sort(messages, (m1, m2) -> (int) (m1.getTimestampMilli() - m2.getTimestampMilli()));

			for (int i = 0; i < contacts.length; i++) {
				for (Message message : messages) {
					Contact contact = mContactDao.getContact(contacts[i]);
					Message newMessage = message.generateForwardMessage(mSharedPreferences.getUserId(), contact.getId(), contact.getLanguage());
					addNewOutgoingMessage(newMessage);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
			}
		});


	}


	public LiveData<List<Message>> getOutgoingPendingMessagesLD() {
		return mMessageDao.getOutgoingPending(mSharedPreferences.getUserId());
	}


	public List<Message> getOutgoingPendingMessages() {
		return mMessageDao.getOutgoingPendingMessages(mSharedPreferences.getUserId());
	}

	public void updateUserName(String name) {
		mAppExecutors.networkIO().execute(() ->
			mWochatApi.updateUserName(name,
				(isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess) {
					mAppExecutors.diskIO().execute(() ->
						mUserDao.updateUserName(name));
				}
				else if (errorLogic != null) {
					mUserProfileEditResult.setValue(new StateData<Void>().errorLogic(errorLogic));
				}
				else if (errorComm != null) {
					mUserProfileEditResult.setValue(new StateData<Void>().errorComm(errorComm));
				}
			}));
	}

	public void updateUserStatus(String status) {
		mAppExecutors.networkIO().execute(() ->
			mWochatApi.updateUserStatus(status,
			(isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess) {
					mAppExecutors.diskIO().execute(() ->
						mUserDao.updateUserStatus(status));
				}
				else if (errorLogic != null) {
					mUserProfileEditResult.setValue(new StateData<Void>().errorLogic(errorLogic));
				}
				else if (errorComm != null) {
					mUserProfileEditResult.setValue(new StateData<Void>().errorComm(errorComm));
				}
			}));
	}

	public void uploadUpdatedProfilePic(byte[] profilePicByte) {
		mSharedPreferences.saveUserProfileImages(profilePicByte);
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.dataUploadFile(profilePicByte, mWochatApi.UPLOAD_MIME_TYPE_IAMGE,
				(isSuccess, errorLogic, errorComm, response) -> {
					if (isSuccess) {
						try {
							String imageUrl = response.getString("url");
			mWochatApi.updateUserProfilePicUrl(imageUrl,
				(isSuccess1, errorLogic1, errorComm1, response1) -> {
					if (isSuccess1) {
						mAppExecutors.diskIO().execute(() ->
								mUserDao.updateUserProfilePic(imageUrl));
					}
					else if (errorLogic1 != null) {
						mUserProfileEditResult.setValue(new StateData<Void>().errorLogic(errorLogic1));
					}
					else if (errorComm1 != null) {
						mUserProfileEditResult.setValue(new StateData<Void>().errorComm(errorComm1));
					}
				});
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
		});
	}

	public MutableLiveData<List<SupportedLanguage>> getSupportedLanguages() {
    	return  mSupportLanguages;
	}

	public void loadLanguages(String deviceLanguageCode) {
		mAppExecutors.networkIO().execute(new Runnable() {
			@Override
			public void run() {
				mWochatApi.getSupportedLanguages(deviceLanguageCode,
					(isSuccess, errorLogic, errorComm, response) -> {
						if (isSuccess) {
							try {
								JSONArray jsonSupportLanguagesArray = response.getJSONArray("languages");
								Gson gson = new Gson();
								SupportedLanguage[] languagesFromJson = gson.fromJson(jsonSupportLanguagesArray.toString(), SupportedLanguage[].class);
								List<SupportedLanguage>supportedLanguageList = Arrays.asList(languagesFromJson);
								mSupportLanguages.setValue(supportedLanguageList);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
			}
		});
	}

	public void updateUserLanguage(String languageCode) {
    	mAppExecutors.networkIO().execute(() -> {
			mWochatApi.updateUserLanguage(languageCode, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess) {
					mAppExecutors.diskIO().execute(() -> {
						mUserDao.updateUserLanguage(languageCode);
						mSharedPreferences.saveUserLanguage(languageCode);
					});

				}
				else if (errorLogic != null) {
					mUserProfileEditResult.setValue(new StateData<Void>().errorLogic(errorLogic));
				}
				else if (errorComm != null) {
					mUserProfileEditResult.setValue(new StateData<Void>().errorComm(errorComm));
				}
			});
		});
	}

	public void updateUserCountryCode(String countryCode) {
		mAppExecutors.networkIO().execute(() -> {
			   mWochatApi.updateUserCountryCode(countryCode, (isSuccess, errorLogic, errorComm, response) -> {
				       if (isSuccess) {
						   mAppExecutors.diskIO().execute(() ->
							   mUserDao.updateUserCountryCode(countryCode));
					   }
					   else if (errorLogic != null) {
						   mUserProfileEditResult.setValue(new StateData<Void>().errorLogic(errorLogic));
					   }
					   else if (errorComm != null) {
						   mUserProfileEditResult.setValue(new StateData<Void>().errorComm(errorComm));
					   }
				   });
		});
	}

	public MutableLiveData<StateData<Void>> getUserProfileEditResult() {
    	return mUserProfileEditResult;
	}
	
	
	
	public interface NotificationDataListener {
		void onNotificationDataResult(NotificationData data);
	}


	public void updateNotificationClicked(String conversationId) {
		mAppExecutors.diskIO().execute(() -> {
			mNotifDao.updateIsCanceledForConversation(conversationId);
			mNotifDao.deleteNotifications(new Date(System.currentTimeMillis() - DateUtils.WEEK_IN_MILLIS)); // delete old notifications from db
		});
	}


	public void cancelNotification(String messageId){
		mAppExecutors.diskIO().execute(() -> {
			mNotifDao.updateIsCanceled(messageId);
		});
	}



	public void getNotificationDataForGroupEvent(Message message, ContactServer contactServer, NotificationDataListener listener) {
		if ((message == null)||(!message.isGroupEvent())) {
			Log.e(TAG, "getNotificationDataForGroupEvent: null or not group event - exit ");
			return;
		}
		mAppExecutors.diskIO().execute(() -> {

			Notif notif = mNotifDao.getNotification(message.getId());
			if (notif != null){
				listener.onNotificationDataResult(null);
				return;
			}


			NotificationData data = new NotificationData();

			Contact actingUser;
			Contact otherUser;
			String actingUserId = message.getActingUser();
			String otherUserId = message.getOtherUser();
			if ((Utils.isNotNullAndNotEmpty(actingUserId))&&(!mContactDao.hasContact(actingUserId))) {
				actingUser = new Contact(actingUserId);
				mContactDao.insert(actingUser);
			}
			else {
				actingUser = mContactDao.getContact(actingUserId);
			}

			if ((Utils.isNotNullAndNotEmpty(otherUserId))&&(!mContactDao.hasContact(otherUserId))) {
				otherUser = new Contact(otherUserId);
				mContactDao.insert(otherUser);
			}
			else {
				otherUser = mContactDao.getContact(otherUserId);
			}



			data.contact = null;
			data.contactName = message.getGroupName();
			data.selfUser = mUserDao.getSelfUser();

			Conversation conversation;

			if(!mConversationDao.hasConversation(message.getConversationId())){
				conversation = new Conversation();
				conversation.setParticipantId(null);
				conversation.setConversationId(message.getConversationId());
				conversation.setParticipantName(null);
				conversation.setParticipantLanguage(null);
				conversation.setGroupName(message.getGroupName());
				try {
					Log.e(TAG, "ERROR getNotificationData: message.getConversationId: " +  message.getConversationId());
					Log.e(TAG, "ERROR getNotificationData: insert conversation: " +  conversation.toString());
					mConversationDao.insert(conversation);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				conversation = mConversationDao.getConversation(message.getConversationId());
			}

			data.conversation = conversation;
			data.title =message.getGroupName();
			data.conversationId = message.getGroupId();
			data.messageId = message.getId();


			Notif newNotif = new Notif();
			newNotif.setMessageId(data.messageId);
			newNotif.setCanceled(false);
			newNotif.setDisplayed(true);
			newNotif.setContactId(contactServer.getUserId());
			newNotif.setConversationId(data.conversationId);
			newNotif.setTimestamp(new Date());
			mNotifDao.insert(newNotif);


			message.setActingUserName(actingUser.getName());
			message.setOtherUserName(otherUser.getName());
			data.body = message.getGroupEventNotificationMessage();

			Log.e(TAG, "getNotificationData, text: " + data.body);

			mAppExecutors.mainThread().execute(() -> {
				Log.e(TAG, "getNotificationData, download: " + data.conversation.getGroupImageUrl());

				if (data.conversation.getGroupImageUrl() == null){
					Log.e(TAG, "getNotificationData, no profile image");
					data.largeIcon = null;
					listener.onNotificationDataResult(data);
					return;
				}

				try {
					Picasso.get().load(data.conversation.getGroupImageUrl()).error(R.drawable.profile_circle).into(new Target() {
						@Override
						public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
							Log.e(TAG, "getNotificationData, onBitmapLoaded");
							data.largeIcon = Utils.getCircleBitmap(bitmap);
							listener.onNotificationDataResult(data);
						}

						@Override
						public void onBitmapFailed(Exception e, Drawable errorDrawable) {
							Log.e(TAG, "getNotificationData, onBitmapFailed");
							data.largeIcon = null;
							listener.onNotificationDataResult(data);
						}

						@Override
						public void onPrepareLoad(Drawable placeHolderDrawable) {
							Log.e(TAG, "getNotificationData, onPrepareLoad");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "getNotificationData, onBitmapFailed");
					data.largeIcon = null;
					listener.onNotificationDataResult(data);
				}
			});



		});
	}


	public void getNotificationData(Message message, ContactServer contactServer, NotificationDataListener listener) {
    	if (message == null) {
			Log.e(TAG, "getNotificationData: null - exit ");
    		return;
		}
    	mAppExecutors.diskIO().execute(() -> {

			Notif notif = mNotifDao.getNotification(message.getId());
			if (notif != null){
				listener.onNotificationDataResult(null);
				return;
			}


			NotificationData data = new NotificationData();

			Contact contact;
			if (contactServer != null) {
				if (!mContactDao.hasContact(contactServer.getUserId())) {
					contact = new Contact(contactServer);
					mContactDao.insert(contact);
				}
				else {
					contact = mContactDao.getContact(contactServer.getUserId());
				}
				data.contact = contact;
				data.contactName = contact.getName();
			}
			else {
				data.contact = null;
				data.contactName = "";
			}


			data.selfUser = mUserDao.getSelfUser();

			Conversation conversation;

			if(!mConversationDao.hasConversation(message.getConversationId())){
				conversation = new Conversation();
				conversation.setConversationId(message.getConversationId());
				conversation.setParticipantId(data.contact != null?data.contact.getId():null);
				conversation.setParticipantName(data.contactName);
				conversation.setParticipantLanguage(data.contact != null?data.contact.getLanguage():null);
				conversation.setParticipantProfilePicUrl(data.contact != null?data.contact.getAvatar():null);
				try {
					Log.e(TAG, "ERROR getNotificationData: message.getConversationId: " +  message.getConversationId());
					Log.e(TAG, "ERROR getNotificationData: insert conversation: " +  conversation.toString());
					mConversationDao.insert(conversation);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				conversation = mConversationDao.getConversation(message.getConversationId());
			}

			data.conversation = conversation;

    		data.title =data.contact != null?data.contact.getDisplayName():null;
    		if (data.title == null)
				data.title = data.contact != null?data.contact.getContactServer().getUserName():null;
			if (data.title == null)
				data.title = "";

			data.conversationId = message.getConversationId();
			data.messageId = message.getId();


			Notif newNotif = new Notif();
			newNotif.setMessageId(data.messageId);
			newNotif.setCanceled(false);
			newNotif.setDisplayed(true);
			newNotif.setContactId(data.contact != null?data.contact.getId():null);
			newNotif.setConversationId(data.conversationId);
			newNotif.setTimestamp(new Date());
			mNotifDao.insert(newNotif);


			switch (message.getMessageType()){
				case Message.MSG_TYPE_GROUP_EVENT:
					data.body = message.getGroupEventNotificationMessage();
					break;
				case Message.MSG_TYPE_TEXT:
					if ((message.getTranslatedText() != null)&&(!message.getTranslatedText().isEmpty()))
						data.body = message.getTranslatedText();
					else
						data.body = message.getMessageText();
					break;

				case Message.MSG_TYPE_VIDEO:
					data.body = "\uD83D\uDCF9 Video";
					break;
				case Message.MSG_TYPE_AUDIO:
				case Message.MSG_TYPE_SPEECHABLE:
					data.body = "\uD83C\uDFA4 Audio";
					break;
				case Message.MSG_TYPE_IMAGE:
					data.body = "\uD83D\uDCF7 Image";
					break;
				case Message.MSG_TYPE_LOCATION:
					data.body = "\uD83D\uDCCD Location";
					break;
			}

			Log.e(TAG, "getNotificationData, text: " + data.body);

			mAppExecutors.mainThread().execute(() -> {
				String imageUrl;
				if (conversation.isGroup())
					imageUrl = conversation.getGroupImageUrl();
				else
					imageUrl = contactServer.getProfilePicUrl();

				Log.e(TAG, "getNotificationData, download: " + imageUrl);



				if (imageUrl == null){
					Log.e(TAG, "getNotificationData, no profile image");
					data.largeIcon = null;
					listener.onNotificationDataResult(data);
					return;
				}

				try {
					Picasso.get().load(imageUrl).error(R.drawable.profile_circle).into(new Target() {
						@Override
						public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
							Log.e(TAG, "getNotificationData, onBitmapLoaded");
							data.largeIcon = Utils.getCircleBitmap(bitmap);
							listener.onNotificationDataResult(data);
						}

						@Override
						public void onBitmapFailed(Exception e, Drawable errorDrawable) {
							Log.e(TAG, "getNotificationData, onBitmapFailed");
							data.largeIcon = null;
							listener.onNotificationDataResult(data);
						}

						@Override
						public void onPrepareLoad(Drawable placeHolderDrawable) {
							Log.e(TAG, "getNotificationData, onPrepareLoad");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "getNotificationData, onBitmapFailed");
					data.largeIcon = null;
					listener.onNotificationDataResult(data);
				}
			});



		});
	}
	

	public void clearConversation(String conversationId){
		mAppExecutors.diskIO().execute(() -> {
			mConversationDao.removeLastMessage(conversationId);
			mMessageDao.deleteMessagesFromConversation(conversationId);
		});
	}

	public LiveData<List<Message>> getMediaMessagesConversation(String conversationId) {
		return mMessageDao.getMediaMessagesConversation(conversationId);
	}


	public void updateMagicButtonLangCode(String conversationId, String langCode){
		mAppExecutors.diskIO().execute(() -> {
			mConversationDao.updateMagicButtonLangCode(conversationId, langCode);
		});
	}


	public LiveData<String> getMagicButtonLangCode(String conversationId){
		return mConversationDao.getMagicButtonLangCode(conversationId);
	}


	public void sendImageToContacts(String[] contacts, Uri photoFileUri) {
		byte[] bytes = ImagePickerUtil.getImageBytes(mContentResolver, photoFileUri);
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.dataUploadFile(bytes, mWochatApi.UPLOAD_MIME_TYPE_IAMGE, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					try {
						String imageUrl = response.getString("url");
						String imageThumbUrl = response.getString("thumb_url");
						String selfId = mSharedPreferences.getUserId();
						String selfLang = mSharedPreferences.getUserLang();

						String convId = Conversation.getConversationId(contacts[0], selfId);
						Message message = Message.CreateImageMessage(contacts[0], selfId, convId, photoFileUri, selfLang);
						message.setMediaThumbnailUrl(imageThumbUrl);
						message.setMediaUrl(imageUrl);
						ArrayList<Message> messages = new ArrayList<>();
						messages.add(message);
						forwardMessagesToContacts(contacts, messages);

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		});


	}


	public LiveData<String> getMessageGroupStatus(String messageId){
		LiveData<List<String>> ld = mGroupDao.getMembersMessagesAckStatusLD(messageId);

		LiveData<String> res = Transformations.map(ld, input -> {
			Collections.sort(input, Message.getAckStatusComperator());
			return input.get(0);
		});
		return res;
	}


	public void getAllUserGroupsDetails(Resources resources){
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.getAllUserGroupsDetails(mSharedPreferences.getUserId(),
				(isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					mAppExecutors.diskIO().execute(() -> {
						List<ConversationAndItsGroupMembers> conversationAndItsMembersList = new ArrayList<>();
						for(int i=0; i<response.length(); i++){
							try {
								JSONObject object = response.getJSONObject(i);
								ConversationAndItsGroupMembers cgm = handleGroupResult(object, resources);
								conversationAndItsMembersList.add(cgm);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						mAppExecutors.diskIO().execute(() -> {
							for(ConversationAndItsGroupMembers caigm : conversationAndItsMembersList){
								updateDBWithGroupData(caigm.getConversation().getId(), caigm);
							}
						});

					});
				}
			});
		});

	}

	public void getGroupDetailsAndInsertToDB(String groupId, Resources resources){
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.getGroupDetails(groupId, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					ConversationAndItsGroupMembers cgm = handleGroupResult(response, resources);
					mAppExecutors.diskIO().execute(() -> {
						updateDBWithGroupData(groupId, cgm);
					});
				}
			});
		});
	}

	private void updateDBWithGroupData(String groupId, ConversationAndItsGroupMembers cgm){
		List<String> contactsIds = new ArrayList<>();

		for(GroupMember groupMember : cgm.getGroupMembers()){
			if(!mContactDao.hasContact(groupMember.getUserId())){
				Contact contact = new Contact(groupMember.getUserId());
				mContactDao.insert(contact);
				contactsIds.add(groupMember.getUserId());
			}
		}

		if (mConversationDao.hasConversation(groupId)) {
			//mConversationDao.update(cgm.getConversation());
			Conversation conv = cgm.getConversation();
			mConversationDao.updateGroupData(
				groupId,
				conv.getGroupName(),
				conv.getGroupDescription(),
				conv.getGroupImageUrl(),
				conv.getGroupCreatedDate(),
				conv.getGroupCreatedBy());
		}
		else
			mConversationDao.insert(cgm.getConversation());

		mGroupDao.removeMembers(groupId); // handle the case if user where added or deleted
		mGroupDao.insert(cgm.getGroupMembers());

		if (contactsIds.size() > 0) {
			String[] contactArray = contactsIds.toArray(new String[0]);
			getContactsFromServer(contactArray);
		}
	}


	public LiveData<List<GroupMember>> getMembersLD(String groupId){
		return mGroupDao.getMembersLD(groupId);
	}


	public LiveData<List<GroupMemberContact>> getMembersContact(String groupId){
		return mGroupDao.getMembersContacts(groupId);
	}

//	public LiveData<List<GroupMemberContact>> getMembersContactOldBad(String groupId){
//		LiveData<List<GroupMember>> members = mGroupDao.getMembersLD(groupId);
//
//		LiveData<List<GroupMemberContact>> res = Transformations.map(members, input -> {
//			List<GroupMemberContact> gmcList = new ArrayList<>();
//			for(GroupMember groupMember : input){
//				GroupMemberContact gmc = new GroupMemberContact();
//				gmc.setGroupMember(groupMember);
//				Contact contact = mContactDao.getContact(groupMember.getUserId());
//				gmc.setContact(contact);
//				gmcList.add(gmc);
//			}
//			return gmcList;
//		});
//		return res;
//
//	}

	public void removeAdmin(String groupId, String memberId) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.removeAdminFromGroup(groupId, memberId, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess) {
					mAppExecutors.diskIO().execute(() -> {
						mGroupDao.removeAdmin(groupId, memberId);
					});
				}
			});
		});
	}

	public void makeAdmin(String groupId, String memberId) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.makeAdminToGroup(groupId, memberId, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess) {
					mAppExecutors.diskIO().execute(() -> {
						mGroupDao.makeAdmin(groupId, memberId);
					});
				}
			});
		});
	}

	public void removeMember(String groupId, String memberId, Resources resources) {
		mAppExecutors.networkIO().execute(() -> {
			String[] contactArray = new String[1];
			contactArray[0] = memberId;
			mWochatApi.removeContactsFromGroup(groupId, contactArray, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					ConversationAndItsGroupMembers cgm = handleGroupResult(response, resources);
					mAppExecutors.diskIO().execute(() -> {
						mGroupDao.removeMember(groupId, memberId);
					});
				}
			});
		});
	}

	public void addMembers(String groupId, String[] memberIds, Resources resources) {
		mAppExecutors.networkIO().execute(() -> {
			mWochatApi.addContactsToGroup(groupId, memberIds, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					ConversationAndItsGroupMembers cgm = handleGroupResult(response, resources);
					mAppExecutors.diskIO().execute(() -> {
						updateDBWithGroupData(groupId, cgm);
					});
				}
			});
		});
	}


	public void leaveGroup(String groupId) {
		mAppExecutors.networkIO().execute(() -> {
			String[] contactArray = new String[1];
			String selfId = mSharedPreferences.getUserId();
			contactArray[0] = selfId;
			mWochatApi.removeContactsFromGroup(groupId, contactArray, (isSuccess, errorLogic, errorComm, response) -> {
				if (isSuccess){
					mAppExecutors.diskIO().execute(() -> {
						mGroupDao.removeMember(groupId, selfId);
						mConversationDao.leaveGroup(groupId);
					});
				}
			});
		});

	}

	public LiveData<Boolean> isSelfInGroup(String groupId) {
		return mGroupDao.isSelfInGroup(groupId);
	}





}
