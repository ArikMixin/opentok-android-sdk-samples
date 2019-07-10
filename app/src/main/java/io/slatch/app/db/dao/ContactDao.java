package io.slatch.app.db.dao;

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

import io.slatch.app.db.entity.Contact;
//import io.wochat.app.db.entity.ContactInvitation;
import io.slatch.app.db.entity.ContactLocal;
import io.slatch.app.db.entity.Location;


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

	@Query("SELECT * from contact_table ORDER BY has_server_data DESC, cntct_local_display_name ASC")
	LiveData<List<Contact>> getContacts();

	@Query("SELECT * from contact_table WHERE has_server_data=1 ORDER BY cntct_local_display_name ASC ")
	LiveData<List<Contact>> getServerContacts();

	@Query("SELECT * from contact_table WHERE has_server_data=1 AND contact_id <> :self ORDER BY cntct_local_display_name ASC ")
	LiveData<List<Contact>> getServerContactsWithoutSelf(String self);

	@Query("SELECT * FROM contact_table")
	public Contact[] getAllContacts();

	@Query("SELECT * FROM contact_table LIMIT 1")
	LiveData<Contact> getFirstContact();

	@Query("SELECT * FROM contact_table WHERE contact_id =:id")
	LiveData<Contact> getContactLD(String id);


	@Query("SELECT EXISTS(SELECT * FROM contact_table WHERE contact_id =:id LIMIT 1)")
	public boolean hasContact(String id);

	@Query("SELECT * FROM contact_table WHERE contact_id =:id")
	Contact getContact(String id);
	

	@Query("DELETE FROM contact_table")
    void deleteAll();

//	@Query("UPDATE contact_table SET  " +
//		"cntct_local_display_name = :displayName , " +
//		"cntct_local_os_id = :oSId , " +
//		"cntct_local_phone_num_stripped = :phoneNumStripped ," +
//		"cntct_local_phone_num_iso = :phoneNumIso " +
//		"WHERE id =:phoneNumStripped")
//	void updateWithLocalInfo(String phoneNumStripped, String phoneNumIso, String displayName, String oSId);


	@Update
	void update(ContactLocal[] contactLocals);



	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insert(Contact[] contact);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Contact contact);

	@Update(onConflict = OnConflictStrategy.IGNORE)
	void update(Contact[] contact);

	@Update(onConflict = OnConflictStrategy.REPLACE)
	void updateForce(Contact contact);


	@Query("DELETE FROM contact_table WHERE contact_id =:id")
	void deleteContact(String id);

	@Query("DELETE FROM contact_local_table WHERE phone_num_stripped =:id")
	void deleteContactLocal(String id);


	@Query("UPDATE contact_table SET  " +
		"cntct_srvr_country_code = :countryCode, " +
		"cntct_srvr_user_name = :userName, " +
		"cntct_srvr_status = :status, " +
		"cntct_srvr_country_code = :countryCode, " +
		"cntct_srvr_language = :language, " +
		"cntct_srvr_profile_pic_url = :profilePicUrl, " +
		"cntct_srvr_location = :location, " +
		"cntct_srvr_gender = :gender, " +
		"cntct_srvr_birthdate = :birthdate, " +
		"cntct_srvr_last_update_date = :lastUpdateDate, " +
		"cntct_srvr_discoverable = :discoverable, " +
		"cntct_srvr_os = :os, " +
		"cntct_srvr_language_locale = :languageLocale, " +
		"cntct_srvr_app_version = :appVersion " +
		"WHERE contact_id =:id ")

	void update(String id,
				String userName,
				String status,
				String countryCode,
				String language,
				String profilePicUrl,
				Location location,
				String gender,
				String birthdate,
				Integer lastUpdateDate,
				Boolean discoverable,
				String os,
				String languageLocale,
				String appVersion);



	@Query("UPDATE contact_table SET  " +
		"cntct_local_display_name = :displayName, " +
		"cntct_local_os_id = :osId, " +
		"cntct_local_phone_num_iso = :phoneNumIso, " +
		"cntct_local_phone_num_stripped = :phoneNumStripped " +
		"WHERE contact_id =:phoneNumStripped ")
	void updateLocalData(String displayName, String osId, String phoneNumIso, String phoneNumStripped);




}
