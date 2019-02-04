package io.wochat.app.db.dao;



import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import io.wochat.app.db.entity.ConversationAndItsMessages;


@Dao
public interface ConversationAndItsMessagesDao {


	@Query("SELECT * FROM conversation_table WHERE conversation_id = :conversationId")
	LiveData<ConversationAndItsMessages> getConversationAndItsMessagesLD(String conversationId);

	@Query("SELECT * FROM conversation_table WHERE conversation_id = :conversationId")
	ConversationAndItsMessages getConversationAndItsMessages(String conversationId);

}
