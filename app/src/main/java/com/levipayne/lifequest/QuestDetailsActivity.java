package com.levipayne.lifequest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import com.levipayne.lifequest.dialogs.DatePickerDialogFragment;
import com.levipayne.lifequest.events.ConfirmDeleteEvent;
import com.levipayne.lifequest.events.Event;
import com.levipayne.lifequest.events.IEventListener;
import com.levipayne.lifequest.firebaseTasks.FailQuestAsyncTask;
import com.levipayne.lifequest.models.PastQuest;
import com.levipayne.lifequest.models.PlayerStats;
import com.levipayne.lifequest.models.Quest;

public class QuestDetailsActivity extends PortraitActivity implements DatePickerDialogFragment.DatePickerDialogListener, IEventListener {

    private final String TAG = getClass().getSimpleName();

    private Quest mQuest;

    private LinearLayout descriptionLayout;
    private LinearLayout difficultyLayout;
    private LinearLayout rewardLayout;
    private LinearLayout buttonLayout;
    private LinearLayout dateLayout;

    private TextView descriptionText;
    private LinearLayout difficultyStaticContent;
    private LinearLayout rewardStaticContent;
    private TextView dateText;

    private EditText descriptionEdit;
    private EditText rewardEdit;

    private View difficultySlider;

    private FloatingActionButton editFab;
    private FloatingActionButton deleteFab;

