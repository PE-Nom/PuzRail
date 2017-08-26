package com.pentech.puzrail.piecegarally;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.pentech.puzrail.MainActivity;
import com.pentech.puzrail.R;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.database.Line;
import com.pentech.puzrail.database.SettingParameter;
import com.pentech.puzrail.location.LocationPuzzleActivity;
import com.pentech.puzrail.station.StationPuzzleActivity;
import com.pentech.puzrail.tutorial.TutorialActivity;
import com.pentech.puzrail.ui.GaugeView;
import com.pentech.puzrail.ui.MultiButtonListView;
import com.google.android.gms.common.api.GoogleApiClient;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PieceGarallyActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        AbsListView.OnScrollListener,
        NendAdListener {

    private static String TAG = "PieceGarallyActivity";
    private static final int RESULTCODE = 1;
    private MultiButtonListView listView;
//    private ListView listView;
    private RailwayListAdapter lineListAdapter;
    private DBAdapter db;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private TextView silhouetteProgValue, locationProgValue, stationsProgValue;
    private TextView silhouetteProgDenom, locationProgDenom, stationsProgDenom;
    private GaugeView silhouetteProgress, locationProgress,stationsProgress;
    private int selectedLineIndex = -1;
    private int companyId;
    private int previewAnswerCount = 0;
    private static final int showAnswerMax = 5;
    private int onReceiveAdCnt = 0;

    private SettingParameter settingParameter;
    private FloatingActionButton mFab;
    private boolean fabVisible = true;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_garally);

        db = new DBAdapter(this);
        db.open();

        Intent intent = getIntent();
        this.companyId = intent.getIntExtra("SelectedCompanyId", 3); // デフォルトを西日本旅客鉄道のIdにしておく
        this.previewAnswerCount = intent.getIntExtra("previewAnswerCount",0);

        this.lines = db.getLineList(this.companyId, false);

        this.silhouetteProgDenom = (TextView) findViewById(R.id.silhouetteProgDenominator);
        this.silhouetteProgValue = (TextView) findViewById(R.id.silhouetteProgValue);
        this.silhouetteProgress = (GaugeView) findViewById(R.id.silhouetteProgress) ;
        updateSilhouetteProgress();

        this.locationProgDenom = (TextView) findViewById(R.id.locationProgDenominator);
        this.locationProgValue = (TextView) findViewById(R.id.locationProgValue);
        this.locationProgress =(GaugeView) findViewById(R.id.locationProgress);
        updateLocationProgress();

        this.stationsProgDenom = (TextView) findViewById(R.id.stationsProgDenominator);
        this.stationsProgValue = (TextView) findViewById(R.id.stationsProgValue);
        this.stationsProgress = (GaugeView) findViewById(R.id.stationsProgress);
        updateStationsProgress();

        // GridViewのインスタンスを生成
        this.listView = (MultiButtonListView) findViewById(R.id.railway_list_view);
