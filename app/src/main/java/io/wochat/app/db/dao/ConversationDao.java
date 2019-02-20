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
		"last_message_type = :lastMessageType " +
		"WHERE conversation_id =:conversationId")
	void updateOutgoing(String conversationId,
						String lastMessageId,
						long lastMessageTimeStamp,
						String lastMessageText,
						String lastMessageSenderId,
						String lastMessageAckStatus,
						String lastMessageType);


	@Query("UPDATE conversation_table SET  " +
		"last_message_id = :lastMessageId , " +
		"last_message_timestamp = :lastMessageTimeStamp ," +
		"last_message_text = :lastMessageText ," +
		"last_message_sender_id = :lastMessageSenderId ," +
		"num_of_unread_messages = :numOfUnreadMessages ," +
		"last_message_ack_status = :lastMessageAckStatus ," +
		"last_message_type = :lastMessageType " +
		"WHERE conversation_id =:conversationId")
	void updateIncoming(String conversationId,
						String lastMessageId,
						long lastMessageTimeStamp,
						String lastMessageText,
						String lastMessageSenderId,
						String lastMessageAckStatus,
						String lastMessageType,
						int numOfUnreadMessages);



	@Query("UPDATE conversation_table SET  " +
		"last_message_text = :lastMessageText " +
		"WHERE conversation_id =:conversationId")
	void updateIncomingText(String conversationId,
							String lastMessageText);


//	@Query("UPDATE conversation_table SET " +
//		"num_of_unread_messages = :numOfUnreadMessages " +
//		"WHERE conversation_id =:conversationId")
//	void update(String conversationId, int numOfUnreadMessages);


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



//	@Query("SELECT conversation_table.participant_id AS mConversation, message_table.conversation_id AS unique2 "
//		+ "FROM conversation_table INNER JOIN message_table ON conversation_table..bar = Bar.id")

//	@Query("SELECT conversation_table.conversation_id AS mConvId, " +
//		"message_table.message_text AS mLastMessageText, " +
//		"message_table.sender AS mLastMessageSenderId, " +
//		"message_table.message_id AS mLastMessageId, " +
//		"message_table.timestamp AS mLastMessageTimestamp, " +
//		"message_table.ack_status AS mLastMessageAckStatus, " +
//		"contact_table.cntct_srvr_profile_pic_url AS mContactProfilePicUrl, " +
//		"contact_table.cntct_srvr_user_name AS mContactName, " +
//		"conversation_table.* " +
//		"FROM conversation_table, message_table, contact_table " +
//		"WHERE (conversation_table.conversation_id =:conversationId) AND " +
//		"(conversation_table.participant_id = contact_table.cntct_srvr_contact_server_id) AND " +
//		"(conversation_table.last_message_id = message_table.message_id)")
//	public ConversationComplete getConversationComplete(String conversationId);
//
//
//	@Query("SELECT conversation_table.conversation_id AS mConvId, " +
//		"message_table.message_text AS mLastMessageText, " +
//		"message_table.sender AS mLastMessageSenderId, " +
//		"message_table.message_id AS mLastMessageId, " +
//		"message_table.timestamp AS mLastMessageTimestamp, " +
//		"message_table.ack_status AS mLastMessageAckStatus, " +
//		"contact_table.cntct_srvr_profile_pic_url AS mContactProfilePicUrl, " +
//		"contact_table.cntct_srvr_user_name AS mContactName, " +
//		"conversation_table.* " +
//		"FROM conversation_table, message_table, contact_table " +
//		"WHERE (conversation_table.conversation_id =:conversationId) AND " +
//		"(conversation_table.participant_id = contact_table.cntct_srvr_contact_server_id) AND " +
//		"(conversation_table.last_message_id = message_table.message_id)")
//	public LiveData<ConversationComplete> getConversationCompleteLD(String conversationId);
//
//	@Query("SELECT conversation_table.conversation_id AS mConvId, " +
//		"message_table.message_text AS mLastMessageText, " +
//		"message_table.sender AS mLastMessageSenderId, " +
//		"message_table.message_id AS mLastMessageId, " +
//		"message_table.timestamp AS mLastMessageTimestamp, " +
//		"message_table.ack_status AS mLastMessageAckStatus, " +
//		"contact_table.cntct_srvr_profile_pic_url AS mContactProfilePicUrl, " +
//		"contact_table.cntct_srvr_user_name AS mContactName, " +
//		"conversation_table.* " +
//		"FROM conversation_table, message_table, contact_table " +
//		"WHERE (conversation_table.participant_id = contact_table.cntct_srvr_contact_server_id) AND " +
//		"(conversation_table.last_message_id = message_table.message_id) " +
//		"ORDER BY message_table.timestamp DESC")
//	public LiveData<List<ConversationComplete>> getConversationCompleteListLD();


	@Query("SELECT * " +
		"FROM conversation_table " +
		"ORDER BY last_message_timestamp DESC")
	public LiveData<List<Conversation>> getConversationListLD();

	@Query("UPDATE conversation_table SET  " +
		"last_message_ack_status = :ackStatus " +
		"WHERE conversation_id =:conversationId AND last_message_id =:messageId")
	void updateLastMessageAck(String conversationId, String messageId, String ackStatus);
}
