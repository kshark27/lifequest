package com.levipayne.lifequest.events;

import com.levipayne.lifequest.models.Quest;

/**
 * Created by Levi on 5/11/2016.
 */
public class QuestFailedEvent extends Event {
    public final static String TYPE = "QUEST_FAILED_EVENT";

    public Quest quest;

    public QuestFailedEvent(IEventDispatcher source) {
        super(TYPE, source);
    }
}
