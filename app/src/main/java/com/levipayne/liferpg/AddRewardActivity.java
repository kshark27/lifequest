package com.levipayne.liferpg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddRewardActivity extends AppCompatActivity {

    // Form elements
    private TextView mDescriptionView;
    private TextView mCostView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reward);

        mDescriptionView = (TextView) findViewById(R.id.description);
        mCostView = (TextView) findViewById(R.id.cost);
    }

    public void submit(View view) {
        if (mDescriptionView.getText().toString().equals("") || mCostView.getText().toString().equals("")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
        else {
            String description = mDescriptionView.getText().toString();
            int cost = Integer.valueOf(mCostView.getText().toString());
            Reward reward = new Reward(description, cost);
            Intent intent = new Intent();
            intent.putExtra("reward", reward);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
