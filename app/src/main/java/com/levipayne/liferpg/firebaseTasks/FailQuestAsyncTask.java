package com.levipayne.liferpg.firebaseTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.levipayne.liferpg.PlayerStats;
import com.levipayne.liferpg.Quest;
import com.levipayne.liferpg.R;

/**
 * Created by Levi on 5/12/2016.
 */
public class FailQuestAsyncTask extends AsyncTask<Quest,Void,Void> {
    boolean leveledUp;
    private Context mContext;

    public FailQuestAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Quest... params) {
        for (final Quest quest : params) {
            final Firebase ref = new Firebase(mContext.getResources().getString(R.string.firebase_url));

            // Update player stats
            final Firebase statsRef = ref.child("users").child(ref.getAuth().getUid()).child("stats");
            statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                        stats.hp = Math.max(stats.hp - (int)Math.ceil((double)(Quest.MAX_DIFFICULTY - quest.difficulty + 1)/2.0), 0); // More hp is lost the easier the quest

                        // Save new stats
                        statsRef.setValue(stats);

                        // Move quest to failed past_quests and finish activity
                        ref.child("users").child(ref.getAuth().getUid()).child("quests").child(quest.id).removeValue();
                        ref.child("users").child(ref.getAuth().getUid()).child("past_quests").child("failed").child(quest.id).setValue(quest);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

            // Set last_health_regen to current time (if null) so regeneration will work
            final Firebase lastRegenRef = ref.child("users").child(ref.getAuth().getUid()).child("last_health_regen");
            lastRegenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) lastRegenRef.setValue(System.currentTimeMillis());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (leveledUp) {
            Toast.makeText(mContext, "Leveled up!", Toast.LENGTH_LONG).show();
        }
    }
}
