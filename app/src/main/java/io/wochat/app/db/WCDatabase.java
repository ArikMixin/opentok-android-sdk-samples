package io.wochat.app.db;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.List;

import io.wochat.app.AppExecutors;
import io.wochat.app.db.dao.WordDao;
import io.wochat.app.db.entity.Word;

public abstract class WCDatabase extends RoomDatabase {

	private static WCDatabase mInstance;

	@VisibleForTesting
	public static final String DATABASE_NAME = "wochat-db";

	public abstract WordDao wordDao();

	private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

	public static WCDatabase getInstance(final Context context, AppExecutors executors) {
		if (mInstance == null) {
			synchronized (WCDatabase.class) {
				if (mInstance == null) {
					mInstance = buildDatabase(context.getApplicationContext(), executors);
					mInstance.updateDatabaseCreated(context.getApplicationContext());
				}
			}
		}
		return mInstance;
	}

	@NonNull
	@Override
	protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
		return null;
	}

	@NonNull
	@Override
	protected InvalidationTracker createInvalidationTracker() {
		return null;
	}

	@Override
	public void clearAllTables() {

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
						addDelay();
						// Generate the data for pre-population
						WCDatabase database = WCDatabase.getInstance(appContext, executors);
						List<Word> words = DataGenerator.generateProducts();

						insertData(database, words);
						// notify that the database was created and it's ready to be used
						database.setDatabaseCreated();
					});
				}
			})
			.addMigrations(MIGRATION_1_2)
			.build();
	}

	private static void addDelay() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException ignored) {
		}
	}

	private static void insertData(final WCDatabase database, final List<Word> words) {
		database.runInTransaction(() -> {
			database.wordDao().insert(words.get(0));
		});
	}

	private void updateDatabaseCreated(final Context context) {
		if (context.getDatabasePath(DATABASE_NAME).exists()) {
			setDatabaseCreated();
		}
	}
	private void setDatabaseCreated(){
		mIsDatabaseCreated.postValue(true);
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

}
