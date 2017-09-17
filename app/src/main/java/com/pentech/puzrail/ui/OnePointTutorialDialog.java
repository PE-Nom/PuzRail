package com.pentech.puzrail.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pentech.puzrail.R;
import com.pentech.puzrail.tutorial.TutorialActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by c0932 on 2017/08/30.
 */

public class OnePointTutorialDialog {

    public static int _ABOUT_ = 0;
    public static int _COMPANY_ = 1;
    public static int _SILHOUETTE_ =2;
    public static int _LOCATION_ = 3;
    public static int _STATION_ = 4;
    private AppCompatActivity activity;
    private PopupWindow onePointTutorial = null;
    private Timer mOnePointTutorialDisplayingTimer = null;
    private Handler tutorialTimerHandler = new Handler();
    private final static long TUTORIAL_DISPLAY_TIME = 1000*5;
    private int detailPage;
    private int rid;

    public OnePointTutorialDialog(AppCompatActivity activity,int detailPage, int rid){
        this.activity = activity;
        this.detailPage = detailPage;
        this.rid = rid;
    }

    // ワンポイント チュートリアルの表示の消去
    private class tutorialDisplayTimerElapse extends TimerTask {
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            tutorialTimerHandler.post(new Runnable(){
                /**
                 * When an object implementing interface <code>Runnable</code> is used
                 * to create a thread, starting the thread causes the object's
                 * <code>run</code> method to be called in that separately executing
                 * thread.
                 * <p>
                 * The general contract of the method <code>run</code> is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {
                    if(onePointTutorial !=null){
                        onePointTutorial.dismiss();
                        onePointTutorial = null;
                    }
                    mOnePointTutorialDisplayingTimer = null;
                }
            });
        }
    }

    public void show(){
        if( onePointTutorial == null ){

            onePointTutorial = new PopupWindow(this.activity);
            // レイアウト設定
            View popupView = this.activity.getLayoutInflater().inflate(R.layout.one_point_tutorial_popup, null);

            // ワンポイント　アドバイスのテキスト
            TextView information = (TextView)popupView.findViewById(R.id.information);
            information.setText(createMessage(this.detailPage));

            // 「詳しく...」ボタン設定
            Button moreBtn = (Button)popupView.findViewById(R.id.more);
            moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onePointTutorial.isShowing()) {
                        onePointTutorial.dismiss();
                        // この呼び出しでOnDismissListenerが呼び出されるので
                        // ここでは以下の呼び出しは不要（OnDismissListenerに委譲）
                        // onePointTutorial = null;
                        // mOnePointTutorialDisplayingTimer.cancel();
                        // mOnePointTutorialDisplayingTimer = null;
                    }
                    Intent intent = new Intent(OnePointTutorialDialog.this.activity, TutorialActivity.class);
                    intent.putExtra("page", detailPage);
                    OnePointTutorialDialog.this.activity.startActivity(intent);
                }
            });
            onePointTutorial.setContentView(popupView);

            // 背景設定
            Drawable background = ResourcesCompat.getDrawable(this.activity.getResources(), R.drawable.popup_background, null);
            onePointTutorial.setBackgroundDrawable(background);

            // タップ時に他のViewでキャッチされないための設定
            onePointTutorial.setOutsideTouchable(true);
            onePointTutorial.setFocusable(true);

            // Popup以外のタップでPopup消去
            // 「詳しく...」ボタンのOnClickListener.onClick()で呼び出すdismiss()でも呼び出される
            onePointTutorial.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (onePointTutorial != null) {
                        onePointTutorial = null;
                        mOnePointTutorialDisplayingTimer.cancel();
                        mOnePointTutorialDisplayingTimer = null;
                    }
                }
            });

            // 画面中央に表示
            onePointTutorial.showAtLocation(this.activity.findViewById(rid), Gravity.BOTTOM, 0, 0);

            mOnePointTutorialDisplayingTimer = new Timer(true);
            mOnePointTutorialDisplayingTimer.schedule(new tutorialDisplayTimerElapse(),TUTORIAL_DISPLAY_TIME);

        }
    }

    //　絵文字入りのメッセージ生成
    private SpannableStringBuilder createMessage(int page){
        SpannableStringBuilder ssb = new SpannableStringBuilder().append("none");
        switch(page){
            case 0:
                ssb = createTutorialMessage_ABOUT_();
                break;
            case 1:
                ssb = createTutorialMessage_COMPANY_();
                break;
            case 2:
                ssb = createTutorialMessage_SILHOUETTE_();
                break;
            case 3:
                ssb = createTutorialMessage_LOCATION_();
                break;
            case 4:
                ssb = createTutorialMessage_STATION_();
                break;
        }
        return ssb;
    }

    private SpannableStringBuilder createTutorialMessage_ABOUT_(){
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        int start,end;

        ssb.append("線路と駅について");

        return ssb;
    }

    private SpannableStringBuilder createTutorialMessage_COMPANY_(){
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        int start,end;

        ssb.append("運営会社をタップして始めます");

        return ssb;
    }

    private SpannableStringBuilder createTutorialMessage_SILHOUETTE_(){

        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        int start,end;

        ssb.append(" ");
        start = ssb.length();
        ssb.append('\uFFFC'); // Unicode replacement character
        end = ssb.length();
        ssb.setSpan(new EmojiImageSpan(this.activity, R.drawable.emoji_line_question), start, end, flag);
        ssb.append(" をタップして正解すると得点がもらえ、");
        ssb.append('\n');

        ssb.append(" ");
        start = ssb.length();
        ssb.append('\uFFFC'); // Unicode replacement character
        end = ssb.length();
        ssb.setSpan(new EmojiImageSpan(this.activity, R.drawable.emoji_tracklaying), start, end, flag);
        ssb.append(" と ");

        start = ssb.length();
        ssb.append('\uFFFC'); // Unicode replacement character
        end = ssb.length();
        ssb.setSpan(new EmojiImageSpan(this.activity, R.drawable.emoji_station_open), start, end, flag);
        ssb.append(" が遊べるようになります。");

        return ssb;
    }

    private SpannableStringBuilder createTutorialMessage_LOCATION_(){
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        int start,end;

        ssb.append(" ");
        start = ssb.length();
        ssb.append('\uFFFC'); // Unicode replacement character
        end = ssb.length();
        ssb.setSpan(new EmojiImageSpan(this.activity, R.drawable.emoji_silhouette_piece), start, end, flag);
        ssb.append(" と");

        ssb.append(" ");
        start = ssb.length();
        ssb.append('\uFFFC'); // Unicode replacement character
        end = ssb.length();
        ssb.setSpan(new EmojiImageSpan(this.activity, R.drawable.emoji_map_icon), start, end, flag);
        ssb.append(" の大きさ、位置を合わせると得点がもらえます");

        return ssb;
    }

    private SpannableStringBuilder createTutorialMessage_STATION_(){
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        final int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        int start,end;

        ssb.append(" ");
        start = ssb.length();
        ssb.append('\uFFFC'); // Unicode replacement character
        end = ssb.length();
        ssb.setSpan(new EmojiImageSpan(this.activity, R.drawable.emoji_out_of_sv_station), start, end, flag);
        ssb.append(" の駅をタップして正しい駅名を選ぶと得点がもらえます");

        return ssb;
    }
}
