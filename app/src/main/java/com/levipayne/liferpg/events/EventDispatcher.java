package com.levipayne.liferpg.events;

import java.util.ArrayList;
import java.util.HashMap;

public class EventDispatcher implements IEventDispatcher {
	HashMap<String, ArrayList<IEventListener>> listeners;
	
	public EventDispatcher() {
		listeners = new HashMap<>();
	}

	@Override
	public void addEventListener(IEventListener listener, String eventType) {
		ArrayList<IEventListener> list = listeners.get(eventType);
		if (list == null) list = new ArrayList<IEventListener>();
		list.add(listener);
		listeners.put(eventType, list);
	}

	@Override
	public void removeEventListener(IEventListener listener, String eventType) {
		ArrayList<IEventListener> list = listeners.get(eventType);
		if (list != null) { 
			list.remove(listener);
			listeners.put(eventType, list);
		}
	}

	@Override
	public void dispatchEvent(Event event) {
		ArrayList<IEventListener> list = listeners.get(event.getEventType());
		if (list != null) {
			for (IEventListener l : list) {
				l.handleEvent(event);
			}
		}
	}

	@Override
	public boolean hasEventListener(IEventListener listener, String eventType) {
		ArrayList<IEventListener> list = listeners.get(eventType);
		if (list != null) {
			for (IEventListener l : list) {
				if (l.equals(listener)) return true;
			}
		}
		return false;
	}

}