    private Button completeButton;
    private Button doneButton;
    private Button failButton;
    private Button dateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_details);

        Intent intent = this.getIntent();

        mQuest = (Quest) intent.getSerializableExtra("quest");
        ((TextView)findViewById(R.id.description)).setText(mQuest.description);
        ((TextView)findViewById(R.id.difficulty)).setText(String.valueOf(mQuest.difficulty));
        ((TextView)findViewById(R.id.reward)).setText(String.valueOf(mQuest.reward));

        editFab = (FloatingActionButton) findViewById(R.id.edit_fab);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginEdit();
            }
        });

        deleteFab = (FloatingActionButton) findViewById(R.id.delete_fab);
        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
                dialogFragment.setEvent(new ConfirmDeleteEvent(dialogFragment));
                dialogFragment.setMessage("Are you sure you want to delete this quest?");
                dialogFragment.show(getSupportFragmentManager(), "DeleteConfirmation");
                dialogFragment.addEventListener(QuestDetailsActivity.this, ConfirmDeleteEvent.TYPE);
            }
        });

        completeButton = (Button) findViewById(R.id.complete_button);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
            }
        });

        failButton = (Button) findViewById(R.id.fail_button);
        failButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fail(QuestDetailsActivity.this,mQuest);
                finish();
            }
        });

        descriptionLayout = (LinearLayout) findViewById(R.id.description_container);
        descriptionText = (TextView) findViewById(R.id.description);

        difficultyLayout = (LinearLayout) findViewById(R.id.difficulty_container);
        difficultyStaticContent = (LinearLayout) findViewById(R.id.inner_difficulty_container);

        rewardLayout = (LinearLayout) findViewById(R.id.inner_reward_container);
        rewardStaticContent = (LinearLayout) findViewById(R.id.static_inner_reward_container);
        rewardEdit = (EditText) findViewById(R.id.reward_edit);

        doneButton = (Button) findViewById(R.id.done_button);
        dateButton = (Button) findViewById(R.id.date_button);

        dateLayout = (LinearLayout) findViewById(R.id.inner_date_container);
        dateText = (TextView) findViewById(R.id.due_date_text);
        if (mQuest.dueDate != null) dateText.setText(mQuest.dueDate);
        else dateText.setText("Not set");

        completeButton = (Button) findViewById(R.id.complete_button);
        buttonLayout = (LinearLayout) findViewById(R.id.button_container);
    }

    /**
     * Called when Fail button is pressed. Indicates the player failed the quest and will take damage to their hp
     */
    public static void fail(final PortraitActivity activity, final Quest quest) {
        MainActivity.showLoadingDialog(activity);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update player stats
        final DatabaseReference statsRef = ref.child("users").child(uid).child("stats");
        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                    int hpLost = FailQuestAsyncTask.calculateHpLost(stats.hp, quest.difficulty);
                    stats.hp -= hpLost;

                    // Save new stats
                    dataSnapshot.getRef().removeValue();
                    dataSnapshot.getRef().setValue(stats);

                    PastQuest pQuest = new PastQuest(quest, false, hpLost);

                    // Move quest to failed past_quests and finish activity
                    ref.child("users").child(uid).child("quests").child(quest.id).removeValue();
                    ref.child("users").child(uid).child("past_quests").child("failed").child(pQuest.id).setValue(pQuest);

                    // Set last_health_regen to current time (if null) so regeneration will work
                    final DatabaseReference lastRegenRef = ref.child("users").child(uid).child("last_health_regen");
                    lastRegenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) lastRegenRef.setValue(System.currentTimeMillis());
                            MainActivity.hideLoadingDialog(activity);
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            MainActivity.hideLoadingDialog(activity);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void complete() {
        MainActivity.showLoadingDialog(this);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update player stats
        final DatabaseReference statsRef = ref.child("users").child(uid).child("stats");
        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    boolean leveledUp;
                    PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                    stats.gold += mQuest.reward;
                    stats.xp += mQuest.xp;

                    // Check if player leveled up
                    int max = PlayerStats.getNextXpGoal(stats.level);
                    leveledUp = false;
                    while (stats.xp >= max) {
                        leveledUp = true;
                        stats.xp -= max;
                        stats.level++;
                        stats.maxHp++;
                        stats.hp++;
                        max = PlayerStats.getNextXpGoal(stats.level);
                    }

                    // Save new stats
                    statsRef.setValue(stats);

                    PastQuest pQuest = new PastQuest(mQuest, true, 0);

                    // Move quest to completed past_quests and finish activity
                    ref.child("users").child(uid).child("quests").child(mQuest.id).removeValue();
                    ref.child("users").child(uid).child("past_quests").child("completed").child(pQuest.id).setValue(pQuest);

                    if (leveledUp) {
                        Toast.makeText(QuestDetailsActivity.this, "Leveled up!", Toast.LENGTH_LONG).show();
                    }

                    MainActivity.hideLoadingDialog(QuestDetailsActivity.this);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }


    public void delete() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref.child("users").child(uid).child("quests").child(mQuest.id).removeValue()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "delete successful");
                        finish();
                    }
                    else {
                        Log.d(TAG, "delete failed");
                    }
                }
            });
    }

    public void beginEdit() {
        descriptionEdit = new EditText(this);
        descriptionEdit.setText(descriptionText.getText());
        descriptionLayout.addView(descriptionEdit);
        descriptionText.setVisibility(View.GONE);

        difficultySlider = getLayoutInflater().inflate(R.layout.difficulty_slider_layout, null);
        SeekBar sBar = (SeekBar) difficultySlider.findViewById(R.id.progress_slider);
        sBar.setMax(Quest.MAX_DIFFICULTY - 1);
        sBar.setProgress(mQuest.difficulty - 1);
        final TextView dText = (TextView) difficultySlider.findViewById(R.id.difficulty_text);
        dText.setText(mQuest.difficulty + "");
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int adjusted = progress + 1;
                dText.setText(adjusted + "");
                mQuest.difficulty = adjusted;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        difficultyLayout.addView(difficultySlider);
        difficultyStaticContent.setVisibility(View.GONE);

        rewardLayout.setVisibility(View.VISIBLE);
        rewardEdit.setText(((TextView)findViewById(R.id.reward)).getText());
        rewardStaticContent.setVisibility(View.GONE);

        dateButton.setVisibility(View.VISIBLE);
        dateButton.setText(getResources().getString(R.string.date_button));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialogFragment dialogFragment = new DatePickerDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        editFab.setVisibility(View.GONE);
        deleteFab.setVisibility(View.GONE);
        failButton.setVisibility(View.GONE);

        completeButton.setVisibility(View.GONE);

        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishEdit();
            }
        });
    }

    public void finishEdit() {
        mQuest.description = descriptionEdit.getText().toString();
        mQuest.reward = Integer.valueOf(rewardEdit.getText().toString());

        String dueDate = dateText.getText().toString();
        if (!dueDate.equals("") && !dueDate.equals("Not set")) mQuest.dueDate = dueDate;

        // Update Quest
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref.child("users").child(uid).child("quests").child(mQuest.id).setValue(mQuest);

        descriptionEdit.setVisibility(View.GONE);
        difficultySlider.setVisibility(View.GONE);
        rewardLayout.setVisibility(View.GONE);
        doneButton.setVisibility(View.GONE);
        dateButton.setVisibility(View.GONE);

        descriptionText.setVisibility(View.VISIBLE);
        difficultyStaticContent.setVisibility(View.VISIBLE);
        rewardStaticContent.setVisibility(View.VISIBLE);
        completeButton.setVisibility(View.VISIBLE);
        editFab.setVisibility(View.VISIBLE);
        deleteFab.setVisibility(View.VISIBLE);
        failButton.setVisibility(View.VISIBLE);

        descriptionText.setText(mQuest.description);
        ((TextView)findViewById(R.id.difficulty)).setText(String.valueOf(mQuest.difficulty));
        ((TextView)findViewById(R.id.reward)).setText(String.valueOf(mQuest.reward));
    }

    @Override
    public void onDialogDateSet(int year, int monthOfYear, int dayOfMonth) {
        dateText.setText(monthOfYear + "/" + dayOfMonth + "/" + year);
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getEventType().equals(ConfirmDeleteEvent.TYPE)) delete();
    }
}
