package com.levipayne.lifequest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levipayne.lifequest.dialogs.ConfirmationDialogFragment;
import com.levipayne.lifequest.events.ConfirmDeleteEvent;
import com.levipayne.lifequest.events.ConfirmRestoreEvent;
import com.levipayne.lifequest.events.Event;
import com.levipayne.lifequest.events.IEventListener;
import com.levipayne.lifequest.models.PastQuest;
import com.levipayne.lifequest.models.PlayerStats;
import com.levipayne.lifequest.models.Quest;

/**
 * This class displays information of a single quest which has already been removed from the user's active quests
 * either by completing or failing it. Gives the user an opportunity to restore quests to active status and view
 * their quest history.
 */
public class PastQuestDetailsActivity extends PortraitActivity implements IEventListener {

    private final String TAG = getClass().getSimpleName();

    private PastQuest mQuest;

    private TextView dateText;
    private FloatingActionButton deleteFab;
    private Button restoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_quest_details);

        Intent intent = this.getIntent();

        mQuest = (PastQuest) intent.getSerializableExtra("quest");
        ((TextView)findViewById(R.id.description)).setText(mQuest.description);
        ((TextView)findViewById(R.id.difficulty)).setText(String.valueOf(mQuest.difficulty));
        ((TextView)findViewById(R.id.reward)).setText(String.valueOf(mQuest.reward));

        deleteFab = (FloatingActionButton) findViewById(R.id.delete_fab);
        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
                dialogFragment.setMessage("Are you sure you want to delete this quest?");
                dialogFragment.setEvent(new ConfirmDeleteEvent(dialogFragment));
                dialogFragment.show(getSupportFragmentManager(), "DeleteConfirmation");
                dialogFragment.addEventListener(PastQuestDetailsActivity.this, ConfirmDeleteEvent.TYPE);
            }
        });

        restoreButton = (Button) findViewById(R.id.restore_button);
        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
                dialogFragment.setMessage("Are you sure you want to restore this quest?");
                dialogFragment.setEvent(new ConfirmRestoreEvent(dialogFragment));
                dialogFragment.show(getSupportFragmentManager(), "RestoreConfirmation");
                dialogFragment.addEventListener(PastQuestDetailsActivity.this, ConfirmRestoreEvent.TYPE);
            }
        });

        dateText = (TextView) findViewById(R.id.due_date_text);
        if (mQuest.dueDate != null) dateText.setText(mQuest.dueDate);
        else dateText.setText("Not set");
    }

    public void restore() {

        new AsyncTask<Void, Void, Void>() {
            boolean leveledUp;

            @Override
            protected Void doInBackground(Void... params) {
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Update player stats
                final DatabaseReference statsRef = ref.child("users").child(uid).child("stats");
                statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                            if (!mQuest.completed) stats.hp = Math.min(stats.hp + mQuest.hpLost, stats.maxHp);

                            // Save new stats
                            statsRef.setValue(stats);

                            Quest quest = new Quest(mQuest);

                            // Move quest to completed past_quests and finish activity

                            if (mQuest.completed) ref.child("users").child(uid).child("past_quests").child("completed").child(mQuest.id).removeValue();
                            else ref.child("users").child(uid).child("past_quests").child("failed").child(mQuest.id).removeValue();
                            ref.child("users").child(uid).child("quests").child(mQuest.id).setValue(quest);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (leveledUp) {
                    Toast.makeText(PastQuestDetailsActivity.this, "Leveled up!", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void delete() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference pastQuestRef = ref.child("users").child(uid).child("past_quests");
        if (mQuest.completed) pastQuestRef = pastQuestRef.child("completed").child(mQuest.id);
        else pastQuestRef = pastQuestRef.child("failed").child(mQuest.id);
        pastQuestRef.removeValue()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "delete successful");
                        finish();
                    }
                    else {
                        Log.d(TAG, "Delete failed");
                    }
                }
            });
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getEventType().equals(ConfirmDeleteEvent.TYPE)) delete();
        else if (event.getEventType().equals(ConfirmRestoreEvent.TYPE)) restore();
    }
}
