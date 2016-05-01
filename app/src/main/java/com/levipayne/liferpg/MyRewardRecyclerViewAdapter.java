package com.levipayne.liferpg;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Reward} and makes a call to the
 * specified {@link MainActivity}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyRewardRecyclerViewAdapter extends RecyclerView.Adapter<MyRewardRecyclerViewAdapter.ViewHolder> {

    private final List<Reward> mValues;
    private final MainActivity mListener;
    private final Firebase mFirebaseRef;

    public MyRewardRecyclerViewAdapter(List<Reward> items, MainActivity listener) {
        mValues = new ArrayList<>();
        mListener = listener;

        mFirebaseRef = new Firebase("https://rpgoflife.firebaseio.com/users");
        AuthData authData = mFirebaseRef.getAuth();
        Firebase questsRef = mFirebaseRef.child(authData.getUid()).child("rewards");
        questsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String,Object> rewardMap = (Map<String,Object>) snapshot.getValue();
                List<Reward> rewards = new ArrayList<>();
                for (String key : rewardMap.keySet()) {
                    HashMap<String,Object> rewardVals = (HashMap<String,Object>)rewardMap.get(key);
                    String description = (String)rewardVals.get("description");
                    int cost = (int)((long) rewardVals.get("cost"));
                    Reward reward = new Reward(description, cost);
                    rewards.add(reward);
                }
                MyRewardRecyclerViewAdapter.this.mValues.clear();
                MyRewardRecyclerViewAdapter.this.mValues.addAll(rewards);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDescriptionView.setText(mValues.get(position).description);
        holder.mCostView.setText(String.valueOf(mValues.get(position).cost));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    public void addItem(Reward r) {
        mValues.add(r);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDescriptionView;
        public final TextView mCostView;
        public Reward mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescriptionView = (TextView) view.findViewById(R.id.description);
            mCostView = (TextView) view.findViewById(R.id.cost);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCostView.getText() + "'";
        }
    }
}
