package com.levipayne.lifequest.models;

import com.google.firebase.database.ThrowOnExtraProperties;

import java.io.Serializable;

/**
 * Contains variables needed for post-completion (or failure) of a quest
 * Note: not a subclass because Firebase won't allow it
 */
@ThrowOnExtraProperties
public class PastQuest implements Serializable {

    // Quest attributes
    public String id;
    public String description;
    public int difficulty;
    public int reward;
    public int xp;
    public String dueDate;

    // Extra attributes
    public boolean completed;
    public int hpLost; // If failed

    public PastQuest() {}

    public PastQuest(Quest quest, boolean completed, int hpLost) {
        this.description = quest.description;
        this.difficulty = quest.difficulty;
        this.reward = quest.reward;
        this.xp = quest.xp;
        this.id = quest.id;
        this.dueDate = quest.dueDate;

        this.completed = completed;
        this.hpLost = hpLost;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getHpLost() {
        return hpLost;
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
