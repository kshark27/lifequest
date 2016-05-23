package com.levipayne.liferpg;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levipayne.liferpg.dialogs.PlayerDeathDialog;
import com.levipayne.liferpg.events.Event;
import com.levipayne.liferpg.events.EventDispatcher;
import com.levipayne.liferpg.events.IEventListener;
import com.levipayne.liferpg.events.PlayerDiedEvent;
import com.levipayne.liferpg.events.QuestFailedEvent;
import com.levipayne.liferpg.models.PastQuest;
import com.levipayne.liferpg.models.PlayerStats;
import com.levipayne.liferpg.models.Quest;
import com.levipayne.liferpg.models.Reward;

import java.util.List;

public class MainActivity extends PortraitActivity implements QuestFragment.OnListFragmentInteractionListener,
        RewardFragment.OnListFragmentInteractionListener,
        CompletedPastQuestFragment.OnListFragmentInteractionListener,
        FailedPastQuestFragment.OnListFragmentInteractionListener,
        IEventListener {

    final String TAG = MainActivity.class.getSimpleName();

    // Drawer
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mDrawer;

    static final int NUM_ITEMS = 2;
    private int mCurrentTabPos;
    private FloatingActionButton mFab;
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

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ValueEventListener mStatListener;

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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference().child("users").child(auth.getCurrentUser().getUid());

        showLoadingDialog(this);

        final EventDispatcher dispatcher = new EventDispatcher();
        dispatcher.addEventListener(this, PlayerDiedEvent.TYPE);

        // Load Stats into views
        mStatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
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
                    if (stats.hp <= 0) { // Player died
                        dispatcher.dispatchEvent(new PlayerDiedEvent(dispatcher));
                    }
                    else if (stats.hp < stats.maxHp) { // Player is not at full health
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

                                PlayerStats newStats = new PlayerStats(stats.gold, stats.level, stats.xp, stats.hp, stats.maxHp);
                                if (heartsToRegen > 0) {
                                    newStats.hp = Math.min(stats.maxHp, stats.hp + heartsToRegen);

                                    userRef.child("stats").setValue(newStats);
                                    userRef.child("last_health_regen").setValue(System.currentTimeMillis());
                                }
                                mHpText.setText(newStats.hp + "/" + newStats.maxHp);
                            }

                            @Override
                            public void onCancelled(DatabaseError firebaseError) {

                            }
                        });
                    }
                    else {
                        mHpText.setText(stats.hp + "/" + stats.maxHp);
                    }
                    hideLoadingDialog(MainActivity.this);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        };

