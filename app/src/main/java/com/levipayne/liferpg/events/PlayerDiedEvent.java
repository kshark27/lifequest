package com.levipayne.liferpg.events;

/**
 * Created by Levi on 5/21/2016.
 */
public class PlayerDiedEvent extends Event {
    public static final String TYPE = PlayerDiedEvent.class.getSimpleName();

    public PlayerDiedEvent(IEventDispatcher source) {
        super(TYPE, source);
    }
}
