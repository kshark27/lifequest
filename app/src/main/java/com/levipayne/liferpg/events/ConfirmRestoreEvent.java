package com.levipayne.liferpg.events;

/**
 * Created by Levi on 5/18/2016.
 */
public class ConfirmRestoreEvent extends Event {
    public static final String TYPE = ConfirmRestoreEvent.class.getSimpleName();

    public ConfirmRestoreEvent(IEventDispatcher source) {
        super(TYPE, source);
    }
}
