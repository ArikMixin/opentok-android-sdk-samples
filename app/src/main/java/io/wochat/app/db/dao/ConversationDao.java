package io.wochat.app.db.dao;



import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import java.util.List;

import io.wochat.app.db.entity.Conversation;


@Dao
public interface ConversationDao {

    @Query("SELECT * from conversation_table ORDER BY last_message_timestamp ASC")
	public LiveData<List<Conversation>> getAllConversationsLD();

	@Query("SELECT * from conversation_table ORDER BY last_message_timestamp ASC")
	public List<Conversation> getAllConversations();


	@Query("SELECT * FROM conversation_table WHERE conversation_id =:conversationId LIMIT 1")
	public LiveData<Conversation> getConversationLD(String conversationId);

	@Query("SELECT * FROM conversation_table WHERE conversation_id =:conversationId LIMIT 1")
	public Conversation getConversation(String conversationId);


	@Query("UPDATE conversation_table SET  " +
		"participant_name = :contactName , " +
		"participant_language = :contactLanguage , " +
		"participant_profile_pic_url = :profilePic " +
		"WHERE participant_id =:contactId")
	public void updateConversationWithContactData(String contactId, String contactName, String contactLanguage, String profilePic);



	@Query("SELECT EXISTS(SELECT * FROM conversation_table WHERE conversation_id =:conversationId LIMIT 1)")
	public boolean hasConversation(String conversationId);


	@Query("UPDATE conversation_table SET  " +
		"last_message_id = :lastMessageId , " +
		"last_message_timestamp = :lastMessageTimeStamp ," +
		"last_message_text = :lastMessageText ," +
		"last_message_sender_id = :lastMessageSenderId ," +
		"last_message_ack_status = :lastMessageAckStatus ," +
		"last_message_type = :lastMessageType ," +
		"last_message_duration = :lastMessageDuration " +
		"WHERE conversation_id =:conversationId")
	void updateOutgoing(String conversationId,
						String lastMessageId,
						long lastMessageTimeStamp,
						String lastMessageText,
						String lastMessageSenderId,
						String lastMessageAckStatus,
						String lastMessageType,
						int lastMessageDuration);


	@Query("UPDATE conversation_table SET  " +
		"last_message_id = :lastMessageId , " +
		"last_message_timestamp = :lastMessageTimeStamp ," +
		"last_message_text = :lastMessageText ," +
		"last_message_sender_id = :lastMessageSenderId ," +
		"num_of_unread_messages = :numOfUnreadMessages ," +
		"last_message_ack_status = :lastMessageAckStatus ," +
		"last_message_type = :lastMessageType ," +
		"last_message_duration = :lastMessageDuration " +
		"WHERE conversation_id =:conversationId")
	void updateIncoming(String conversationId,
						String lastMessageId,
						long lastMessageTimeStamp,
						String lastMessageText,
						String lastMessageSenderId,
						String lastMessageAckStatus,
						String lastMessageType,
						int lastMessageDuration,
						int numOfUnreadMessages);



	@Query("UPDATE conversation_table SET  " +
		"last_message_id = NULL , " +
		"last_message_timestamp = 0 ," +
		"last_message_text = '' ," +
		"last_message_sender_id = NULL ," +
		"num_of_unread_messages = 0 ," +
		"last_message_ack_status = NULL ," +
		"last_message_type = NULL ," +
		"last_message_duration = 0 " +
		"WHERE conversation_id =:conversationId")
	void removeLastMessage(String conversationId);



	@Query("UPDATE conversation_table SET  " +
		"last_message_text = :lastMessageText " +
		"WHERE conversation_id =:conversationId")
	void updateIncomingText(String conversationId,
							String lastMessageText);



	@Query("UPDATE conversation_table SET " +
		"num_of_unread_messages = 0 " +
		"WHERE conversation_id =:conversationId")
	void markAllMessagesAsRead(String conversationId);



	@Query("SELECT COUNT(*) FROM conversation_table WHERE " +
		"num_of_unread_messages > 0")
	LiveData<Integer> getUnreadConversationCount();

	//numOfUnreadMessages


	@Insert
    void insert(Conversation conversation);



	@Query("SELECT * " +
		"FROM conversation_table " +
		"ORDER BY last_message_timestamp DESC")
	public LiveData<List<Conversation>> getConversationListLD();

	@Query("UPDATE conversation_table SET  " +
		"last_message_ack_status = :ackStatus " +
		"WHERE conversation_id =:conversationId AND last_message_id =:messageId")
	void updateLastMessageAck(String conversationId, String messageId, String ackStatus);


	@Query("SELECT magic_button_lang_code " +
		"FROM conversation_table " +
		"WHERE conversation_id =:conversationId")
	LiveData<String> getMagicButtonLangCode(String conversationId);


	@Query("UPDATE conversation_table SET  " +
		"magic_button_lang_code = :langCode " +
		"WHERE conversation_id =:conversationId")
	void updateMagicButtonLangCode(String conversationId, String langCode);
}
