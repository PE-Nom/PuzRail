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

    @Override
    public int getCount() {
        return PAGE_NUM;
    }

}
