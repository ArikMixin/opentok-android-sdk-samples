package io.wochat.app.db.dao;

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.ContactLocal;


/**
 * The Room Magic is in this file, where you map a Java method call to an SQL query.
 *
 * When you are using complex data types, such as Date, you have to also supply type converters.
 * To keep this example basic, no types that require type converters are used.
 * See the documentation at
 * https://developer.android.com/topic/libraries/architecture/room.html#type-converters
 */

@Dao
public interface ContactDao {

    // LiveData is a data holder class that can be observed within a given lifecycle.
    // Always holds/caches latest version of data. Notifies its active observers when the
    // data has changed. Since we are getting all the contents of the database,
    // we are notified whenever any of the database contents have changed.
    @Query("SELECT * from contact_table ORDER BY user_id ASC")
	LiveData<List<Contact>> getContact();

	@Query("SELECT * FROM contact_table")
	public Contact[] getAllContacts();

	@Query("SELECT * FROM contact_table LIMIT 1")
	LiveData<Contact> getFirstContact();

    // We do not need a conflict strategy, because the word is our primary key, and you cannot
    // add two items with the same primary key to the database. If the table has more than one
    // column, you can use @Insert(onConflict = OnConflictStrategy.REPLACE) to update a row.
	@Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Contact contact);


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Contact[] contact);


	@Query("DELETE FROM contact_table")
    void deleteAll();

	@Query("UPDATE contact_table SET  " +
		"cntct_local_display_name = :displayName , " +
		"cntct_local_os_id = :oSId , " +
		"cntct_local_phone_num_stripped = :phoneNumStripped ," +
		"cntct_local_phone_num_iso = :phoneNumIso " +
		"WHERE user_id =:phoneNumStripped")
	void updateWithLocalInfo(String phoneNumStripped, String phoneNumIso, String displayName, String oSId);


	@Update
	void update(ContactLocal[] contactLocals);

}
