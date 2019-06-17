package io.wochat.app.db.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.wochat.app.db.entity.Call;

@Dao
public interface CallDao {

    @Query("SELECT * from call_table ORDER BY call_start_timestamp ASC")
	public LiveData<List<Call>> getAllCallsLD();

	@Insert
	void insert(Call call);

	@Query("UPDATE call_table SET  " +
			"call_state = :callState , " +
			"call_duration = :callDuration  " +
			"WHERE call_id =:callID ")
	public void updateCall(Integer callID , String callDuration, String callState);


}
