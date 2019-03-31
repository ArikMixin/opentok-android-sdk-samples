package io.wochat.app.db.dao;



import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.List;

import io.wochat.app.db.entity.Message;
import io.wochat.app.db.entity.UnreadMessagesConversation;


@Dao
public interface MessageDao {

    @Query("SELECT * from message_table ORDER BY timestamp_milli DESC")
	LiveData<List<Message>> getAllMessagesLD();

	@Query("SELECT * from message_table ORDER BY timestamp_milli DESC")
	List<Message> getAllMessages();


	@Query("SELECT * FROM message_table WHERE participant_id =:participantId ORDER BY timestamp_milli DESC")
	LiveData<List<Message>> getMessagesForParticipantLD(String participantId);

	@Query("SELECT * FROM message_table WHERE conversation_id =:conversationId AND should_be_displayed = 1 ORDER BY timestamp_milli DESC")
	LiveData<List<Message>> getMessagesForConversationLD(String conversationId);


	@Query("SELECT * FROM message_table WHERE participant_id =:participantId AND should_be_displayed = 1 ORDER BY timestamp_milli DESC")
	List<Message> getMessagesForParticipant(String participantId);

	@Query("SELECT * FROM message_table WHERE conversation_id =:conversationId AND should_be_displayed = 1 ORDER BY timestamp_milli DESC")
	List<Message> getMessagesForConversation(String conversationId);

	@Query("SELECT * FROM message_table WHERE message_id =:messageId")
	LiveData<Message> getMessage(String messageId);

	@Query("SELECT COUNT(message_id) FROM message_table WHERE " +
		"(conversation_id =:conversationId) AND " +
		"(ack_status <> 'READ') AND" +
		"(sender = participant_id)")
	int getUnreadMessagesCountConversation(String conversationId);

	@Query("SELECT * FROM message_table WHERE " +
		"(conversation_id =:conversationId) AND " +
		"(ack_status <> 'READ') AND" +
		"(sender = participant_id)")
	List<Message> getUnreadMessagesConversation(String conversationId);

	@Query("SELECT * FROM message_table WHERE " +
		"(conversation_id =:conversationId) AND " +
		"(ack_status <> 'READ') AND" +
		"(sender = participant_id)")
	LiveData<List<Message>> getUnreadMessagesConversationLD(String conversationId);


	@Query("SELECT COUNT(message_id) AS mUnreadMessagesCount, conversation_id AS mConversationId FROM message_table WHERE " +
		"(ack_status <> 'READ') AND (sender = participant_id) GROUP BY conversation_id")
	LiveData<List<UnreadMessagesConversation>> getUnreadMessagesConversation();

	@Query("SELECT COUNT(*) FROM message_table WHERE " +
		"(ack_status <> 'READ') AND (sender = participant_id) GROUP BY conversation_id")
	LiveData<Integer> getUnreadMessageCount();


	@Query("UPDATE message_table SET  ack_status = 'READ' WHERE " +
		"(conversation_id =:conversationId) AND" +
		"(ack_status <> 'READ') AND (sender = participant_id)")
	void markAllMessagesAsRead(String conversationId);


//	@Query("UPDATE message_table SET  ack_status = :ackStatus WHERE message_id =:messageId")
//	void updateAckStatus(String messageId, @Message.ACK_STATUS String ackStatus);

	@Query("UPDATE message_table " +
		"SET  ack_status = 'SENT' " +
		"WHERE (message_id =:messageId) AND (ack_status <> 'READ') AND (ack_status <> 'RECEIVED')")
	void updateAckStatusToSent(String messageId);


	@Query("UPDATE message_table " +
		"SET  ack_status = 'RECEIVED' " +
		"WHERE (message_id =:messageId) AND (ack_status <> 'READ')")
	void updateAckStatusToReceived(String messageId);

	@Query("UPDATE message_table " +
		"SET  ack_status = 'READ' " +
		"WHERE message_id =:messageId")
	void updateAckStatusToRead(String messageId);

	@Insert
    void insert(Message message);

	@Update(onConflict = OnConflictStrategy.IGNORE)
	void update(Message message);

	@Query("UPDATE message_table " +
		"SET  translated_text = :translatedText , " +
		"force_translated_text = :forceTranslatedText " +
		"WHERE message_id =:messageId")
	void updateMessageTranslatedText(String messageId, String translatedText, String forceTranslatedText);


	@Delete
	void deleteMessages(List<Message> list);

	@Query("DELETE FROM message_table WHERE conversation_id =:conversationId")
	void deleteMessagesFromConversation(String conversationId);

	@Query("SELECT * FROM message_table WHERE conversation_id =:conversationId AND should_be_displayed = 1 ORDER BY timestamp_milli DESC LIMIT 1")
	Message getLastMessagesForConversation(String conversationId);



	@Query("SELECT * FROM message_table WHERE " +
		"(ack_status = 'PENDING') AND" +
		"(sender = :selfId)")
	LiveData<List<Message>> getOutgoingPending(String selfId);


	@Query("SELECT * FROM message_table WHERE " +
		"(ack_status = 'PENDING') AND" +
		"(sender = :selfId)")
	List<Message> getOutgoingPendingMessages(String selfId);


	@Query("SELECT * FROM message_table WHERE " +
		"(conversation_id =:conversationId) AND " +
		"((message_type = 'IMAGE') OR" +
		"(message_type = 'VIDEO')) ORDER BY timestamp_milli DESC")
	LiveData<List<Message>> getMediaMessagesConversation(String conversationId);
}
