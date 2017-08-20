package com.pentech.puzrail.tutorial;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pentech.puzrail.R;

import java.util.ArrayList;

/**
 * Created by takashi on 2017/08/19.
 */

public class PagerIndicator implements ViewPager.OnPageChangeListener {
    private static String TAG = "PagerIndicator";

    private ArrayList<ImageView> dotsList = new ArrayList<>();
    private LinearLayout mLinearLayout;
    private Context mContext;

    public PagerIndicator(Context context, LinearLayout container, int size) {
        Log.d(TAG,String.format("PagerIndicator size = %d",size));
        this.mLinearLayout = container;
        this.mContext = context;
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                addView(true);
            } else {
                addView(false);
            }
        }
    }

    private void addView(boolean isSelected) {
        Log.d(TAG,String.format("addView"));
        ImageView v = new ImageView(mContext);
        int res = isSelected ? R.drawable.selected_dot : R.drawable.unselected_dot;
        v.setImageDrawable(ContextCompat.getDrawable(mContext, res));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = mContext.getResources().getDimensionPixelSize(R.dimen.margin_2dp);
        params.setMargins(margin, margin, margin, margin);
        dotsList.add(v);
        mLinearLayout.addView(v, params);
    }

    public void notifySizeChanged(int sizeUpdate) {
        if (dotsList.size() > sizeUpdate) {
            mLinearLayout.removeViews(sizeUpdate, dotsList.size() - sizeUpdate);
        } else if (dotsList.size() < sizeUpdate) {
            for (int i = 0; i < (sizeUpdate - dotsList.size()); i++) {
                addView(false);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d(TAG,String.format("onPageScrolled"));
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG,String.format("onPageSelected"));
        for (ImageView imageView : dotsList) {
            setUnSelectDot(imageView);
        }
        setSelectDot(dotsList.get(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(TAG,String.format("onPageScrollStateChanged"));

    }

    private void setSelectDot(ImageView view) {
        Log.d(TAG,String.format("setSelectDot"));
        view.setImageResource(R.drawable.selected_dot);
    }

    private void setUnSelectDot(ImageView view) {
        Log.d(TAG,String.format("setUnSelectDot"));
        view.setImageResource(R.drawable.unselected_dot);
    }
}
