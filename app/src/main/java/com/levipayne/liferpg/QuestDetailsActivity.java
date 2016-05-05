package com.levipayne.liferpg;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class QuestDetailsActivity extends AppCompatActivity {

    private Quest mQuest;
    private LinearLayout descriptionLayout;
    private LinearLayout difficultyLayout;
    private LinearLayout rewardLayout;
    private LinearLayout buttonLayout;
    private TextView descriptionText;
    private TextView difficultyText;
    private TextView rewardText;
    private EditText descriptionEdit;
    private Spinner difficultySpinner;
    private EditText rewardEdit;
    private FloatingActionButton editFab;
    private FloatingActionButton deleteFab;
    private Button completeButton;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_details);

        Intent intent = this.getIntent();
        mQuest = (Quest) intent.getSerializableExtra("quest");
        ((TextView)findViewById(R.id.description)).setText(mQuest.description);
        ((TextView)findViewById(R.id.cost)).setText(mQuest.difficulty);
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
                delete();
            }
        });

        descriptionLayout = (LinearLayout) findViewById(R.id.description_container);
        descriptionText = (TextView) findViewById(R.id.description);
        difficultyLayout = (LinearLayout) findViewById(R.id.difficulty_container);
        difficultyText = (TextView) findViewById(R.id.cost);
        rewardLayout = (LinearLayout) findViewById(R.id.reward_container);
        rewardText = (TextView) findViewById(R.id.reward);
        completeButton = (Button) findViewById(R.id.complete_button);
        buttonLayout = (LinearLayout) findViewById(R.id.button_container);
    }

    public void delete() {
        Firebase ref = new Firebase("https://rpgoflife.firebaseio.com");
        ref.child("users").child(ref.getAuth().getUid()).child("quests").child(mQuest.id).removeValue();
        Toast.makeText(this, "Quest deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void beginEdit() {
        descriptionEdit = new EditText(this);
        descriptionEdit.setText(descriptionText.getText());
        descriptionLayout.addView(descriptionEdit);
        descriptionText.setVisibility(View.GONE);

        difficultySpinner = new Spinner(this);
        String[] difficulties = {Quest.EASY_DIFFICULTY, Quest.MEDIUM_DIFFICULTY, Quest.HARD_DIFFICULTY, Quest.LEGENDARY_DIFFICULTY};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);
        int spinnerPos = 0;
        switch (mQuest.difficulty) {
            case Quest.MEDIUM_DIFFICULTY:
                spinnerPos = 1;
                break;
            case Quest.HARD_DIFFICULTY:
                spinnerPos = 2;
                break;
            case Quest.LEGENDARY_DIFFICULTY:
                spinnerPos = 3;
                break;
        }
        difficultySpinner.setSelection(spinnerPos);
        difficultyLayout.addView(difficultySpinner);
        difficultyText.setVisibility(View.GONE);

        rewardEdit = new EditText(this);
        rewardEdit.setText(rewardText.getText());
        rewardLayout.addView(rewardEdit);
        rewardEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        rewardText.setVisibility(View.GONE);

        editFab.setVisibility(View.GONE);
        deleteFab.setVisibility(View.GONE);
        doneButton = new Button(this);
        completeButton.setVisibility(View.GONE);
        doneButton.setText("Done");
        buttonLayout.addView(doneButton);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishEdit();
            }
        });
    }

    public void finishEdit() {
        mQuest.description = descriptionEdit.getText().toString();
        mQuest.difficulty = (String) difficultySpinner.getAdapter().getItem(difficultySpinner.getSelectedItemPosition());
        mQuest.reward = Integer.valueOf(rewardEdit.getText().toString());

        // Update Quest
        Firebase ref = new Firebase("https://rpgoflife.firebaseio.com");
        String uId = ref.getAuth().getUid();
        ref.child("users").child(uId).child("quests").child(mQuest.id).setValue(mQuest);

        descriptionEdit.setVisibility(View.GONE);
        difficultySpinner.setVisibility(View.GONE);
        rewardEdit.setVisibility(View.GONE);
        doneButton.setVisibility(View.GONE);
        descriptionText.setVisibility(View.VISIBLE);
        difficultyText.setVisibility(View.VISIBLE);
        rewardText.setVisibility(View.VISIBLE);
        completeButton.setVisibility(View.VISIBLE);
        editFab.setVisibility(View.VISIBLE);
        deleteFab.setVisibility(View.VISIBLE);
        descriptionText.setText(mQuest.description);
        difficultyText.setText(mQuest.difficulty);
        rewardText.setText(String.valueOf(mQuest.reward));

        Intent intent = new Intent();
        intent.putExtra("alteredQuest", mQuest);
        setResult(RESULT_OK, intent);
    }
}
