package com.levipayne.liferpg;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


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
                String currPassword = mCurrPasswordEdit.getText().toString();
                String newPassword = mNewPasswordEdit.getText().toString();

                Firebase ref = new Firebase(getResources().getString(R.string.firebase_url));
                ref.changePassword(ref.getAuth().getProviderData().get("email").toString(), currPassword, newPassword, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), "Password changed!", Toast.LENGTH_LONG).show();
                        ((MainActivity)getActivity()).selectItem(0); // Go home
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(getActivity(), "Something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