//        this.listView = (ListView) findViewById(R.id.railway_list_view);
        this.lineListAdapter = new RailwayListAdapter(this.getApplicationContext(), this.lines, db);;
        this.listView.setAdapter(this.lineListAdapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("線路と駅パズル：路線シルエット");
        actionBar.setSubtitle(db.getCompany(this.companyId).getName());

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInformation();
            }
        });
        this.settingParameter = db.getSettingParameter();
        fabVisible = settingParameter.isFabVisibility();
        if(fabVisible){
            mFab.show();
        }
        else{
            mFab.hide();
        }
        this.listView.setOnScrollListener(this);

        NendAdView nendAdView = (NendAdView) findViewById(R.id.nend);
        nendAdView.setListener(this);
        nendAdView.loadAd();

    }

    @Override
    public void onReceiveAd(NendAdView nendAdView) {
        Log.d(TAG,String.format("onReceiveAd onReceiveAdCnt = %d",this.onReceiveAdCnt));
        this.onReceiveAdCnt++;
    }

    @Override
    public void onFailedToReceiveAd(NendAdView nendAdView) {
        Log.d(TAG,"onFailedToReceiveAd");
    }

    @Override
    public void onClick(NendAdView nendAdView) {
        Log.d(TAG,"onClick");
        this.previewAnswerCount = 0;
        this.onReceiveAdCnt = 0;
    }

    @Override
    public void onDismissScreen(NendAdView nendAdView) {
        Log.d(TAG,"onDismissScreen");
    }

    private void updateSilhouetteProgress(){
        int cnt = db.countSilhouetteAnsweredLines(this.companyId);
        int lineNameProgress = 100*cnt/this.lines.size();
        this.silhouetteProgress.setData(lineNameProgress,"%",  ContextCompat.getColor(this, R.color.color_90), 90, true);
//        this.silhouetteProgValue.setText(String.format("%d/%d",cnt,this.lines.size()));
        this.silhouetteProgValue.setText(String.format("%d",cnt));
        this.silhouetteProgDenom.setText(String.format("/%d",this.lines.size()));
    }

    private void updateLocationProgress(){
        int answeredLines = db.countLocationAnsweredLines(this.companyId);
        int locationProgress = 100*answeredLines/lines.size();
        this.locationProgress.setData(locationProgress,"%",  ContextCompat.getColor(this, R.color.color_60), 90, true);
//        this.locationProgValue.setText(String.format("%d/%d",answeredLines,lines.size()));
        this.locationProgValue.setText(String.format("%d",answeredLines));
        this.locationProgDenom.setText(String.format("/%d",this.lines.size()));
    }

    private void updateStationsProgress(){
        int answeredStations = db.countAnsweredStationsInCompany(this.companyId);
        int totalStations = db.countTotalStationsInCompany(this.companyId);
        int stationAnsweredProgress = 100*answeredStations/totalStations;
        this.stationsProgress.setData(stationAnsweredProgress,"%",  ContextCompat.getColor(this, R.color.color_30), 90, true);
//        this.stationsProgValue.setText(String.format("%d/%d",answeredStations,totalStations));
        this.stationsProgValue.setText(String.format("%d",answeredStations));
        this.stationsProgDenom.setText(String.format("/%d",totalStations));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, RESULTCODE);
        // アニメーションの設定
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
        db.close();
        finish();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int remainingItemCount = totalItemCount - (firstVisibleItem + visibleItemCount);
        if (PieceGarallyActivity.this.fabVisible && totalItemCount > visibleItemCount) {
            if (remainingItemCount > 0) {
                // SHow FAB Here
                PieceGarallyActivity.this.mFab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            } else {
                // Hide FAB Here
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) PieceGarallyActivity.this.mFab.getLayoutParams();
                int fab_bottomMargin = layoutParams.bottomMargin;
                PieceGarallyActivity.this.mFab.animate().translationY(PieceGarallyActivity.this.mFab.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    private AlertDialog mDialog;
    private void selectLineSilhouette(int position){
        // アイコンタップでTextViewにその名前を表示する
        Log.d(TAG, String.format("onItemLongClick position = %d", position));
        Line line = this.lines.get(position);
        if(!line.isNameCompleted()){
            this.selectedLineIndex = position;
            final ArrayList<Line> sortedRemainLines = new ArrayList<Line>();
            final ArrayList<Line> randomizedRemainLines = new ArrayList<Line>();

            //路線名　未正解の路線を抽出（lines→sortedRemainLines)
            Iterator<Line> lineIte = this.lines.iterator();
            while(lineIte.hasNext()){
                Line ln = lineIte.next();
                if(!ln.isNameCompleted()){
                    sortedRemainLines.add(ln);
                }
            }

            Random rnd = new Random();
            while(randomizedRemainLines.size()<sortedRemainLines.size()){
                // 0～未正解件数までの整数をランダムに生成
                int idx = rnd.nextInt(sortedRemainLines.size());
                Line fromLine = sortedRemainLines.get(idx);
                Iterator<Line> li = randomizedRemainLines.iterator();
                boolean already = false;
                while(li.hasNext()){
                    Line toLine = li.next();
                    if(toLine.getLineId() == fromLine.getLineId()){
                        already = true;
                        break;
                    }
                }
                if(!already){
                    randomizedRemainLines.add(fromLine);
                }
            }

            // 未正解路線のシルエットのグリッドビュー生成
            GridView remainLinesGridView = new GridView(this);
            remainLinesGridView.setNumColumns(4);
            remainLinesGridView.setVerticalSpacing(4);
            remainLinesGridView.setGravity(Gravity.CENTER);
            remainLinesGridView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.backgound_bg, null));

            RailwayGridAdapter remainLinesAdapter = new RailwayGridAdapter(this.getApplicationContext(), randomizedRemainLines );
            remainLinesGridView.setAdapter(remainLinesAdapter);
            remainLinesGridView.setOnItemClickListener(
                    // ダイアログ上の未正解アイテムがクリックされたら答え合わせする
                    new AdapterView.OnItemClickListener(){
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            mDialog.dismiss();

                            int correctAnswerIdx = PieceGarallyActivity.this.selectedLineIndex;
                            Line correctLine = (Line)(PieceGarallyActivity.this.lineListAdapter.getItem(correctAnswerIdx));
                            String correctLineName = correctLine.getRawName()+"("+correctLine.getRawKana()+")";

                            Line selectedLine = randomizedRemainLines.get(position);
                            String selectedLineName = selectedLine.getRawName()+"("+selectedLine.getRawKana()+")";

                            Log.d(TAG,String.format("correct %s, selected %s",correctLineName,selectedLineName));
                            //正解判定
                            if(correctLine.getLineId() == selectedLine.getLineId()){
                                Toast.makeText(PieceGarallyActivity.this,"正解!!! v(￣Д￣)v ", Toast.LENGTH_SHORT).show();
                                correctLine.setNameAnswerStatus();
                                PieceGarallyActivity.this.db.updateLineNameAnswerStatus(correctLine);
                                PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                                PieceGarallyActivity.this.updateSilhouetteProgress();
                            }
                            else{
                                Toast.makeText(PieceGarallyActivity.this,"残念･･･ Σ(￣ロ￣lll)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

            // ダイアログ表示
            mDialog = new AlertDialog.Builder(this)
                    .setTitle(String.format("｢%s｣は？",line.getRawName()))
                    .setPositiveButton("Cancel",null)
                    .setView(remainLinesGridView)
                    .create();
            mDialog.show();

        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Line line = this.lines.get(position);
        switch(view.getId()){
            case R.id.mapImageButton: {
                    if(line.isNameCompleted()){
                        Intent intent = new Intent(PieceGarallyActivity.this, LocationPuzzleActivity.class);
                        intent.putExtra("SelectedLineId", line.getLineId());
                        intent.putExtra("previewAnswerCount", this.previewAnswerCount);
                        startActivity(intent);
                        // アニメーションの設定
                        overridePendingTransition(R.anim.in_right, R.anim.out_left);
                        db.close();
                        finish();
                    }
                }
                break;
            case R.id.stationImageButton : {
                    if(line.isNameCompleted()){
                        Intent intent = new Intent(PieceGarallyActivity.this, StationPuzzleActivity.class);
                        intent.putExtra("SelectedLineId", line.getLineId());
                        intent.putExtra("previewAnswerCount", this.previewAnswerCount);
                        startActivity(intent);
                        // アニメーションの設定
                        overridePendingTransition(R.anim.in_right, R.anim.out_left);
                        db.close();
                        finish();
                    }
                }
                break;
            default:
                selectLineSilhouette(position);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     * <p>
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(0);
        if(fabVisible){
            item.setTitle("iボタンを消す");
        }
        else{
            item.setTitle("iボタンを表示");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // --------------------
    // ワンポイント　チュートリアルの表示
    private PopupWindow onePointTutorial = null;
    private Timer mOnePointTutorialDisplayingTimer = null;
    private Handler tutorialTimerHandler = new Handler();
    private final static long TUTORIAL_DISPLAY_TIME = 1000*5;
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

    private void showInformation(){
        if(onePointTutorial == null){

            onePointTutorial = new PopupWindow(this);
            // レイアウト設定
            View popupView = getLayoutInflater().inflate(R.layout.one_point_tutorial_popup, null);

            // ワンポイント　アドバイスのテキスト
            TextView information = (TextView)popupView.findViewById(R.id.information);
            information.setText("■シルエット■をタップして正解すると\n■地図合わせ■、■駅並べ■が遊べるよ");

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
                    Intent intent = new Intent(PieceGarallyActivity.this, TutorialActivity.class);
                    intent.putExtra("page", 2);
                    startActivity(intent);
                }
            });
            onePointTutorial.setContentView(popupView);

            // 背景設定
            Drawable background = ResourcesCompat.getDrawable(this.getResources(), R.drawable.popup_background, null);
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
            onePointTutorial.showAtLocation(findViewById(R.id.silhouetteProgValue), Gravity.BOTTOM, 0, 0);

            mOnePointTutorialDisplayingTimer = new Timer(true);
            mOnePointTutorialDisplayingTimer.schedule(new tutorialDisplayTimerElapse(),TUTORIAL_DISPLAY_TIME);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_information) {
            if(fabVisible){
                fabVisible = false;
                mFab.hide();
                item.setTitle("iボタンを表示");
                Log.d(TAG,String.format("visibility = %b",fabVisible));
            }
            else{
                fabVisible = true;
                mFab.show();
                item.setTitle("iボタンを消す");
                Log.d(TAG,String.format("visibility = %b",fabVisible));
            }
            settingParameter.setFabVisibility(fabVisible);
            PieceGarallyActivity.this.db.updateFabVisibility(fabVisible);
            return true;
        }
        else if (id == R.id.action_AboutPuzzRail) {
            Intent intent = new Intent(PieceGarallyActivity.this, TutorialActivity.class);
            intent.putExtra("page", 0);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_Help) {
            Intent intent = new Intent(PieceGarallyActivity.this, TutorialActivity.class);
            intent.putExtra("page", 2);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_Ask) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "puzrail@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "「線路と駅」のお問い合わせ");
            startActivity(Intent.createChooser(intent, ""));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 回答クリアの対象選択
    private Line longClickSelectedLine = null;

    int answerClearSelectedItem = 0;
    private void answerClear(){
        final String[] items = {"｢路線シルエット｣と\n｢地図合わせ｣と\n｢駅並べ｣", "｢地図合わせ｣だけ", "｢駅並べ｣だけ"};

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(longClickSelectedLine.getRawName() +" : 回答クリア");
        alertDialog.setSingleChoiceItems(items, answerClearSelectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,String.format("which = %d",which));
                    answerClearSelectedItem = which;
                }
            });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,String.format("which = %d",which));
                switch (answerClearSelectedItem){
                    case 0:
                        // 路線シルエット
                        // Log.d(TAG,String.format("%s:路線シルエットのクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                        PieceGarallyActivity.this.longClickSelectedLine.resetNameAnswerStatus();
                        PieceGarallyActivity.this.db.updateLineNameAnswerStatus(PieceGarallyActivity.this.longClickSelectedLine);
                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                        PieceGarallyActivity.this.updateSilhouetteProgress();

                        // 地図合わせ
                        // Log.d(TAG,String.format("%s:地図合わせのクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                        PieceGarallyActivity.this.longClickSelectedLine.resetLocationAnswerStatus();
                        PieceGarallyActivity.this.db.updateLineLocationAnswerStatus(PieceGarallyActivity.this.longClickSelectedLine);
                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                        PieceGarallyActivity.this.updateLocationProgress();

                        // 駅並べ
                        // Log.d(TAG,String.format("%s:駅並べのクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                        // longClickSelectedLine.getLineId()で指定される路線の初ターミナルを除くすべて駅の回答ステータスを変更する
                        PieceGarallyActivity.this.db.updateStationsAnswerStatusInLine(PieceGarallyActivity.this.longClickSelectedLine.getLineId(),false);
                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                        PieceGarallyActivity.this.updateStationsProgress();
                        break;
                    case 1:
                        // Log.d(TAG,String.format("%s:地図合わせのクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                        PieceGarallyActivity.this.longClickSelectedLine.resetLocationAnswerStatus();
                        PieceGarallyActivity.this.db.updateLineLocationAnswerStatus(PieceGarallyActivity.this.longClickSelectedLine);
                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                        PieceGarallyActivity.this.updateLocationProgress();
                        break;
                    case 2:
                        // Log.d(TAG,String.format("%s:駅並べのクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                        // longClickSelectedLine.getLineId()で指定される路線の初ターミナルを除くすべて駅の回答ステータスを変更する
                        PieceGarallyActivity.this.db.updateStationsAnswerStatusInLine(PieceGarallyActivity.this.longClickSelectedLine.getLineId(),false);
                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                        PieceGarallyActivity.this.updateStationsProgress();
                        break;
                    default:
                        break;
                    }
                }
            });
        alertDialog.setNegativeButton("Cancel", null);
        alertDialog.show();
    }

    // --------------------
    // 路線シルエットの回答表示
    private AlertDialog railwaySlhouetteShowDialog = null;
    private Timer mAnswerDisplayingTimer = null;
    private Handler mHandler = new Handler();
    private final static long DISPLAY_ANSWER_TIME = 1000*3;
    // 回答表示の消去
    private class answerDisplayTimerElapse extends TimerTask {
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            mHandler.post(new Runnable(){
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
                    if(railwaySlhouetteShowDialog !=null){
                        railwaySlhouetteShowDialog.dismiss();
                        railwaySlhouetteShowDialog = null;
                    }
                    mAnswerDisplayingTimer = null;
                }
            });
        }
    }

    private void showRailwaySilhouetteAnswer(Line line){
        final ArrayList<Line> lines = new ArrayList<Line>();
        lines.add(line);

        if(railwaySlhouetteShowDialog ==null) {
            GridView answerShowLinesGridView = new GridView(this);
            answerShowLinesGridView.setVerticalSpacing(40);
            answerShowLinesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            answerShowLinesGridView.setGravity(Gravity.CENTER);
            answerShowLinesGridView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.backgound_bg, null));

            RailwayGridAdapter answerShowLinesAdapter = new RailwayGridAdapter(this.getApplicationContext(), lines );
            answerShowLinesGridView.setAdapter(answerShowLinesAdapter);
            // ダイアログ表示
            railwaySlhouetteShowDialog = new AlertDialog.Builder(this)
                    .setTitle(line.getRawName()+"("+line.getRawKana()+")のシルエット")
                    .setPositiveButton("OK",null)
                    .setView(answerShowLinesGridView)
                    .create();
            railwaySlhouetteShowDialog.show();
        }
        mAnswerDisplayingTimer = new Timer(true);
        mAnswerDisplayingTimer.schedule(new answerDisplayTimerElapse(),DISPLAY_ANSWER_TIME);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        longClickSelectedLine = lines.get(position);

        final ArrayList<String> contextMenuList = new ArrayList<String>();
        contextMenuList.add("回答クリア");
        contextMenuList.add("回答を見る");
        contextMenuList.add("Webを検索する");

        ArrayAdapter<String> contextMenuAdapter
                = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,contextMenuList);

        // 未正解アイテムのリストビュー生成
        ListView contextMenuListView = new ListView(this);
        contextMenuListView.setAdapter(contextMenuAdapter);
        contextMenuListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        mDialog.dismiss();
                        switch(position) {
                            case 0: // 回答をクリア
                                answerClear();
                                break;
                            case 1: // 回答を見る
                                if(previewAnswerCount < showAnswerMax ){
                                    showRailwaySilhouetteAnswer(longClickSelectedLine);
                                    if(PieceGarallyActivity.this.onReceiveAdCnt > 1){
                                        previewAnswerCount++;
                                    }
                                }
                                else{
                                    final Snackbar sb = Snackbar.make(PieceGarallyActivity.this.listView,
                                            "広告クリックお願いしま～っす",
                                            Snackbar.LENGTH_SHORT);
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(PieceGarallyActivity.this, R.color.color_10));
                                    TextView textView = (TextView) sb.getView().findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(PieceGarallyActivity.this.getApplicationContext(), R.color.color_RED));
                                    sb.show();
                                }
                                break;
                            case 2: // Webを検索する
                                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                intent.putExtra(SearchManager.QUERY, longClickSelectedLine.getRawName()); // query contains search string
                                startActivity(intent);
                                break;
                        }
                    }
                }
        );

        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", longClickSelectedLine.getRawName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;
    }

}
