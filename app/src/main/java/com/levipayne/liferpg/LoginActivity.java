package com.levipayne.liferpg;

import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.levipayne.liferpg.models.PlayerStats;
import com.levipayne.liferpg.models.Quest;
import com.levipayne.liferpg.models.Reward;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends PortraitActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mForgotPasswordButton;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button mRegisterButton = (Button) findViewById(R.id.register);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmailView.getText().toString() != "" && mPasswordView.getText().toString() != "") login(false);
                else Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmailView.getText().toString() != "" && mPasswordView.getText().toString() != "") register();
                else Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });

        mForgotPasswordButton = (Button) findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
//
        if (mFirebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void register() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        MainActivity.showLoadingDialog(this);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                        MainActivity.hideLoadingDialog(LoginActivity.this);

                        login(true);
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                        MainActivity.hideLoadingDialog(LoginActivity.this);
                    }
                }
            });
    }

    public void login(final boolean firstLogin) {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        MainActivity.showLoadingDialog(this);

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();

                        if (firstLogin) {
                            // Init player stats if first login
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            PlayerStats stats = new PlayerStats(0, 1, 0, 5, 5);
                            database.getReference().child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("stats").setValue(stats);

                            // Add a couple starter quests/rewards
                            Quest quest1 = new Quest("Create your first quest", 1, 10, Quest.calculateXpFromDifficulty(1, 1));
                            Quest quest2 = new Quest("Create your first reward", 1, 10, Quest.calculateXpFromDifficulty(1, 1));
                            Reward reward1 = new Reward("Treat yourself to something nice!", 20);

                            DatabaseReference questRef = database.getReference().child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("quests");
                            DatabaseReference questRef1 = questRef.push();
                            quest1.id = questRef1.getKey();
                            questRef1.setValue(quest1);
                            DatabaseReference questRef2 = questRef.push();
                            quest2.id = questRef2.getKey();
                            questRef2.setValue(quest2);

                            DatabaseReference rewardRef = database.getReference().child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("rewards");
                            DatabaseReference rewardRef1 = rewardRef.push();
                            reward1.id = rewardRef1.getKey();
                            rewardRef1.setValue(reward1);
                        }

                        MainActivity.hideLoadingDialog(LoginActivity.this);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("firstLogin", firstLogin);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
                        MainActivity.hideLoadingDialog(LoginActivity.this);
                    }
                }
            });
    }
}