//        if (savedInstanceState != null) {
//            getSupportFragmentManager().popBackStack(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
//            mCurrentPage = savedInstanceState.getInt("currentPage");
//        }

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
        mFab = (FloatingActionButton) findViewById(R.id.addFAB);
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
        mDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems));

        mDrawerTitle = "Title";
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Launch tutorial views if first login
        boolean launchTutorial = getIntent().getBooleanExtra("firstLogin", false);
        if (launchTutorial) {
            launchTutorial();
        }

        // Set up hide fab button
        final ImageView button = (ImageView)findViewById(R.id.arrow_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mFab.isShown()) {
                    mFab.show();
                    button.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.ic_keyboard_arrow_right_black_24dp));
                }
                else {
                    mFab.hide();
                    button.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));
                }
            }
        });
    }

    // ---------------- Lifecycle methods -------------------

    @Override
    public void onStop() {
        super.onStop();


    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onPause() {
        super.onPause();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference statsRef = database.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stats");
            statsRef.removeEventListener(mStatListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference statsRef = database.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stats");
        statsRef.addValueEventListener(mStatListener);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("currentPage", mCurrentPage);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // -------------------------------------------------------

    public static void showLoadingDialog(PortraitActivity activity) {
        LoadingDialogFragment dialogFragment = new LoadingDialogFragment();
        dialogFragment.show(activity.getSupportFragmentManager(), "loading");
    }

    public static void hideLoadingDialog(PortraitActivity activity) {
        LoadingDialogFragment loadingDialogFragment = ((LoadingDialogFragment)activity.getSupportFragmentManager().findFragmentByTag("loading"));
        if (loadingDialogFragment != null) loadingDialogFragment.dismiss();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
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

    @Override
    public void onListFragmentInteraction(PastQuest item) {
        Intent intent = new Intent(this, PastQuestDetailsActivity.class);
        intent.putExtra("quest", item);
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
                    final FirebaseAuth auth = FirebaseAuth.getInstance();
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                    // Update player stats
                    final DatabaseReference statsRef = ref.child("users").child(auth.getCurrentUser().getUid()).child("stats");
                    statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null) {
                                PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                                stats.hp = Math.max(stats.hp - (int)Math.ceil((double)(Quest.MAX_DIFFICULTY - mQuest.difficulty + 1)/2.0), 0); // More hp is lost the easier the quest

                                // Save new stats
                                statsRef.setValue(stats);

                                // Move quest to failed past_quests and finish activity
                                ref.child("users").child(auth.getCurrentUser().getUid()).child("quests").child(mQuest.id).removeValue();
                                ref.child("users").child(auth.getCurrentUser().getUid()).child("past_quests").child("failed").child(mQuest.id).setValue(mQuest);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });

                    // Set last_health_regen to current time (if null) so regeneration will work
                    final DatabaseReference lastRegenRef = ref.child("users").child(auth.getCurrentUser().getUid()).child("last_health_regen");
                    lastRegenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot == null) lastRegenRef.setValue(System.currentTimeMillis());
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

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
        else if (event.getEventType().equals(PlayerDiedEvent.TYPE)) {
            onPlayerDeath();
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    int mCurrentPage = 0;
    Fragment mCurrentFragment;
    /** Swaps fragments in the main content view */
    public void selectItem(int position) {
        Log.d(TAG, "selectItem() called");
        switch (position) {
            case 0:
                if (mCurrentPage != 0) {
                    getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                    mFab.setVisibility(View.VISIBLE);
                    mCurrentPage = 0;

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .remove(mCurrentFragment)
                            .commit();

                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                }
                mDrawerLayout.closeDrawer(mDrawer);
                break;
            case 1:
                if (mCurrentPage != 1) {
                    getSupportActionBar().setTitle(getResources().getString(R.string.quest_history_title));
                    mFab.setVisibility(View.GONE);
                    Fragment fragment = PastQuestFragment.newInstance();
                    mCurrentFragment = fragment;
                    mCurrentPage = 1;

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();

                    // Highlight the selected item, update the title, and close the drawer
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                }
                mDrawerLayout.closeDrawer(mDrawer);
                break;
            case 2:
                if (mCurrentPage != 2) {
                    getSupportActionBar().setTitle(getResources().getString(R.string.change_password_title));
                    mFab.setVisibility(View.GONE);
                    Fragment passwordFragment = new ChangePasswordFragment();
                    mCurrentFragment = passwordFragment;
                    mCurrentPage = 2;

                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager2 = getSupportFragmentManager();
                    fragmentManager2.beginTransaction()
                            .replace(R.id.content_frame, passwordFragment)
                            .commit();

                    // Highlight the selected item, update the title, and close the drawer
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                }
                mDrawerLayout.closeDrawer(mDrawer);
                break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPage == 0) super.onBackPressed(); // Exit app
        else selectItem(0); // Go home
    }

    /**
     * Call when player dies. Resets level, xp, and health
     */
    public void onPlayerDeath() {
        PlayerDeathDialog dialog = new PlayerDeathDialog();
        dialog.show(getSupportFragmentManager(), "DeathDialog");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference statsRef = ref.child("users").child(auth.getCurrentUser().getUid()).child("stats");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    PlayerStats stats = dataSnapshot.getValue(PlayerStats.class);
                    stats.level = 1;
                    stats.hp = PlayerStats.START_MAX_HP;
                    stats.maxHp = PlayerStats.START_MAX_HP;
                    stats.xp = 0;

                    dataSnapshot.getRef().setValue(stats);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Very ugly method that launches showcase views consecutively. Hopefully can clean this up later
     */
    public void launchTutorial() {
        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(findViewById(R.id.level_text)))
                .setContentTitle(getResources().getString(R.string.showcase_title_level))
                .setContentText(getResources().getString(R.string.showcase_text_level))
                .hideOnTouchOutside()
                .setStyle(R.style.CustomShowcaseTheme)
                .withMaterialShowcase()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        new ShowcaseView.Builder(MainActivity.this)
                                .setTarget(new ViewTarget(findViewById(R.id.hp_container)))
                                .setContentTitle(getResources().getString(R.string.showcase_title_hp))
                                .setContentText(getResources().getString(R.string.showcase_text_hp))
                                .hideOnTouchOutside()
                                .setStyle(R.style.CustomShowcaseTheme)
                                .withMaterialShowcase()
                                .setShowcaseEventListener(new OnShowcaseEventListener() {
                                    @Override
                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                        new ShowcaseView.Builder(MainActivity.this)
                                                .setTarget(new ViewTarget(findViewById(R.id.gold_container)))
                                                .setContentTitle(getResources().getString(R.string.showcase_title_gold))
                                                .setContentText(getResources().getString(R.string.showcase_text_gold))
                                                .hideOnTouchOutside()
                                                .setStyle(R.style.CustomShowcaseTheme)
                                                .withMaterialShowcase()
                                                .setShowcaseEventListener(new OnShowcaseEventListener() {
                                                    @Override
                                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                                        ShowcaseView view = new ShowcaseView.Builder(MainActivity.this)
                                                                .setTarget(new ViewTarget(findViewById(R.id.addFAB)))
                                                                .setContentTitle(getResources().getString(R.string.showcase_title_add))
                                                                .setContentText(getResources().getString(R.string.showcase_text_add))
                                                                .hideOnTouchOutside()
                                                                .setStyle(R.style.CustomShowcaseTheme)
                                                                .withMaterialShowcase()
                                                                .setShowcaseEventListener(new OnShowcaseEventListener() {
                                                                    @Override
                                                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                                                        new ShowcaseView.Builder(MainActivity.this)
                                                                                .setTarget(new PointTarget(75,150))
                                                                                .setContentTitle(getResources().getString(R.string.showcase_title_drawer))
                                                                                .setContentText(getResources().getString(R.string.showcase_text_drawer))
                                                                                .hideOnTouchOutside()
                                                                                .setStyle(R.style.CustomShowcaseTheme)
                                                                                .withMaterialShowcase()
                                                                                .setShowcaseEventListener(new OnShowcaseEventListener() {
                                                                                    @Override
                                                                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

                                                                                    }

                                                                                    @Override
                                                                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                                                                    }

                                                                                    @Override
                                                                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                                                                    }

                                                                                    @Override
                                                                                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                                                                    }
                                                                                })
                                                                                .build();
                                                                    }

                                                                    @Override
                                                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                                                    }

                                                                    @Override
                                                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                                                    }

                                                                    @Override
                                                                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                                                    }
                                                                })
                                                                .build();
                                                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                                        params.bottomMargin = 25;
                                                        params.leftMargin = 25;
                                                        view.setButtonPosition(params);
                                                    }

                                                    @Override
                                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                                    }

                                                    @Override
                                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                                    }

                                                    @Override
                                                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                                    }
                                                })
                                                .build();
                                    }

                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                    }

                                    @Override
                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                    }

                                    @Override
                                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                    }
                                })
                                .build();
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                    }
                })
                .build();
    }

}
