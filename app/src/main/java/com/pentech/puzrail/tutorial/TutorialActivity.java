package com.pentech.puzrail.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.pentech.puzrail.R;

/**
 * Created by takashi on 2017/08/19.
 */

public class TutorialActivity extends AppCompatActivity {
    private static String TAG = "TutorialMain";

    private ViewPager pager;
    private FragmentPagerAdapter adapter;
    private int currentPage;
    private PagerIndicator indicator;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_main);

        Intent intent = getIntent();
        this.currentPage = intent.getIntExtra("page", 0);
        Log.d(TAG,String.format("CurrentPage = %d",this.currentPage));

        pager = (ViewPager) findViewById(R.id.pager);
        container = (LinearLayout) findViewById(R.id.pager_dots_container);

        adapter = new UserInfoViewPagerAdapter(getSupportFragmentManager());
        indicator = new PagerIndicator(this,container,adapter.getCount());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(indicator);
        pager.setCurrentItem(currentPage);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void onClickNext(View view) {
        currentPage ++;
        pager.setCurrentItem(currentPage);
    }

    public void onClickGoToTop(View view) {
        currentPage = 0;
        pager.setCurrentItem(currentPage);
    }

}
