package com.pentech.puzrail.piecegarally;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.pentech.puzrail.MainActivity;
import com.pentech.puzrail.R;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.database.Line;
import com.pentech.puzrail.location.LocationPuzzleActivity;
import com.pentech.puzrail.station.StationPuzzleActivity;
import com.pentech.puzrail.ui.GaugeView;
import com.pentech.puzrail.ui.MultiButtonListView;
import com.pentech.puzrail.ui.PopUp;
import com.google.android.gms.common.api.GoogleApiClient;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PieceGarallyActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,NendAdListener {

    private static String TAG = "PieceGarallyActivity";
    private static final int RESULTCODE = 1;
    private MultiButtonListView listView;
//    private ListView listView;
    private RailwayListAdapter lineListAdapter;
    private DBAdapter db;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private TextView lineNameProgValue,lineMapProgValue,stationProgValue;
    private GaugeView lineNameProgress, lineMapProgress,stationsProgress;
    private int selectedLineIndex = -1;
    private int companyId;
    private int previewAnswerCount = 0;
    private static final int showAnswerMax = 5;
    private int onReceiveAdCnt = 0;

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

        this.lineNameProgValue = (TextView) findViewById(R.id.lineNameProgValue);
        this.lineNameProgress = (GaugeView) findViewById(R.id.lineNameProgress) ;
        updateLineNameProgress();

        this.lineMapProgValue = (TextView) findViewById(R.id.lineMapProgValue);
        this.lineMapProgress =(GaugeView) findViewById(R.id.lineMapProgress);
        updateLocationProgress();

        this.stationProgValue = (TextView) findViewById(R.id.stationProgValue);
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

    private void updateLineNameProgress(){
        int cnt = db.countLineNameAnsweredLines(this.companyId);
        int lineNameProgress = 100*cnt/this.lines.size();
        this.lineNameProgress.setData(lineNameProgress,"%",  ContextCompat.getColor(this, R.color.color_90), 90, true);
        this.lineNameProgValue.setText(String.format("%d/%d",cnt,this.lines.size()));
    }

    private void updateLocationProgress(){
        int answeredLines = db.countLocationAnsweredLines(this.companyId);
        int locationProgress = 100*answeredLines/lines.size();
        this.lineMapProgress.setData(locationProgress,"%",  ContextCompat.getColor(this, R.color.color_60), 90, true);
        this.lineMapProgValue.setText(String.format("%d/%d",answeredLines,lines.size()));
    }

    private void updateStationsProgress(){
        int answeredStations = db.countAnsweredStationsInCompany(this.companyId);
        int totalStations = db.countTotalStationsInCompany(this.companyId);
        int stationAnsweredProgress = 100*answeredStations/totalStations;
        this.stationsProgress.setData(stationAnsweredProgress,"%",  ContextCompat.getColor(this, R.color.color_30), 90, true);
        this.stationProgValue.setText(String.format("%d/%d",answeredStations,totalStations));
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
//            remainLinesGridView.setHorizontalSpacing(2);
            remainLinesGridView.setVerticalSpacing(4);
//            remainLinesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
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
                                PieceGarallyActivity.this.updateLineNameProgress();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_AboutPuzzRail) {
            PopUp.makePopup(this,this.listView,"file:///android_asset/about_puzrail.html");
            return true;
        }
        else if (id == R.id.action_Help) {
            PopUp.makePopup(this,this.listView,"file:///android_asset/help_puzrail.html");
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
        alertDialog.setTitle(longClickSelectedLine.getName() +" : 回答クリア");
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
                        PieceGarallyActivity.this.updateLineNameProgress();

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

//    private PopupWindow railwaySlhouetteShowPopup = null;
    private AlertDialog railwaySlhouetteShowPopup = null;
    private Timer mAnswerDisplayingTimer = null;
    private Handler mHandler = new Handler();
    private final static long DISPLAY_ANSWER_TIME = 1000*3;
    // 回答表示の消去
    private class displayTimerElapse extends TimerTask {
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
                    if(railwaySlhouetteShowPopup!=null){
                        railwaySlhouetteShowPopup.dismiss();
                        railwaySlhouetteShowPopup = null;
                    }
                    mAnswerDisplayingTimer = null;
                }
            });
        }
    }

    private void showRailwaySilhouetteAnswer(Line line){
        final ArrayList<Line> lines = new ArrayList<Line>();
        lines.add(line);

        if(railwaySlhouetteShowPopup==null) {
            GridView answerShowLinesGridView = new GridView(this);
            answerShowLinesGridView.setVerticalSpacing(40);
            answerShowLinesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            answerShowLinesGridView.setGravity(Gravity.CENTER);
            answerShowLinesGridView.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.backgound_bg, null));

            RailwayGridAdapter answerShowLinesAdapter = new RailwayGridAdapter(this.getApplicationContext(), lines );
            answerShowLinesGridView.setAdapter(answerShowLinesAdapter);
            // ダイアログ表示
            railwaySlhouetteShowPopup = new AlertDialog.Builder(this)
                    .setTitle(line.getRawName()+"("+line.getRawKana()+")のシルエット")
                    .setPositiveButton("OK",null)
                    .setView(answerShowLinesGridView)
                    .create();
            railwaySlhouetteShowPopup.show();
        }
        mAnswerDisplayingTimer = new Timer(true);
        mAnswerDisplayingTimer.schedule(new displayTimerElapse(),DISPLAY_ANSWER_TIME);
    }

