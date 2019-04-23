package io.wochat.app.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.wochat.app.db.entity.GroupMember;


@Dao
public interface GroupDao {

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(GroupMember groupMember);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(List<GroupMember> groupMembers);


	@Query("SELECT * FROM group_memeber_table WHERE group_id =:groupId")
	LiveData<List<GroupMember>> getMembersLD(String groupId);



}
