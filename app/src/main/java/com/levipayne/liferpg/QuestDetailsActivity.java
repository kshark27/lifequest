package com.levipayne.liferpg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private EditText difficultyEdit;
    private EditText rewardEdit;
    private Button editButton;
    private Button completeButton;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_details);

        Intent intent = this.getIntent();
        mQuest = (Quest) intent.getSerializableExtra("quest");
        ((TextView)findViewById(R.id.description)).setText(mQuest.description);
        ((TextView)findViewById(R.id.difficulty)).setText(mQuest.difficulty);
        ((TextView)findViewById(R.id.reward)).setText(String.valueOf(mQuest.reward));

        editButton = (Button)findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginEdit();
            }
        });

        descriptionLayout = (LinearLayout) findViewById(R.id.description_container);
        descriptionText = (TextView) findViewById(R.id.description);
        difficultyLayout = (LinearLayout) findViewById(R.id.difficulty_container);
        difficultyText = (TextView) findViewById(R.id.difficulty);
        rewardLayout = (LinearLayout) findViewById(R.id.reward_container);
        rewardText = (TextView) findViewById(R.id.reward);
        completeButton = (Button) findViewById(R.id.complete_button);
        buttonLayout = (LinearLayout) findViewById(R.id.button_container);
    }

    public void beginEdit() {
        descriptionEdit = new EditText(this);
        descriptionEdit.setText(descriptionText.getText());
        descriptionLayout.addView(descriptionEdit);
        descriptionText.setVisibility(View.GONE);

        difficultyEdit = new EditText(this);
        difficultyEdit.setText(difficultyText.getText());
        difficultyLayout.addView(difficultyEdit);
        difficultyText.setVisibility(View.GONE);

        rewardEdit = new EditText(this);
        rewardEdit.setText(rewardText.getText());
        rewardLayout.addView(rewardEdit);
        rewardText.setVisibility(View.GONE);

        editButton.setVisibility(View.GONE);
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
        mQuest.difficulty = difficultyEdit.getText().toString();
        mQuest.reward = Integer.valueOf(rewardEdit.getText().toString());

        descriptionEdit.setVisibility(View.GONE);
        difficultyEdit.setVisibility(View.GONE);
        rewardEdit.setVisibility(View.GONE);
        doneButton.setVisibility(View.GONE);
        descriptionText.setVisibility(View.VISIBLE);
        difficultyText.setVisibility(View.VISIBLE);
        rewardText.setVisibility(View.VISIBLE);
        completeButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.VISIBLE);
        descriptionText.setText(mQuest.description);
        difficultyText.setText(mQuest.difficulty);
        rewardText.setText(String.valueOf(mQuest.reward));
    }
}
