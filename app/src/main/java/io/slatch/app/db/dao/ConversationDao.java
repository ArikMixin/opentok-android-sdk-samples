package io.slatch.app.db.dao;



import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.Date;
import java.util.List;

import io.slatch.app.db.entity.Conversation;


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

	@Query("SELECT EXISTS(SELECT * FROM message_table WHERE conversation_id =:conversationId LIMIT 1)")
	boolean hasMessages(String conversationId);


	@Query("SELECT EXISTS(SELECT * FROM conversation_table WHERE conversation_id =:conversationId LIMIT 1)")
	public boolean hasConversation(String conversationId);


	@Query("UPDATE conversation_table SET  " +
		"last_message_id = :lastMessageId , " +
		"last_message_timestamp = :lastMessageTimeStamp ," +
		"last_message_text = :lastMessageText ," +
		"last_message_sender_id = :lastMessageSenderId ," +
		"last_message_sender_name = :lastMessageSenderName ," +
		"last_message_ack_status = :lastMessageAckStatus ," +
		"last_message_type = :lastMessageType ," +
		"last_message_duration = :lastMessageDuration " +
		"WHERE conversation_id =:conversationId")
	void updateOutgoing(String conversationId,
						String lastMessageId,
						long lastMessageTimeStamp,
						String lastMessageText,
						String lastMessageSenderId,
						String lastMessageSenderName,
						String lastMessageAckStatus,
						String lastMessageType,
						int lastMessageDuration);


	@Query("UPDATE conversation_table SET  " +
		"last_message_id = :lastMessageId , " +
		"last_message_timestamp = :lastMessageTimeStamp ," +
		"last_message_text = :lastMessageText ," +
		"last_message_sender_id = :lastMessageSenderId ," +
		"last_message_sender_name = :lastMessageSenderName ," +
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
						String lastMessageSenderName,
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

	@Update
	void update(Conversation conversation);

	@Query("UPDATE conversation_table SET  " +
		"group_name = :groupName , " +
		"group_description = :description , " +
		"group_image_url = :image_url , " +
		"group_created_date = :created_date , " +
		"group_created_by = :created_by " +
		"WHERE conversation_id =:id" )
	void updateGroupData(String id, String groupName, String description, String image_url, Date created_date, String created_by);






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


	@Query("UPDATE conversation_table SET  " +
		"participant_profile_pic_url = :participantProfilePicUrl, " +
		"participant_language = :participantLanguage " +
		"WHERE participant_id =:participantId")
	void updateParticipantData(String participantId, String participantProfilePicUrl, String participantLanguage);


	@Query("UPDATE conversation_table SET  " +
		"participant_name = :participantName " +
		"WHERE participant_id =:participantId")
	void updateParticipantName(String participantId, String participantName);



	@Query("UPDATE conversation_table SET  " +
		"group_image_url = :imageUrl " +
		"WHERE conversation_id =:conversationId")
	void updateGroupImage(String conversationId, String imageUrl);

	@Query("UPDATE conversation_table SET  " +
		"group_name = :name " +
		"WHERE conversation_id =:conversationId")
	void updateGroupName(String conversationId, String name);


	@Query("UPDATE conversation_table SET  " +
		"is_self_in_group = 0 " +
		"WHERE conversation_id =:conversationId")
	void leaveGroup(String conversationId);


	@Query("UPDATE conversation_table SET  " +
		"is_self_in_group = 1 " +
		"WHERE conversation_id =:conversationId")
	void updateSelfInGroupTrue(String conversationId);

	@Query("UPDATE conversation_table SET  " +
		"is_self_in_group = 0 " +
		"WHERE conversation_id =:conversationId")
	void updateSelfInGroupFalse(String conversationId);


	@Query("SELECT * from conversation_table WHERE is_group = 1 ORDER BY last_message_timestamp ASC")
	public LiveData<List<Conversation>> getAllGroupConversationsLD();

	@Query("DELETE FROM conversation_table WHERE conversation_id =:conversation_id ")
	public void deleteConveration(String conversation_id);
}
