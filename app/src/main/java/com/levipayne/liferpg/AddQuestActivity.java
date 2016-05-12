package com.levipayne.liferpg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class AddQuestActivity extends AppCompatActivity implements DatePickerDialogFragment.DatePickerDialogListener {

    // Form elements
    private TextView mDescriptionView;
    private SeekBar mDifficultySeekbar;
    private TextView mRewardView;
    private Button mDateButton;

    private Firebase mFirebaseRef;

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

        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
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
            mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("stats").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
                        Firebase newRef = mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("quests").push();
                        String id = newRef.getKey();
                        quest.id = id;
                        newRef.setValue(quest);

                        finish();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public void onDialogDateSet(int year, int monthOfYear, int dayOfMonth) {
        ((TextView)findViewById(R.id.date_text)).setText(monthOfYear + "/" + dayOfMonth + "/" + year);
    }
}
