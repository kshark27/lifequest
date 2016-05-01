package com.levipayne.liferpg;

import java.io.Serializable;

/**
 * Created by Levi on 4/14/2016.
 */
public class Quest implements Serializable {
    public static final String EASY_DIFFICULTY = "EASY";
    public static final String MEDIUM_DIFFICULTY = "MEDIUM";
    public static final String HARD_DIFFICULTY = "HARD";
    public static final String LEGENDARY_DIFFICULTY = "LEGENDARY";

    public String description;
    public String difficulty;
    public int reward;
    public int xp;

    public Quest() {}

    public Quest(String description, String difficulty, int reward, int xp) {
        this.description = description;
        this.difficulty = difficulty;
        this.reward = reward;
        this.xp = xp;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    public int getReward() {
        return this.reward;
    }

    public int getXp() {
        return this.xp;
    }
}
