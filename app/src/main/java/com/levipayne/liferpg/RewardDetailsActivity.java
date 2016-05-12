package com.levipayne.liferpg;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class RewardDetailsActivity extends AppCompatActivity {

    private Reward mReward;
    private LinearLayout descriptionLayout;
    private LinearLayout costLayout;
    private LinearLayout buttonLayout;
    private TextView descriptionText;
    private TextView costText;
    private EditText descriptionEdit;
    private EditText costEdit;
    private FloatingActionButton editFab;
    private FloatingActionButton deleteFab;
    private Button doneButton;
    private Button mPurchaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_details);

        Intent intent = this.getIntent();
        mReward = (Reward) intent.getSerializableExtra("reward");
        ((TextView)findViewById(R.id.description)).setText(mReward.description);
        ((TextView)findViewById(R.id.difficulty)).setText(mReward.cost + "");

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

        mPurchaseButton = (Button) findViewById(R.id.purchase_button);
        mPurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchase();
            }
        });

        doneButton = (Button) findViewById(R.id.done_button);

        descriptionLayout = (LinearLayout) findViewById(R.id.description_container);
        descriptionText = (TextView) findViewById(R.id.description);
        costLayout = (LinearLayout) findViewById(R.id.inner_cost_container);
        costText = (TextView) findViewById(R.id.difficulty);
        buttonLayout = (LinearLayout) findViewById(R.id.button_container);
    }

    public void purchase() {
        new AsyncTask<Void, Void, Void>() {
            boolean success = false;

            @Override
            protected Void doInBackground(Void... params) {
                // Get player stats (in order to get player's total gold)
                Firebase ref = new Firebase(getResources().getString(R.string.firebase_url));
                final Firebase statsRef = ref.child("users").child(ref.getAuth().getUid()).child("stats");
                statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                            if (stats.gold > mReward.cost) { // Purchase item (deduct from player gold)
                                success = true;
                                stats.gold -= mReward.cost;
                                statsRef.setValue(stats);
                            }
                        }
                        else { // Player does not have enough gold
                            success = false;
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                String toastMessage = "";
                if (success) toastMessage = "Reward purchased!";
                else toastMessage = "You do not have enough gold to purchase this.";
                Toast.makeText(RewardDetailsActivity.this, toastMessage, Toast.LENGTH_LONG).show();

                if (success) { // Delete reward and finish
                    delete();
                }
            }
        }.execute();
    }

    public void delete() {
        Firebase ref = new Firebase("https://rpgoflife.firebaseio.com");
        ref.child("users").child(ref.getAuth().getUid()).child("rewards").child(mReward.id).removeValue();
//        Toast.makeText(this, "Reward deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void beginEdit() {
        descriptionEdit = new EditText(this);
        descriptionEdit.setText(descriptionText.getText());
        descriptionLayout.addView(descriptionEdit);
        descriptionText.setVisibility(View.GONE);

        costEdit = new EditText(this);
        costEdit.setText(costText.getText());
        costLayout.addView(costEdit);
        costEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        costText.setVisibility(View.GONE);

        editFab.setVisibility(View.GONE);
        deleteFab.setVisibility(View.GONE);
        mPurchaseButton.setVisibility(View.GONE);

        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishEdit();
            }
        });
    }

    public void finishEdit() {
        mReward.description = descriptionEdit.getText().toString();
        mReward.cost = Integer.valueOf(costEdit.getText().toString());

        // Update Quest
        Firebase ref = new Firebase("https://rpgoflife.firebaseio.com/users");
        ref.child(ref.getAuth().getUid()).child("rewards").child(mReward.id).setValue(mReward);

        descriptionEdit.setVisibility(View.GONE);
        costEdit.setVisibility(View.GONE);
        doneButton.setVisibility(View.GONE);

        descriptionText.setVisibility(View.VISIBLE);
        costText.setVisibility(View.VISIBLE);
        editFab.setVisibility(View.VISIBLE);
        deleteFab.setVisibility(View.VISIBLE);
        mPurchaseButton.setVisibility(View.VISIBLE);

        descriptionText.setText(mReward.description);
        costText.setText(mReward.cost + "");

        Intent intent = new Intent();
        intent.putExtra("alteredReward", mReward);
        setResult(RESULT_OK, intent);
    }
}
