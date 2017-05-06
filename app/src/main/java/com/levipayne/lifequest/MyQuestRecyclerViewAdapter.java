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
import com.levipayne.lifequest.firebaseTasks.FailQuestAsyncTask;
import com.levipayne.lifequest.models.Quest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link} and makes a call to the
 * specified {@link MainActivity}.
 */
public class MyQuestRecyclerViewAdapter extends RecyclerView.Adapter<MyQuestRecyclerViewAdapter.ViewHolder> {

    final String TAG = MyQuestRecyclerViewAdapter.class.getSimpleName();

    private final List<Quest> mValues;
    private final MainActivity mListener;
    private final DatabaseReference mFirebaseRef;

    public MyQuestRecyclerViewAdapter(List<Quest> items, MainActivity listener) {
        mValues = items;
        mListener = listener;

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference questsRef = mFirebaseRef.child("users").child(uId).child("quests");

        questsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Quest quest = dataSnapshot.getValue(Quest.class);
                mValues.add(quest);
                notifyDataSetChanged();

                if (quest.dueDate != null) {
                    try {
                        checkQuestExpiration(quest);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
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
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(TAG, firebaseError.toString());
            }
        });


    }

    public void checkQuestExpiration(Quest quest) throws ParseException {
        Log.d(TAG, "Quest due date: " + quest.dueDate);
        SimpleDateFormat format = new SimpleDateFormat("M/d/yyyy");
        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(format.parse(quest.dueDate));
        Calendar currDate = Calendar.getInstance();
        Log.d(TAG, "Due date for <" + quest.description + "> is: " + dueDate.toString());
        if (!isToday(dueDate) && currDate.after(dueDate)) { // Quest is past due
            // Fail quest
            new FailQuestAsyncTask(mListener).execute(quest);
            Log.d(TAG, "quest: <" + quest.description + "> expired");
        }
        else Log.d(TAG, "quest: <" + quest.description + "> not expired");
    }

    public boolean isToday(Calendar date1) {
        Calendar currDate = Calendar.getInstance();
        Log.d(TAG, "Day:[" + date1.get(Calendar.DAY_OF_MONTH) + "," + currDate.get(Calendar.DAY_OF_MONTH) + "]");
        Log.d(TAG, "Month:[" + date1.get(Calendar.MONTH) + "," + currDate.get(Calendar.MONTH) + "]");
        Log.d(TAG, "Year:[" + date1.get(Calendar.YEAR) + "," + currDate.get(Calendar.YEAR) + "]");
        return date1.get(Calendar.DAY_OF_MONTH) == currDate.get(Calendar.DAY_OF_MONTH)
                && date1.get(Calendar.MONTH) == currDate.get(Calendar.MONTH)
                && date1.get(Calendar.YEAR) == currDate.get(Calendar.YEAR);
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
        public final LinearLayout mDateContainer;
        public Quest mQuest;

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
