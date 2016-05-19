package com.levipayne.liferpg;

/**
 * Contains variables needed for post-completion (or failure) of a quest
 */
public class PastQuest extends Quest {

    public boolean completed;
    public int hpLost; // If failed

    public PastQuest() {}

    public PastQuest(Quest quest, boolean completed, int hpLost) {
        super(quest.description, quest.difficulty, quest.reward, quest.xp);
        super.id = quest.id;
        super.dueDate = quest.dueDate;

        this.completed = completed;
        this.hpLost = hpLost;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getHpLost() {
        return hpLost;
    }
}
