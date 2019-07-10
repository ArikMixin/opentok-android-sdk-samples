package io.slatch.app.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;

import io.slatch.app.db.entity.Notif;

@Dao
public interface NotifDao {

    @Query("SELECT * from notif_table WHERE messageId =:id")
	Notif getNotification(String id);



	@Query("DELETE from notif_table WHERE timestamp < :date")
    void deleteNotifications(Date date);

	@Query("DELETE from notif_table WHERE messageId = :id")
	void deleteNotifications(String id);


	@Insert (onConflict = OnConflictStrategy.REPLACE)
	void insert(Notif notif);

	@Update(onConflict = OnConflictStrategy.REPLACE)
	void update(Notif notif);


	@Query("UPDATE notif_table SET  isDisplayed = 1 WHERE messageId =:id")
	void updateIsDisplayed(String id);

	@Query("UPDATE notif_table SET  isCanceled = 1 WHERE messageId =:id")
	void updateIsCanceled(String id);


	@Query("UPDATE notif_table SET  isCanceled = 1 WHERE conversationId =:conversationId")
	void updateIsCanceledForConversation(String conversationId);


	@Query("SELECT EXISTS(SELECT * FROM notif_table WHERE messageId =:id AND isDisplayed = 1 AND isCanceled = 0)")
	boolean isDisplayed(String id);


	@Query("SELECT EXISTS(SELECT * FROM notif_table WHERE messageId =:id AND isDisplayed = 1 AND isCanceled = 1)")
	boolean isCanceled(String id);

}
