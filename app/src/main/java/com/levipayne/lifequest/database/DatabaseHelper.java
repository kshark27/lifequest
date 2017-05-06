package com.levipayne.lifequest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Levi on 4/28/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String CREATE_QUESTS_TABLE =
            "CREATE TABLE " + DatabaseContract.DBEntry.QUESTS_TABLE_NAME + " (" +
                    DatabaseContract.DBEntry._ID + " INTEGER PRIMARY KEY AUTO-INCREMENT," +
                    DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_DESCRIPTION +  " " + DatabaseContract.DBEntry.QUESTS_COLUMN_TYPE_DESCRIPTION + COMMA_SEP +
                    DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_DIFFICULTY + " " + DatabaseContract.DBEntry.QUESTS_COLUMN_TYPE_DIFFICULTY + COMMA_SEP +
                    DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_REWARD + " " + DatabaseContract.DBEntry.QUESTS_COLUMN_TYPE_REWARD + COMMA_SEP +
                    DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_XP + " " + DatabaseContract.DBEntry.QUESTS_COLUMN_TYPE_XP + " )";

    private static final String CREATE_REWARDS_TABLE =
            "CREATE TABLE " + DatabaseContract.DBEntry.REWARDS_TABLE_NAME + " (" +
                    DatabaseContract.DBEntry._ID + " INTEGER PRIMARY KEY AUTO-INCREMENT," +
                    DatabaseContract.DBEntry.REWARDS_COLUMN_NAME_DESCRIPTION +  " " + DatabaseContract.DBEntry.REWARDS_COLUMN_TYPE_DESCRIPTION + COMMA_SEP +
                    DatabaseContract.DBEntry.REWARDS_COLUMN_NAME_COST + " " + DatabaseContract.DBEntry.REWARDS_COLUMN_TYPE_COST + " )";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUESTS_TABLE);
        db.execSQL(CREATE_REWARDS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
//        db.execSQL(SQL_DELETE_ENTRIES);
//        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        onUpgrade(db, oldVersion, newVersion);
    }

}
