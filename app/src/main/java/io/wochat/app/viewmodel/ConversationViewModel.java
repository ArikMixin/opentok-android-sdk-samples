package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;



import java.util.ArrayList;
import java.util.List;

import io.wochat.app.AppExecutors;
import io.wochat.app.WCApplication;
import io.wochat.app.WCRepository;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.ConversationAndItsMessages;
import io.wochat.app.db.entity.ImageInfo;
import io.wochat.app.db.entity.Message;
import io.wochat.app.model.ContactOrGroup;
import io.wochat.app.model.StateData;

public class ConversationViewModel extends AndroidViewModel {

	private static final String TAG = "ConversationViewModel";
	private final WCRepository mRepository;
	private final AppExecutors mAppExecutors;



	public interface ConversationListener{
		void onConversationResult(Conversation conversation);
	}
	public interface ConversationAndItsMessagesListener{
		void onConversationAndItsMessagesResult(ConversationAndItsMessages conversationAndItsMessages);
	}

	public interface HasConversationListener{
		void onHasConversationResult(boolean hasConversation);
	}

	public ConversationViewModel(@NonNull Application application) {
		super(application);
		mRepository = ((WCApplication) application).getRepository();
		mAppExecutors = ((WCApplication) application).getAppExecutors();



	}


	public void getConversationAndMessages(String conversationId,
										   String participantId,
										   String participantProfilePicUrl,
										   String participantName,
										   String participantLang,
										   String selfId,
										   final ConversationAndItsMessagesListener lsnr){
		ConversationAndItsMessagesAsyncTask asyncTask = new ConversationAndItsMessagesAsyncTask(lsnr);
		asyncTask.execute(conversationId, participantId, participantProfilePicUrl, participantName, participantLang, selfId);
		//asyncTask.executeOnExecutor(mAppExecutors.diskIO(), conversationId, participantId, participantProfilePicUrl, participantName, selfId);
	}




	public LiveData<List<Message>> getMessagesLD(String conversationId){
		return mRepository.getMessagesLD(conversationId);
	}



	public LiveData<List<Conversation>> getConversationListLD(){
		return mRepository.getConversationListLD();
	}

	public LiveData<Conversation> getConversationLD(String id){
		return mRepository.getConversationLD(id);
	}


	public LiveData<List<Message>> getUnreadMessagesConversation(String conversationId){
		return mRepository.getUnreadMessagesConversation(conversationId);
	}


	public LiveData<Integer> getUnreadConversationNum(){
		return mRepository.getUnreadConversationNum();
	}


	public LiveData<Message> getMessage(String messageId){
		return mRepository.getMessage(messageId);
	}

	public void addNewOutcomingMessage(Message message){
		mRepository.addNewOutgoingMessageInIOThread(message);
	}

	public void sendImageToContacts(String[] contacts, Uri cameraPhotoFileUri){
		mRepository.sendImageToContacts(contacts, cameraPhotoFileUri);
	}

	public void markAllMessagesAsRead(String conversationId){
		Log.e(TAG, "markAllMessagesAsRead, conversationId: " + conversationId);
		mRepository.markAllMessagesAsRead(conversationId);
	}




	private class ConversationAndItsMessagesAsyncTask extends AsyncTask<String, Void, ConversationAndItsMessages> {

		private ConversationAndItsMessagesListener mListener;

		ConversationAndItsMessagesAsyncTask(ConversationAndItsMessagesListener lsnr) {
			mListener = lsnr;
		}

		@Override
		protected ConversationAndItsMessages doInBackground(String... params) {
			String conversationId = params[0];
			String participantId = params[1];
			String participantProfilePicUrl = params[2];
			String participantName = params[3];
			String participantLang = params[4];
			String selfId = params[5];
			boolean hasConversation = mRepository.hasConversation(conversationId);
			if (hasConversation) {
				ConversationAndItsMessages caim = mRepository.getConversationAndMessagesSorted(params[0]);
				return caim;
			}
			else {
				Conversation conversation = new Conversation(participantId, selfId);
				conversation.setParticipantProfilePicUrl(participantProfilePicUrl);
				conversation.setParticipantName(participantName);
				conversation.setParticipantLanguage(participantLang);
				mRepository.addNewConversation(conversation);
				ConversationAndItsMessages caim = new ConversationAndItsMessages();
				caim.setConversation(conversation);
				caim.setMessages(new ArrayList<>());
				return caim;
			}
		}

		@Override
		protected void onPostExecute(ConversationAndItsMessages conversationAndItsMessages) {
			mListener.onConversationAndItsMessagesResult(conversationAndItsMessages);
		}
	}

	public void deleteMessages(List<Message> messages){
		mRepository.deleteMessages(messages);
	}


	public void forwardMessagesToContacts(String[] contacts, ArrayList<Message> messages) {
		mRepository.forwardMessagesToContacts(contacts, messages);
	}

	public void forwardMessagesToContactsGroups(List<ContactOrGroup> contactOrGroupList, ArrayList<Message> messages) {
		mRepository.forwardMessagesToContactsGroups(contactOrGroupList, messages);
	}

	public LiveData<List<Message>> getOutgoingPendingMessagesLD(){
		return mRepository.getOutgoingPendingMessagesLD();
	}


	public void updateNotificationClicked(String conversationId) {
		mRepository.updateNotificationClicked(conversationId);
	}


	public void clearConversation(String conversationId){
		mRepository.clearConversation(conversationId);
	}


	public LiveData<List<Message>> getMediaMessagesConversation(String conversationId) {
		return mRepository.getMediaMessagesConversation(conversationId);
	}


	public void updateMagicButtonLangCode(String conversationId, String langCode){
		mRepository.updateMagicButtonLangCode(conversationId, langCode);
	}


	public LiveData<String> getMagicButtonLangCode(String conversationId){
		return mRepository.getMagicButtonLangCode(conversationId);
	}
}
