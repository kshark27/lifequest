package com.levipayne.lifequest.firebaseTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levipayne.lifequest.models.PastQuest;
import com.levipayne.lifequest.models.PlayerStats;
import com.levipayne.lifequest.models.Quest;

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
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Update player stats
            final DatabaseReference statsRef = ref.child("users").child(uid).child("stats");
            statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                        int hpLost = calculateHpLost(stats.hp, quest.difficulty);
                        stats.hp -= hpLost;

                        // Save new stats
                        statsRef.setValue(stats);

                        PastQuest pQuest = new PastQuest(quest, false, hpLost);

                        // Move quest to failed past_quests and finish activity
                        ref.child("users").child(uid).child("quests").child(quest.id).removeValue();
                        ref.child("users").child(uid).child("past_quests").child("failed").child(pQuest.id).setValue(pQuest);
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });

            // Set last_health_regen to current time (if null) so regeneration will work
            final DatabaseReference lastRegenRef = ref.child("users").child(uid).child("last_health_regen");
            lastRegenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) lastRegenRef.setValue(System.currentTimeMillis());
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    public static int calculateHpLost(int hp, int difficulty) {
        int maxDamage = (int)Math.ceil((double)(Quest.MAX_DIFFICULTY - difficulty + 1)/2.0);
        if (hp - maxDamage >= 0) return maxDamage;
        else return hp;
    }
}
