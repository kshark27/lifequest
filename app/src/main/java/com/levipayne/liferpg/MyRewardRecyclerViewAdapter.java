package com.levipayne.liferpg;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.levipayne.liferpg.models.Reward;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Reward} and makes a call to the
 * specified {@link MainActivity}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyRewardRecyclerViewAdapter extends RecyclerView.Adapter<MyRewardRecyclerViewAdapter.ViewHolder> {
    final String TAG = MyRewardRecyclerViewAdapter.class.getSimpleName();

    private final List<Reward> mValues;
    private final MainActivity mListener;
    private final DatabaseReference mFirebaseRef;

    public MyRewardRecyclerViewAdapter(List<Reward> items, MainActivity listener) {
        mValues = new ArrayList<>();
        mListener = listener;

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rewardsRef = mFirebaseRef.child("users").child(uId).child("rewards");

        rewardsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Reward reward = dataSnapshot.getValue(Reward.class);
                mValues.add(reward);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Reward reward = dataSnapshot.getValue(Reward.class);
                int index = -1;
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i).id.equals(reward.id)) index = i;
                }
                if (index != -1) {
                    mValues.remove(index);
                    mValues.add(index, reward);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Reward reward = dataSnapshot.getValue(Reward.class);
                int index = -1;
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i).id.equals(reward.id)) index = i;
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
            mCostView = (TextView) view.findViewById(R.id.difficulty);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCostView.getText() + "'";
        }
    }
}
