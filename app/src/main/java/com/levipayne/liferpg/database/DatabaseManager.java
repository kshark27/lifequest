package com.levipayne.liferpg.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.levipayne.liferpg.Quest;
import com.levipayne.liferpg.Reward;

/**
 * Created by Levi on 4/28/2016.
 */
public class DatabaseManager {

    public static void addQuest(Context context, Quest quest) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_DESCRIPTION, quest.description);
        values.put(DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_DIFFICULTY, quest.difficulty);
        values.put(DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_REWARD, quest.reward);
        values.put(DatabaseContract.DBEntry.QUESTS_COLUMN_NAME_XP, quest.xp);

        long newRowId = db.insert(
                DatabaseContract.DBEntry.QUESTS_TABLE_NAME,
                null,
                values
        );
    }

    public static void addReward(Reward reward) {

    }

    public static void updateQuest(Quest quest) {

    }

    public static void updateReward(Reward reward) {

    }

}
