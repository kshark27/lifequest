package com.levipayne.lifequest;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levipayne.lifequest.dialogs.ConfirmationDialogFragment;
import com.levipayne.lifequest.events.ConfirmDeleteEvent;
import com.levipayne.lifequest.events.Event;
import com.levipayne.lifequest.events.IEventListener;
import com.levipayne.lifequest.models.PlayerStats;
import com.levipayne.lifequest.models.Reward;

public class RewardDetailsActivity extends PortraitActivity implements IEventListener {

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
                ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
                dialogFragment.setMessage("Are you sure you want to delete this reward?");
                dialogFragment.setEvent(new ConfirmDeleteEvent(dialogFragment));
                dialogFragment.show(getSupportFragmentManager(), "DeleteConfirmation");
                dialogFragment.addEventListener(RewardDetailsActivity.this, ConfirmDeleteEvent.TYPE);
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
        MainActivity.showLoadingDialog(this);
        // Get player stats (in order to get player's total gold)
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference statsRef = ref.child("users").child(uid).child("stats");
        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                    String toastMessage = "";
                    if (stats.gold >= mReward.cost) { // Purchase item (deduct from player gold)
                        toastMessage = "Reward purchased!";
                        stats.gold -= mReward.cost;
                        statsRef.setValue(stats);
                        delete();
                    }
                    else { // Player does not have enough gold
                        toastMessage = "You do not have enough gold to purchase this.";
                    }

                    MainActivity.hideLoadingDialog(RewardDetailsActivity.this);
                    Toast.makeText(RewardDetailsActivity.this, toastMessage, Toast.LENGTH_LONG).show();
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
        ref.child("users").child(uid).child("rewards").child(mReward.id).removeValue();
//        Toast.makeText(this, "Reward deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void beginEdit() {
        descriptionEdit = new EditText(this);
        descriptionEdit.setText(descriptionText.getText());
        descriptionLayout.addView(descriptionEdit);
        descriptionText.setVisibility(View.GONE);

        costEdit = (EditText) findViewById(R.id.cost_edit);
        costEdit.setVisibility(View.VISIBLE);
        costEdit.setText(costText.getText());
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
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref.child("users").child(uid).child("rewards").child(mReward.id).setValue(mReward);

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

    @Override
    public void handleEvent(Event event) {
        if (event.getEventType().equals(ConfirmDeleteEvent.TYPE)) delete();
    }
}
