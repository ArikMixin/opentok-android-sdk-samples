package io.wochat.app.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.GroupMemberContact;
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

	@Query("SELECT color FROM group_memeber_table WHERE group_id =:groupId AND user_id =:userId")
	int getMemberColor(String groupId, String userId);

	/**************************************************************************************************/

	@Query("SELECT * FROM group_member_message_table WHERE message_id =:messageId")
	List<GroupMemberMessage> getMembersMessages(String messageId);


	@Query("SELECT DISTINCT ack_status FROM group_member_message_table WHERE message_id =:messageId")
	List<String> getMembersMessagesAckStatus(String messageId);

	@Query("SELECT DISTINCT ack_status FROM group_member_message_table WHERE message_id =:messageId")
	LiveData<List<String>> getMembersMessagesAckStatusLD(String messageId);


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(GroupMemberMessage groupMemberMessage);


	@Query("SELECT contact_table.*, group_memeber_table.* " +
		"FROM contact_table INNER JOIN group_memeber_table " +
		"ON contact_table.contact_id = user_id " +
		"WHERE group_id =:groupId")
	LiveData<List<GroupMemberContact>> getMembersContacts(String groupId);

	@Query("UPDATE group_memeber_table " +
		"SET  is_admin = 0 " +
		"WHERE group_id =:groupId AND user_id =:memberId")
	void removeAdmin(String groupId, String memberId);

	@Query("UPDATE group_memeber_table " +
		"SET  is_admin = 1 " +
		"WHERE group_id =:groupId AND user_id =:memberId")
	void makeAdmin(String groupId, String memberId);

	@Query("DELETE FROM group_memeber_table " +
		"WHERE group_id =:groupId AND user_id =:memberId")
	void removeMember(String groupId, String memberId);

	@Query("SELECT is_self_in_group " +
		"FROM conversation_table  " +
		"WHERE conversation_id =:groupId")
	LiveData<Boolean> isSelfInGroup(String groupId);

	@Query("DELETE FROM group_memeber_table " +
		"WHERE group_id =:groupId")
	void removeMembers(String groupId);

	@Query("UPDATE conversation_table SET  " +
		"is_self_in_group = 1 " +
		"WHERE conversation_id =:groupId")
	void joinBackGroup(String groupId);




}
