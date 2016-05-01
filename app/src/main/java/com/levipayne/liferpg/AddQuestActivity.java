package com.levipayne.liferpg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

public class AddQuestActivity extends AppCompatActivity {

    // Form elements
    private TextView mDescriptionView;
    private Spinner mDifficultySpinner;
    private TextView mCostView;
    private Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);

        mDescriptionView = (TextView) findViewById(R.id.description);
        mDifficultySpinner = (Spinner) findViewById(R.id.difficulty);
        mCostView = (TextView) findViewById(R.id.cost);

        // Set up spinner
        String[] difficulties = {Quest.EASY_DIFFICULTY, Quest.MEDIUM_DIFFICULTY, Quest.HARD_DIFFICULTY, Quest.LEGENDARY_DIFFICULTY};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDifficultySpinner.setAdapter(adapter);

        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
    }

    public void submit(View view) {
        if (mDescriptionView.getText().toString().equals("") || mDifficultySpinner.getSelectedItem().toString().equals("")
                || mCostView.getText().toString().equals("")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
        else {
            String description = mDescriptionView.getText().toString();
            String difficulty = mDifficultySpinner.getSelectedItem().toString();
            int cost = Integer.valueOf(mCostView.getText().toString());
            int xp;
            switch (difficulty) {
                case Quest.EASY_DIFFICULTY:
                    xp = 10;
                    break;
                case Quest.MEDIUM_DIFFICULTY:
                    xp = 75;
                    break;
                case Quest.HARD_DIFFICULTY:
                    xp = 500;
                    break;
                case Quest.LEGENDARY_DIFFICULTY:
                    xp = 1000;
                    break;
                default:
                    xp = 10;
            }
            Quest quest = new Quest(description, difficulty, cost, xp);

            // Save quest
            AuthData authData = mFirebaseRef.getAuth();
            mFirebaseRef.child("users").child(authData.getUid()).child("quests").push().setValue(quest);

            Intent intent = new Intent();
            intent.putExtra("quest", quest);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
