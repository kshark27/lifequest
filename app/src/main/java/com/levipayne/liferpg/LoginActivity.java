package com.levipayne.liferpg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BatchActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mForgotPasswordButton;

    private Firebase mFirebaseRef;

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

        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

        AuthData authData = mFirebaseRef.getAuth();
        if (authData != null) {
            Log.d(TAG, "id: " + authData.getUid());
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void register() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        MainActivity.showLoadingDialog(this);

        mFirebaseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Toast.makeText(LoginActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                MainActivity.hideLoadingDialog(LoginActivity.this);

                login(true);
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(LoginActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login(final boolean firstLogin) {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        MainActivity.showLoadingDialog(this);

        mFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Toast.makeText(LoginActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();

                if (firstLogin) {
                    // Init player stats if first login
                    PlayerStats stats = new PlayerStats(0, 1, 0, 5, 5);
                    mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("stats").setValue(stats);

                    // Add a couple starter quests/rewards
                    Quest quest1 = new Quest("Create your first quest", 1, 10, Quest.calculateXpFromDifficulty(1, 1));
                    Quest quest2 = new Quest("Create your first reward", 1, 10, Quest.calculateXpFromDifficulty(1, 1));
                    Reward reward1 = new Reward("Treat yourself to something nice!", 20);

                    Firebase questRef = mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("quests");
                    Firebase questRef1 = questRef.push();
                    quest1.id = questRef1.getKey();
                    questRef1.setValue(quest1);
                    Firebase questRef2 = questRef.push();
                    quest2.id = questRef2.getKey();
                    questRef2.setValue(quest2);

                    Firebase rewardRef = mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("rewards");
                    Firebase rewardRef1 = rewardRef.push();
                    reward1.id = rewardRef1.getKey();
                    rewardRef1.setValue(reward1);
                }

                MainActivity.hideLoadingDialog(LoginActivity.this);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("firstLogin", firstLogin);
                startActivity(intent);
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Toast.makeText(LoginActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

