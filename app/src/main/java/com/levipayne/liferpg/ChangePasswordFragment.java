package com.levipayne.liferpg;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordFragment extends Fragment {

    // UI Refs
    private EditText mCurrPasswordEdit;
    private EditText mNewPasswordEdit;
    private Button mChangeButton;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mCurrPasswordEdit = (EditText) getView().findViewById(R.id.current_password);
        mNewPasswordEdit = (EditText) getView().findViewById(R.id.new_password);
        mChangeButton = (Button) getView().findViewById(R.id.change_button);

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkForValidInput()) {
                    MainActivity.showLoadingDialog((PortraitActivity) ChangePasswordFragment.this.getActivity());

                    String currPassword = mCurrPasswordEdit.getText().toString();
                    final String newPassword = mNewPasswordEdit.getText().toString();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(user.getEmail(), currPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), "Password changed!", Toast.LENGTH_LONG).show();
                                                            ((MainActivity) getActivity()).selectItem(0); // Go home
                                                        } else {
                                                            Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_LONG).show();

                                                        }
                                                        MainActivity.hideLoadingDialog((PortraitActivity) ChangePasswordFragment.this.getActivity());
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getActivity(), "Current password is incorrect", Toast.LENGTH_LONG).show();
                                        MainActivity.hideLoadingDialog((PortraitActivity) ChangePasswordFragment.this.getActivity());
                                    }
                                }
                            });


                }
            }
        });
    }

    public boolean checkForValidInput() {
        if (mNewPasswordEdit.getText().toString().equals("") || mCurrPasswordEdit.getText().toString().equals("")) {
            Toast.makeText(this.getActivity(), "Please fill in both fields", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
