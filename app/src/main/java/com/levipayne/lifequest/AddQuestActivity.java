package com.levipayne.lifequest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levipayne.lifequest.dialogs.DatePickerDialogFragment;
import com.levipayne.lifequest.models.PlayerStats;
import com.levipayne.lifequest.models.Quest;

public class AddQuestActivity extends PortraitActivity implements DatePickerDialogFragment.DatePickerDialogListener {

    // Form elements
    private TextView mDescriptionView;
    private SeekBar mDifficultySeekbar;
    private TextView mRewardView;
    private Button mDateButton;

    private DatabaseReference mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);

        mDescriptionView = (TextView) findViewById(R.id.description);
        mDifficultySeekbar = (SeekBar) findViewById(R.id.difficulty);
        mRewardView = (TextView) findViewById(R.id.reward);
        mDateButton = (Button) findViewById(R.id.date_button);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        final TextView difficultyText = (TextView) findViewById(R.id.difficulty_num);

        // Set up seekbar
        mDifficultySeekbar.setMax(Quest.MAX_DIFFICULTY - 1);
        mDifficultySeekbar.setProgress(Quest.MAX_DIFFICULTY/2);
        difficultyText.setText(mDifficultySeekbar.getProgress()+1 + "");
        mDifficultySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int adjustedProgress = progress + 1;
                difficultyText.setText(adjustedProgress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public void showDatePickerDialog() {
        DatePickerDialogFragment dialogFragment = new DatePickerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "datepicker");
    }

    public void submit(View view) {
        if (mDescriptionView.getText().toString().equals("") || mRewardView.getText().toString().equals("")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
        else {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            final String uid = auth.getCurrentUser().getUid();
            mFirebaseRef.child("users").child(uid).child("stats").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {

                        PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);

                        String description = mDescriptionView.getText().toString();
                        int difficulty = mDifficultySeekbar.getProgress() + 1;
                        int cost = Integer.valueOf(mRewardView.getText().toString());
                        int xp = Quest.calculateXpFromDifficulty(stats.level, difficulty);
                        Quest quest = new Quest(description, difficulty, cost, xp);

                        // Add due date if there is one
                        String dueDate = ((TextView)findViewById(R.id.date_text)).getText().toString();
                        if (!dueDate.equals("")) quest.dueDate = dueDate;

                        // Save quest
                        DatabaseReference newRef = mFirebaseRef.child("users").child(uid).child("quests").push();
                        String id = newRef.getKey();
                        quest.id = id;
                        newRef.setValue(quest);

                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public void onDialogDateSet(int year, int monthOfYear, int dayOfMonth) {
        ((TextView)findViewById(R.id.date_text)).setText(monthOfYear + "/" + dayOfMonth + "/" + year);
    }
}
