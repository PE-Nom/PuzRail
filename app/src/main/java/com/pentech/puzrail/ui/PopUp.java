package com.pentech.puzrail.ui;

import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.PopupWindow;

import com.pentech.puzrail.R;

/**
 * Created by c0932 on 2017/03/30.
 */

public class PopUp implements View.OnClickListener {
    PopupWindow mPopupWindow;

    public static PopUp makePopup(Activity act, View parent, String url){
        return new PopUp(act,parent,url);
    }

    @Override
    public void onClick(View v) {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    public PopUp(Activity act, View parent, String url){

        mPopupWindow = new PopupWindow(act.getApplicationContext());

        // レイアウト設定
        View popupView = act.getLayoutInflater().inflate(R.layout.popup_layout, null);
        WebView wv = (WebView) popupView.findViewById(R.id.webview);
        wv.loadUrl(url);

        popupView.findViewById(R.id.closeBtn).setOnClickListener(this);

        mPopupWindow.setContentView(popupView);

        // 背景設定
        mPopupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(act.getResources(),R.drawable.popup_background,null));

        // タップ時に他のViewでキャッチされないための設定
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, act.getResources().getDisplayMetrics());
//        mPopupWindow.setWidth((int) width);
        mPopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // 画面中央に表示
        mPopupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }
}
