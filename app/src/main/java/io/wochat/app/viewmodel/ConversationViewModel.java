package io.wochat.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
import io.wochat.app.db.entity.Message;

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

//	public void getConversation(String conversationId, final ConversationListener listener){
//		GetConversationAsyncTask task = new GetConversationAsyncTask(listener);
//		task.execute(conversationId);
//	}


	public LiveData<List<Message>> getMessagesLD(String conversationId){
		return mRepository.getMessagesLD(conversationId);
	}

//	public LiveData<Conversation> getConversationLD(String conversationId){
//		return mRepository.getConversationLD(conversationId);
//	}

	public LiveData<List<Conversation>> getConversationListLD(){
		return mRepository.getConversationListLD();
	}

//	public MutableLiveData<List<Message>> getMarkAsReadAffectedMessages(){
//		return mRepository.getMarkAsReadAffectedMessages();
//	}

	public LiveData<List<Message>> getUnreadMessagesConversation(String conversationId){
		return mRepository.getUnreadMessagesConversation(conversationId);
	}
//	public LiveData<ConversationComplete> getConversationCompleteLD(String conversationId){
//		return mRepository.getConversationCompleteLD(conversationId);
//	}
//
//	public LiveData<List<ConversationComplete>> getConversationCompleteListLD(){
//		return mRepository.getConversationCompleteListLD();
//	}
//
//
//	public MediatorLiveData getMediatorConversationLiveData(){
//		return mRepository.getMediatorConversationLiveData();
//	}

//	public void hasConversation(String conversationId, HasConversationListener listener){
//		HasConversationAsyncTask asyncTask = new HasConversationAsyncTask(listener);
//		asyncTask.execute(conversationId);
//	}

//	public void addNewConversation(Conversation conversation){
//		mRepository.addNewConversation(conversation);
//	}

	public void addNewOutcomingMessage(Message message){
		mRepository.addNewOutgoingMessage(message);
	}

	public void markAllMessagesAsRead(String conversationId){
		Log.e(TAG, "markAllMessagesAsRead, conversationId: " + conversationId);
		mRepository.markAllMessagesAsRead(conversationId);
	}


//	private class GetConversationAsyncTask extends AsyncTask<String, Void, Conversation> {
//
//		private ConversationListener mConversationListener;
//
//		GetConversationAsyncTask(ConversationListener lsnr) {
//			mConversationListener = lsnr;
//		}
//
//		@Override
//		protected Conversation doInBackground(String... params) {
//			Conversation conversation = mRepository.getConversation(params[0]);
//			return conversation;
//		}
//
//		@Override
//		protected void onPostExecute(Conversation conversation) {
//			mConversationListener.onConversationResult(conversation);
//		}
//	}


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

//	private class HasConversationAsyncTask extends AsyncTask<String, Void, Boolean> {
//
//		private HasConversationListener mListener;
//
//		HasConversationAsyncTask(HasConversationListener lsnr) {
//			mListener = lsnr;
//		}
//
//		@Override
//		protected Boolean doInBackground(String... params) {
//			Boolean b = mRepository.hasConversation(params[0]);
//			return b;
//		}
//
//		@Override
//		protected void onPostExecute(Boolean hasConversation) {
//			mListener.onHasConversationResult(hasConversation);
//		}
//	}


}
