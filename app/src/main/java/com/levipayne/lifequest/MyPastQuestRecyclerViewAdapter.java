package com.levipayne.lifequest;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.levipayne.lifequest.models.PastQuest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Levi on 5/12/2016.
 */
public class MyPastQuestRecyclerViewAdapter extends RecyclerView.Adapter<MyPastQuestRecyclerViewAdapter.ViewHolder> {
    final String TAG = MyPastQuestRecyclerViewAdapter.class.getSimpleName();

    private final List<PastQuest> mValues;
    private final MainActivity mListener;
    private final DatabaseReference mFirebaseRef;

    public MyPastQuestRecyclerViewAdapter(MainActivity listener, boolean complete) {
        mValues = new ArrayList<>();
        mListener = listener;

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference questsRef = mFirebaseRef.child("users").child(uId).child("past_quests");
        if (complete) questsRef = questsRef.child("completed");
        else questsRef = questsRef.child("failed");

        questsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                PastQuest quest = dataSnapshot.getValue(PastQuest.class);
                mValues.add(0, quest);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                PastQuest quest = dataSnapshot.getValue(PastQuest.class);
                int index = -1;
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i).id.equals(quest.id)) index = i;
                }
                if (index != -1) {
                    mValues.remove(index);
                    mValues.add(index, quest);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                PastQuest quest = dataSnapshot.getValue(PastQuest.class);
                int index = -1;
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i).id.equals(quest.id)) index = i;
                }
                if (index != -1) mValues.remove(index);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(TAG, firebaseError.toString());
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_quest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mQuest = mValues.get(position);
        holder.mDescriptionView.setText(mValues.get(position).description);
        holder.mDifficultyView.setText(mValues.get(position).difficulty + "");
        holder.mGoldView.setText(String.valueOf(mValues.get(position).reward));

        if (mValues.get(position).dueDate != null) {
            holder.mDateContainer.setVisibility(View.VISIBLE);
            ((TextView)holder.mDateContainer.findViewById(R.id.date_text)).setText(mValues.get(position).dueDate);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mQuest);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDescriptionView;
        public final TextView mDifficultyView;
        public final TextView mGoldView;
        public final LinearLayout mDateContainer;
        public PastQuest mQuest;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescriptionView = (TextView) view.findViewById(R.id.description);
            mDifficultyView = (TextView) view.findViewById(R.id.difficulty);
            mGoldView = (TextView) view.findViewById(R.id.g);
            mDateContainer = (LinearLayout) view.findViewById(R.id.date_container);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDifficultyView.getText() + "'";
        }
    }
}
