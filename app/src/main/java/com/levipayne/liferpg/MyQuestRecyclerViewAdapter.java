package com.levipayne.liferpg;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link} and makes a call to the
 * specified {@link MainActivity}.
 */
public class MyQuestRecyclerViewAdapter extends RecyclerView.Adapter<MyQuestRecyclerViewAdapter.ViewHolder> {

    final String TAG = MyQuestRecyclerViewAdapter.class.getSimpleName();

    private final List<Quest> mValues;
    private final MainActivity mListener;
    private final Firebase mFirebaseRef;

    public MyQuestRecyclerViewAdapter(List<Quest> items, MainActivity listener) {
        mValues = items;
        mListener = listener;

        mFirebaseRef = new Firebase(listener.getResources().getString(R.string.firebase_url));
        AuthData authData = mFirebaseRef.getAuth();
        String uId = authData.getUid();
        Firebase questsRef = mFirebaseRef.child("users").child(uId).child("quests");
//        questsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                Map<String, Object> questMap = (Map<String, Object>) snapshot.getValue();
//                List<Quest> quests = new ArrayList<Quest>();
//                for (String key : questMap.keySet()) {
//                    HashMap<String, Object> questVals = (HashMap<String, Object>) questMap.get(key);
//                    String description = (String) questVals.get("description");
//                    String difficulty = (String) questVals.get("difficulty");
//                    int reward = (int) ((long) questVals.get("reward"));
//                    int xp = (int) ((long) questVals.get("xp"));
//                    Quest quest = new Quest(description, difficulty, reward, xp);
//                    quest.id = key;
//                    quests.add(quest);
//                }
//                MyQuestRecyclerViewAdapter.this.mValues.clear();
//                MyQuestRecyclerViewAdapter.this.mValues.addAll(quests);
//                notifyDataSetChanged();
//            }
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                Log.d(TAG, "The read failed: " + firebaseError.getMessage());
//            }
//        });

        questsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Quest quest = dataSnapshot.getValue(Quest.class);
                mValues.add(quest);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Quest quest = dataSnapshot.getValue(Quest.class);
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
                Quest quest = dataSnapshot.getValue(Quest.class);
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
            public void onCancelled(FirebaseError firebaseError) {
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
        holder.mDifficultyView.setText(mValues.get(position).difficulty);
        holder.mGoldView.setText(String.valueOf(mValues.get(position).reward));

        switch (mValues.get(position).difficulty) {
            case Quest.EASY_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.easyColor));
                break;
            case Quest.MEDIUM_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.mediumColor));
                break;
            case Quest.HARD_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.hardColor));
                break;
            case Quest.LEGENDARY_DIFFICULTY:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.legendaryColor));
                break;
            default:
                holder.mView.setBackgroundColor(ContextCompat.getColor(mListener.getApplicationContext(), R.color.easyColor));
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

    public void addItem(Quest q) {
        mValues.add(q);
        this.notifyDataSetChanged();
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
        public Quest mQuest;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescriptionView = (TextView) view.findViewById(R.id.description);
            mDifficultyView = (TextView) view.findViewById(R.id.cost);
            mGoldView = (TextView) view.findViewById(R.id.g);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDifficultyView.getText() + "'";
        }
    }
}
