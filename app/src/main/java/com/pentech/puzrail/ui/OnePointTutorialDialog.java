package com.pentech.puzrail.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pentech.puzrail.MainActivity;
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

    private static String tutorialMessages[] ={
            "線路と駅について",
            "運営会社をタップしてね",
            "■シルエット■をタップして正解すると\n■地図合わせ■、■駅並べ■が遊べるよ",
            "■シルエットピース■と■地図■の大きさ、位置を合わせてね",
            "■未回答■をタップして駅を正しい順に並べてね"
    };
    private String createMessage(int page){
        String str = "None";
        switch(page){
            case 0:
                str = this.tutorialMessages[OnePointTutorialDialog._ABOUT_];
                break;
            case 1:
                str = this.tutorialMessages[OnePointTutorialDialog._COMPANY_];
                break;
            case 2:
                str = this.tutorialMessages[OnePointTutorialDialog._SILHOUETTE_];
                break;
            case 3:
                str = this.tutorialMessages[OnePointTutorialDialog._LOCATION_];
                break;
            case 4:
                str = this.tutorialMessages[OnePointTutorialDialog._STATION_];
                break;
        }
        return str;
    }
}
