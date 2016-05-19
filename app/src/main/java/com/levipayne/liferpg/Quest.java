package com.levipayne.liferpg;

import java.io.Serializable;

/**
 * Created by Levi on 4/14/2016.
 */
public class Quest implements Serializable {

    public static final int MAX_DIFFICULTY = 10;

    public String id;
    public String description;
    public int difficulty;
    public int reward;
    public int xp;
    public String dueDate;

    public Quest() {}

    public Quest(String description, int difficulty, int reward, int xp) {
        this.description = description;
        this.difficulty = difficulty;
        this.reward = reward;
        this.xp = xp;
    }

    public Quest(PastQuest pQuest) {
        this.description = pQuest.description;
        this.difficulty = pQuest.difficulty;
        this.reward = pQuest.reward;
        this.xp = pQuest.xp;
        this.id = pQuest.id;
    }

    /**
     * Calculates amount of xp earned for completing a quest based on difficulty and player level
     * @param level Player's level
     * @param difficulty Difficulty of quest (1-10)
     * @return XP that should be earned for completing the quest
     */
    public static int calculateXpFromDifficulty(int level, int difficulty) {
        int x = level + difficulty - 1;
        return (int) (x * Math.log((double)x) * 5 + 5);
    }

    public String getDescription() {
        return this.description;
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public int getReward() {
        return this.reward;
    }

    public int getXp() {
        return this.xp;
    }

    public String getId() {
        return id;
    }

    public String getDueDate() {
        return dueDate;
    }
}
