package com.levipayne.liferpg;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity implements QuestFragment.OnListFragmentInteractionListener, RewardFragment.OnListFragmentInteractionListener {

    static final int NUM_ITEMS = 2;
    private final int QUEST_REQUEST_CODE = 1;
    private final int REWARD_REQUEST_CODE = 2;
    private final int QUEST_DETAIL_CODE = 3;
    private int mCurrentTabPos;
    private View mFab;
    private QuestFragment mQuestFrag;
    private RewardFragment mRewardFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentTabPos = 0;

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
                        startActivityForResult(intent, QUEST_REQUEST_CODE);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, AddRewardActivity.class);
                        startActivityForResult(intent, REWARD_REQUEST_CODE);
                        break;
                    default:
                }
            }
        });
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Quest item) {
        Intent intent = new Intent(this, QuestDetailsActivity.class);
        intent.putExtra("quest", item);
        startActivityForResult(intent,QUEST_DETAIL_CODE);
    }

    @Override
    public void onListFragmentInteraction(Reward item) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QUEST_REQUEST_CODE) {
            if(resultCode == RESULT_OK){
                Quest quest = (Quest) data.getSerializableExtra("quest");
                if (mQuestFrag == null) {
                    FragmentManager fm = getSupportFragmentManager();
                    List<Fragment> fragments = fm.getFragments();
                    if (fragments != null) {
                        for (Fragment f : fragments) {
                            if (f instanceof QuestFragment) mQuestFrag = (QuestFragment) f;
                            else if (f instanceof RewardFragment) mRewardFrag = (RewardFragment) f;
                        }
                    }
                }
                mQuestFrag.addQuest(quest);
            }
        }
        else if (requestCode == REWARD_REQUEST_CODE && resultCode == RESULT_OK) {
            Reward reward = (Reward) data.getSerializableExtra("reward");
            if (mRewardFrag == null) {
                FragmentManager fm = getSupportFragmentManager();
                List<Fragment> fragments = fm.getFragments();
                if (fragments != null) {
                    for (Fragment f : fragments) {
                        if (f instanceof QuestFragment) mQuestFrag = (QuestFragment) f;
                        else if (f instanceof RewardFragment) mRewardFrag = (RewardFragment) f;
                    }
                }
            }
            mRewardFrag.addReward(reward);
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



}
