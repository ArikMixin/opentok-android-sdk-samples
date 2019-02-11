/*
 * Copyright 2017, The Android Open Source Project
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

package io.wochat.app.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.Map;

import io.wochat.app.AppExecutors;
import io.wochat.app.db.converter.DateConverter;
import io.wochat.app.db.converter.LocationConverter;
import io.wochat.app.db.converter.StringArrayConverter;
import io.wochat.app.db.dao.ContactDao;
import io.wochat.app.db.dao.ContactLocalDao;
import io.wochat.app.db.dao.ConversationDao;
import io.wochat.app.db.dao.MessageDao;
import io.wochat.app.db.dao.UserDao;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.ContactLocal;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.Message;
import io.wochat.app.db.entity.User;


@Database(entities = {User.class, Contact.class, ContactLocal.class, Conversation.class, Message.class}, version = 2)
@TypeConverters({LocationConverter.class, DateConverter.class, StringArrayConverter.class})
public abstract class WCDatabase extends RoomDatabase {

	private static WCDatabase sInstance;

	@VisibleForTesting
	public static final String DATABASE_NAME = "wochat-db";


	public abstract UserDao userDao();
	public abstract ContactDao contactDao();
	public abstract ContactLocalDao contactLocalDao();
	public abstract MessageDao messageDao();
	public abstract ConversationDao conversationDao();


	private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

	public static WCDatabase getInstance(final Context context, final AppExecutors executors) {
		if (sInstance == null) {
			synchronized (WCDatabase.class) {
				if (sInstance == null) {
					sInstance = buildDatabase(context.getApplicationContext(), executors);
					sInstance.updateDatabaseCreated(context.getApplicationContext());
				}
			}
		}
		return sInstance;
	}

	/**
	 * Build the database. {@link Builder#build()} only sets up the database configuration and
	 * creates a new instance of the database.
	 * The SQLite database is only created when it's accessed for the first time.
	 */
	private static WCDatabase buildDatabase(final Context appContext,
											 final AppExecutors executors) {
		return Room.databaseBuilder(appContext, WCDatabase.class, DATABASE_NAME)
			.addCallback(new Callback() {
				@Override
				public void onCreate(@NonNull SupportSQLiteDatabase db) {
					super.onCreate(db);
					executors.diskIO().execute(() -> {
						// Add a delay to simulate a long-running operation
						//addDelay();
						// Generate the data for pre-population
						WCDatabase database = WCDatabase.getInstance(appContext, executors);
						//List<ProductEntity> products = DataGenerator.generateProducts();

						//insertData(database, products, comments);
						// notify that the database was created and it's ready to be used
						database.setDatabaseCreated();
					});
				}
			})
			.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
			.build();
	}

	/**
	 * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
	 */
	private void updateDatabaseCreated(final Context context) {
		if (context.getDatabasePath(DATABASE_NAME).exists()) {
			setDatabaseCreated();
		}
	}

	private void setDatabaseCreated(){
		mIsDatabaseCreated.postValue(true);
	}

	public static void insertUser(final WCDatabase database, final User user) {
		database.runInTransaction(() -> {
			database.userDao().insert(user);
		});
	}

	public static void insertContacts(final WCDatabase database, final Contact[] contacts) {
		database.runInTransaction(() -> {
			try {
				database.contactDao().insert(contacts);
				database.contactDao().update(contacts);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	public static void insertContactsLocal(final WCDatabase database, final ContactLocal[] contactsLocals) {
		database.runInTransaction(() -> {
			database.contactLocalDao().insert(contactsLocals);
		});
	}

//	public static void updateContactsWithLocals(WCDatabase database, ContactLocal[] contactLocals) {
//		database.runInTransaction(() -> {
//			database.contactDao().updateWithLocalInfo();
//		});
//
//	}


	private static void addDelay() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException ignored) {
		}
	}

	public LiveData<Boolean> getDatabaseCreated() {
		return mIsDatabaseCreated;
	}

	private static final Migration MIGRATION_1_2 = new Migration(1, 2) {

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
//			database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `productsFts` USING FTS4("
//				+ "`name` TEXT, `description` TEXT, content=`products`)");
//			database.execSQL("INSERT INTO productsFts (`rowid`, `name`, `description`) "
//				+ "SELECT `id`, `name`, `description` FROM products");

		}
	};

	private static final Migration MIGRATION_2_3 = new Migration(2, 3) {

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
		}
	};


//	public static void insertContactInvitation(final WCDatabase database, final ContactInvitation contactInvitation) {
//		database.runInTransaction(() -> {
//			database.contactDao().insert(contactInvitation);
//		});
//	}
}
