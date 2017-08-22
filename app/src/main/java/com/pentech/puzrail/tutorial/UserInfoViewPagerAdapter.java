package com.pentech.puzrail.tutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by takashi on 2017/08/19.
 */

public class UserInfoViewPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_NUM = 5;
    private static String urlString[] = {
            "file:///android_asset/about_puzrail.html",
            "file:///android_asset/help_puzrail.html",
            "file:///android_asset/help_puzrail.html",
            "file:///android_asset/help_puzrail.html",
            "file:///android_asset/help_puzrail.html",
    };

    public UserInfoViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                AboutFragment aboutFragment = new AboutFragment();
                aboutFragment.setUrl(this.urlString[position]);
                fragment = aboutFragment;
                break;
            case 1:
                CompanyFragment companyFragment = new CompanyFragment();
                companyFragment.setUrl(this.urlString[position]);
                fragment = companyFragment;
                break;
            case 2:
                SilhouetteFragment silhouetteFragment = new SilhouetteFragment();
                silhouetteFragment.setUrl(this.urlString[position]);
                fragment = silhouetteFragment;
                break;
            case 3:
                LocationFragment locationFragment = new LocationFragment();
                locationFragment.setUrl(this.urlString[position]);
                fragment = locationFragment;
                break;
            case 4:
                StationFragment stationFragment= new StationFragment();
                stationFragment.setUrl(this.urlString[position]);
                fragment = stationFragment;
                break;
            default:
        }
        return fragment;
    }

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        String pageTitle = "None";
        switch(position){
            case 0:
                pageTitle = "「線路と駅」について";
                break;
            case 1:
                pageTitle = "「事業者選択」の遊び方";
                break;
            case 2:
                pageTitle = "「路線シルエット」の遊び方";
                break;
            case 3:
                pageTitle = "「地図合わせ」の遊び方";
                break;
            case 4:
                pageTitle = "「駅並べ」の遊び方";
                break;
            default:
                break;
        }
        return pageTitle;
    }

    @Override
    public int getCount() {
        return PAGE_NUM;
    }

}
