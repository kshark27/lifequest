package com.levipayne.liferpg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class ForgotPasswordActivity extends AppCompatActivity {

    // UI Refs
    private EditText mEmailEdit;
    private Button mResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mEmailEdit = (EditText) findViewById(R.id.email);
        mResetButton = (Button) findViewById(R.id.reset_password_button);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEdit.getText().toString();

                Firebase ref = new Firebase(getResources().getString(R.string.firebase_url));
                ref.resetPassword(email, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ForgotPasswordActivity.this, "Email sent!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(ForgotPasswordActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
