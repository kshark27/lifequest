package com.levipayne.liferpg;

/**
 * Created by Levi on 4/14/2016.
 */
public class Quest {
    public static final String EASY_DIFFICULTY = "EASY";
    public static final String MEDIUM_DIFFICULTY = "MEDIUM";
    public static final String HARD_DIFFICULTY = "HARD";
    public static final String LEGENDARY_DIFFICULTY = "LEGENDARY";

    String description;
    String difficulty;
    int gold;
    int xp;

    public Quest(String description, String difficulty, int gold, int xp) {
        this.description = description;
        this.difficulty = difficulty;
        this.gold = gold;
        this.xp = xp;
    }
}
