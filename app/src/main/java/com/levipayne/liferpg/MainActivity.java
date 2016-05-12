package com.levipayne.liferpg;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.levipayne.liferpg.events.Event;
import com.levipayne.liferpg.events.IEventListener;
import com.levipayne.liferpg.events.QuestFailedEvent;

import java.util.List;

public class MainActivity extends AppCompatActivity implements QuestFragment.OnListFragmentInteractionListener, RewardFragment.OnListFragmentInteractionListener, IEventListener {
    final String TAG = MainActivity.class.getSimpleName();

    static final int NUM_ITEMS = 2;
    private final int QUEST_REQUEST_CODE = 1;
    private final int REWARD_REQUEST_CODE = 2;
    private final int QUEST_DETAIL_CODE = 3;
    private final int REWARD_DETAIL_CODE = 4;
    private int mCurrentTabPos;
    private View mFab;
    private QuestFragment mQuestFrag;
    private RewardFragment mRewardFrag;
    private DialogFragment mCurrentDialog;

    // UI References
    private TextView mLevelText;
    private TextView mXpText;
    private TextView mGoldText;
    private TextView mHpText;
    private ProgressBar mXpBar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // UI Reference Instantiation
        mLevelText = (TextView) findViewById(R.id.level);
        mXpText = (TextView) findViewById(R.id.xp);
        mGoldText = (TextView) findViewById(R.id.gold);
        mHpText = (TextView) findViewById(R.id.hp);
        mXpBar = (ProgressBar) findViewById(R.id.xp_bar);

        Firebase ref = new Firebase(getResources().getString(R.string.firebase_url) + "/users");
        AuthData authData = ref.getAuth();
        Log.d(TAG, "id: " + authData.getUid());
        final Firebase userRef = ref.child(authData.getUid());

        showLoadingDialog();

        // Load Stats into views
        userRef.child("stats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);

                    // Level
                    mLevelText.setText(stats.level + "");

                    // XP
                    int max = PlayerStats.getNextXpGoal(stats.level);
                    mXpText.setText(stats.xp + " / " + max);
                    mXpBar.setMax(max);
                    mXpBar.setProgress(stats.xp);

                    // Gold
                    mGoldText.setText(stats.gold + "");

                    // HP
                    if (stats.hp < stats.maxHp) { // Player is not at full health
                        // Check to see if health needs to be regenerated (at least one day has passed)
                        userRef.child("last_health_regen").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long millisInADay = 1000 * 60 * 60 * 24;
                                int heartsToRegen = 0;
                                if (dataSnapshot.getValue() == null) {
                                    heartsToRegen = 1;
                                } else if (System.currentTimeMillis() - (long) dataSnapshot.getValue() - millisInADay >= 0) { // Either last health regen is not set or it has been more than a day
                                    long elapsedMillis = System.currentTimeMillis() - (long) dataSnapshot.getValue();
                                    int elapsedDays = (int) (elapsedMillis / millisInADay);
                                    heartsToRegen = elapsedDays;

                                    Log.d(TAG, "Health regenerated after " + elapsedDays + " days");
                                } else Log.d(TAG, "Health not regenerated");

                                PlayerStats newStats = stats;
                                if (heartsToRegen > 0) {
                                    newStats.hp = Math.min(stats.maxHp, stats.hp + heartsToRegen);

                                    userRef.child("stats").setValue(newStats);
                                    userRef.child("last_health_regen").setValue(System.currentTimeMillis());
                                }
                                mHpText.setText(newStats.hp + "/" + newStats.maxHp);

                                hideLoadingDialog();
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }
                    else {
                        mHpText.setText(stats.hp + "/" + stats.maxHp);

                        hideLoadingDialog();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mCurrentTabPos = 0;

        // Set up Reward and Quest Fragments
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Quests"));
        tabLayout.addTab(tabLayout.newTab().setText("Rewards"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTabPos = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f instanceof QuestFragment) mQuestFrag = (QuestFragment) f;
                else if (f instanceof RewardFragment) mRewardFrag = (RewardFragment) f;
            }
        }

        // FAB
        mFab = findViewById(R.id.addFAB);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                switch (mCurrentTabPos) {
                    case 0:
                        intent = new Intent(MainActivity.this, AddQuestActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, AddRewardActivity.class);
                        startActivity(intent);
                        break;
                    default:
                }
            }
        });

        // Drawer setup
        final String[] drawerItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Go to past quests
                        break;
                    case 1: // Go to past rewards

                }
            }
        });
    }

    public void showLoadingDialog() {
        LoadingDialogFragment dialogFragment = new LoadingDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "loading");
    }

    public void hideLoadingDialog() {
        LoadingDialogFragment loadingDialogFragment = ((LoadingDialogFragment)getSupportFragmentManager().findFragmentByTag("loading"));
        if (loadingDialogFragment != null) loadingDialogFragment.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Firebase ref = new Firebase(getResources().getString(R.string.firebase_url));
        ref.unauth();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onListFragmentInteraction(Quest item) {
        Intent intent = new Intent(this, QuestDetailsActivity.class);
        intent.putExtra("quest", item);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(Reward item) {
        Intent intent = new Intent(this, RewardDetailsActivity.class);
        intent.putExtra("reward", item);
        startActivity(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void handleEvent(final Event event) {
        if (event instanceof QuestFailedEvent) {
            new AsyncTask<Void, Void, Void>() {
                Quest mQuest = ((QuestFailedEvent)event).quest;

                boolean leveledUp;

                @Override
                protected Void doInBackground(Void... params) {
                    final Firebase ref = new Firebase(getResources().getString(R.string.firebase_url));

                    // Update player stats
                    final Firebase statsRef = ref.child("users").child(ref.getAuth().getUid()).child("stats");
                    statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                                stats.hp = Math.max(stats.hp - (int)Math.ceil((double)(Quest.MAX_DIFFICULTY - mQuest.difficulty + 1)/2.0), 0); // More hp is lost the easier the quest

                                // Save new stats
                                statsRef.setValue(stats);

                                // Move quest to failed past_quests and finish activity
                                ref.child("users").child(ref.getAuth().getUid()).child("quests").child(mQuest.id).removeValue();
                                ref.child("users").child(ref.getAuth().getUid()).child("past_quests").child("failed").child(mQuest.id).setValue(mQuest);
                                finish();
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
                            if (dataSnapshot == null) lastRegenRef.setValue(System.currentTimeMillis());
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (leveledUp) {
                        Toast.makeText(MainActivity.this, "Leveled up!", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    public static class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    QuestFragment tab1 = new QuestFragment();
                    return tab1;
                case 1:
                    RewardFragment tab2 = new RewardFragment();
                    return tab2;
                default:
                    return null;
            }
        }
    }

    public static class LoadingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.loading_dialog, null))
                    .setTitle("Loading...");
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