/*    private void showRailwaySilhouetteAnswer(Line line){
        if(railwaySlhouetteShowPopup==null){

            railwaySlhouetteShowPopup = new PopupWindow(this);
            // レイアウト設定
            View popupView = getLayoutInflater().inflate(R.layout.railway_silhouette_answer_show_popup, null);

            // タイトル設定
            TextView popupTitle = (TextView)popupView.findViewById(R.id.railway_silhouette_answer_popup_title);
            popupTitle.setText(line.getRawName()+"("+line.getRawKana()+")のシルエット");

            //　railwayシルエット設定
            ImageView silhouetteView = (ImageView)popupView.findViewById(R.id.railway_silhouette_answer_popup_view);
            Drawable lineDrawable = ResourcesCompat.getDrawable(this.getResources(), line.getDrawableResourceId(), null);
            silhouetteView.setImageDrawable(lineDrawable);

            // closeボタン設定
            Button closeBtn = (Button)popupView.findViewById(R.id.railway_silhouette_answer_popup_button);
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (railwaySlhouetteShowPopup.isShowing()) {
                        railwaySlhouetteShowPopup.dismiss();
                        railwaySlhouetteShowPopup = null;
                    }
                }
            });

            railwaySlhouetteShowPopup.setContentView(popupView);

            // 背景設定
            Drawable background = ResourcesCompat.getDrawable(this.getResources(), R.drawable.popup_background, null);
            railwaySlhouetteShowPopup.setBackgroundDrawable(background);

            // タップ時に他のViewでキャッチされないための設定
            railwaySlhouetteShowPopup.setOutsideTouchable(true);
            railwaySlhouetteShowPopup.setFocusable(true);

            // 画面中央に表示
            railwaySlhouetteShowPopup.showAtLocation(findViewById(R.id.lineNameProgValue), Gravity.BOTTOM, 0, 0);

            mAnswerDisplayingTimer = new Timer(true);
            mAnswerDisplayingTimer.schedule(new displayTimerElapse(),DISPLAY_ANSWER_TIME);
        }
    }
*/
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
/*                                    final Snackbar sb = Snackbar.make(PieceGarallyActivity.this.listView,
                                            longClickSelectedLine.getRawName()+"("+longClickSelectedLine.getRawKana()+")",
                                            Snackbar.LENGTH_SHORT);
                                    sb.setActionTextColor(ContextCompat.getColor(PieceGarallyActivity.this, R.color.background1));
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(PieceGarallyActivity.this, R.color.color_10));
                                    sb.show();
*/
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
                .setTitle(String.format("%s", longClickSelectedLine.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;
    }

}
