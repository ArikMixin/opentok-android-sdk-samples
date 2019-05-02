package io.wochat.app.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.GroupMemberMessage;


@Dao
public interface GroupDao {


/**************************************************************************************************/

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(GroupMember groupMember);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(List<GroupMember> groupMembers);


	@Query("SELECT * FROM group_memeber_table WHERE group_id =:groupId")
	LiveData<List<GroupMember>> getMembersLD(String groupId);

	@Query("SELECT * FROM group_memeber_table WHERE group_id =:groupId")
	List<GroupMember> getMembers(String groupId);

/**************************************************************************************************/

	@Query("SELECT * FROM group_member_message_table WHERE message_id =:messageId")
	List<GroupMemberMessage> getMembersMessages(String messageId);


	@Query("SELECT DISTINCT ack_status FROM group_member_message_table WHERE message_id =:messageId")
	List<String> getMembersMessagesAckStatus(String messageId);

	@Query("SELECT DISTINCT ack_status FROM group_member_message_table WHERE message_id =:messageId")
	LiveData<List<String>> getMembersMessagesAckStatusLD(String messageId);


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(GroupMemberMessage groupMemberMessage);

}
