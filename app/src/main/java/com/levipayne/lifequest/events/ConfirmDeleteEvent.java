package com.levipayne.lifequest.events;

/**
 * Created by Levi on 5/17/2016.
 */
public class ConfirmDeleteEvent extends Event {
    public final static String TYPE = "CONFIRM_DELETE_EVENT";

    public ConfirmDeleteEvent(IEventDispatcher source) {
        super(TYPE, source);
    }
}
