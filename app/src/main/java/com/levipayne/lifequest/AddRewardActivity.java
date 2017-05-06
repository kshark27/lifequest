package com.levipayne.lifequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.levipayne.lifequest.models.Reward;


public class AddRewardActivity extends PortraitActivity {

    // Form elements
    private TextView mDescriptionView;
    private TextView mCostView;
    private DatabaseReference mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reward);

        mDescriptionView = (TextView) findViewById(R.id.description);
        mCostView = (TextView) findViewById(R.id.difficulty);

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public void submit(View view) {
        if (mDescriptionView.getText().toString().equals("") || mCostView.getText().toString().equals("")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
        else {
            String description = mDescriptionView.getText().toString();
            int cost = Integer.valueOf(mCostView.getText().toString());
            Reward reward = new Reward(description, cost);

            // Save reward
            DatabaseReference newRef = mFirebaseRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("rewards").push();
            String id = newRef.getKey();
            reward.id = id;
            newRef.setValue(reward);
            
            Intent intent = new Intent();
            intent.putExtra("reward", reward);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
