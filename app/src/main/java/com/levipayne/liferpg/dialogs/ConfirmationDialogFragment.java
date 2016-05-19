package com.levipayne.liferpg.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.levipayne.liferpg.R;
import com.levipayne.liferpg.events.ConfirmDeleteEvent;
import com.levipayne.liferpg.events.Event;
import com.levipayne.liferpg.events.EventDispatcher;
import com.levipayne.liferpg.events.IEventDispatcher;
import com.levipayne.liferpg.events.IEventListener;

/**
 * Created by Levi on 5/17/2016.
 */
public class ConfirmationDialogFragment extends DialogFragment implements IEventDispatcher {
    private String message = "Are you sure?";
    private EventDispatcher dispatcher = new EventDispatcher();
    private Event event = new Event("Event", this);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchEvent(event);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void addEventListener(IEventListener listener, String eventType) {
        dispatcher.addEventListener(listener, eventType);
    }

    @Override
    public void removeEventListener(IEventListener listener, String eventType) {
        dispatcher.removeEventListener(listener, eventType);
    }

    @Override
    public void dispatchEvent(Event event) {
        dispatcher.dispatchEvent(event);
    }

    @Override
    public boolean hasEventListener(IEventListener listener, String eventType) {
        return dispatcher.hasEventListener(listener, eventType);
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
