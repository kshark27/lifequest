package com.levipayne.lifequest.database;

import android.provider.BaseColumns;

/**
 * Created by Levi on 4/28/2016.
 */
public class DatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class DBEntry implements BaseColumns {
        // Quests Table
        public static final String QUESTS_TABLE_NAME = "Quests";
        public static final String QUESTS_COLUMN_NAME_ID = "id";
        public static final String QUESTS_COLUMN_TYPE_ID = "int";
        public static final String QUESTS_COLUMN_NAME_DESCRIPTION = "description";
        public static final String QUESTS_COLUMN_TYPE_DESCRIPTION = "char(35)";
        public static final String QUESTS_COLUMN_NAME_DIFFICULTY = "difficulty";
        public static final String QUESTS_COLUMN_TYPE_DIFFICULTY = "int";
        public static final String QUESTS_COLUMN_NAME_REWARD = "reward";
        public static final String QUESTS_COLUMN_TYPE_REWARD = "int";
        public static final String QUESTS_COLUMN_NAME_XP = "xp";
        public static final String QUESTS_COLUMN_TYPE_XP = "int";

        // Rewards Table
        public static final String REWARDS_TABLE_NAME = "Rewards";
        public static final String REWARDS_COLUMN_NAME_ID = "id";
        public static final String REWARDS_COLUMN_TYPE_ID = "int";
        public static final String REWARDS_COLUMN_NAME_DESCRIPTION = "description";
        public static final String REWARDS_COLUMN_TYPE_DESCRIPTION = "char(35)";
        public static final String REWARDS_COLUMN_NAME_COST = "cost";
        public static final String REWARDS_COLUMN_TYPE_COST = "int";
    }
}
