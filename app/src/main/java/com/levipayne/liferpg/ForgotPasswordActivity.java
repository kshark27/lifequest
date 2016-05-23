package com.levipayne.liferpg;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends PortraitActivity {

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
                MainActivity.showLoadingDialog(ForgotPasswordActivity.this);

                String email = mEmailEdit.getText().toString();

                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, "Email sent!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else {
                                Toast.makeText(ForgotPasswordActivity.this, "Something went wrong. Please try again: " + task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                            MainActivity.hideLoadingDialog(ForgotPasswordActivity.this);
                        }
                    });
            }
        });
    }
}
