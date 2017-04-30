package com.pentech.puzrail.piecegarally;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class PieceGarallyActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static String TAG = "PieceGarallyActivity";
    private static final int RESULTCODE = 1;
    private MultiButtonListView listView;
    private RailwayListAdapter lineListAdapter;
    private DBAdapter db;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private TextView lineNameProgValue,lineMapProgValue,stationProgValue;
    private GaugeView lineNameProgress, lineMapProgress,stationsProgress;
    private int selectedLineIndex = -1;
    private int companyId;
    private int previewAnswerCount = 0;
    private static final int showAnswerMax = 3;

    private AlertDialog mDialog;
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

        this.lines = db.getLineList(this.companyId, true);

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
        this.lineListAdapter = new RailwayListAdapter(this.getApplicationContext(), this.lines, db);;
        this.listView.setAdapter(this.lineListAdapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("パズレール：路線名当て");
        actionBar.setSubtitle(db.getCompany(this.companyId).getName());
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

    private void selectLineName(int position){
        // アイコンタップでTextViewにその名前を表示する
        Log.d(TAG, String.format("onItemLongClick position = %d", position));
        Line line = this.lines.get(position);
        if(!line.isNameCompleted()){
            this.selectedLineIndex = position;
            final ArrayList<Line> sortedRemainLines = new ArrayList<Line>();
            final ArrayList<String> randomizedRemainLines = new ArrayList<String>();

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
                Iterator<String> li = randomizedRemainLines.iterator();
                boolean already = false;
                while(li.hasNext()){
                    String toLineName = li.next();
                    if(toLineName.equals(fromLine.getRawName()+"("+fromLine.getRawKana()+")")){
                        already = true;
                        break;
                    }
                }
                if(!already){
                    randomizedRemainLines.add(fromLine.getRawName()+"("+fromLine.getRawKana()+")");
                }
            }

            ArrayAdapter<String> remainLinesAdapter
                    = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,randomizedRemainLines);

            // 未正解アイテムのリストビュー生成
            ListView remainLinesListView = new ListView(this);
            remainLinesListView.setAdapter(remainLinesAdapter);
            remainLinesListView.setOnItemClickListener(
                    // ダイアログ上の未正解アイテムがクリックされたら答え合わせする
                    new AdapterView.OnItemClickListener(){
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            mDialog.dismiss();
                            int correctAnswerIdx = PieceGarallyActivity.this.selectedLineIndex;
                            Line correctLine = (Line)(PieceGarallyActivity.this.lineListAdapter.getItem(correctAnswerIdx));
                            String correctLineName = correctLine.getRawName()+"("+correctLine.getRawKana()+")";
                            String selectedLineName = randomizedRemainLines.get(position);
                            Log.d(TAG,String.format("correct %s, selected %s",correctLineName,selectedLineName));
                            //正解判定
                            if(correctLineName.equals(selectedLineName)){
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
                    .setTitle("路線リスト")
                    .setPositiveButton("Cancel",null)
                    .setView(remainLinesListView)
                    .create();
            mDialog.show();

        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Line line = this.lines.get(position);
        switch(view.getId()){
            case R.id.mapImageButton: {
                    Intent intent = new Intent(PieceGarallyActivity.this, LocationPuzzleActivity.class);
                    intent.putExtra("SelectedLineId", line.getLineId());
                    intent.putExtra("previewAnswerCount", this.previewAnswerCount);
                    startActivity(intent);
                    // アニメーションの設定
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                    db.close();
                    finish();
                }
                break;
            case R.id.stationImageButton : {
                    Intent intent = new Intent(PieceGarallyActivity.this, StationPuzzleActivity.class);
                    intent.putExtra("SelectedLineId", line.getLineId());
                    intent.putExtra("previewAnswerCount", this.previewAnswerCount);
                    startActivity(intent);
                    // アニメーションの設定
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                    db.close();
                    finish();
                }
                break;
            default:
                selectLineName(position);
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
            PopUp.makePopup(this,this.listView,"file:///android_asset/puzzrail_help.html");
            return true;
        }
        else if (id == R.id.action_Help) {
            Toast.makeText(PieceGarallyActivity.this, "使い方", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.action_Ask) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "ib65629@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "パズレールについてのお問い合わせ");
            startActivity(Intent.createChooser(intent, ""));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 回答クリアの対象選択
    private Line longClickSelectedLine = null;
    private void answerClear(){
        final String[] items = {"路線名", "地図合わせ", "駅並べ（全駅）"};
        final Boolean[] checkedItems = {false,false,false};
        new AlertDialog.Builder(this)
                .setTitle(longClickSelectedLine.getName() +" : 回答クリア")
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for( int i=0; i<checkedItems.length; i++){
                            switch (i){
                                case 0:
                                    if(checkedItems[i]){
                                        Log.d(TAG,String.format("%s:路線名回答のクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                                        PieceGarallyActivity.this.longClickSelectedLine.resetNameAnswerStatus();
                                        PieceGarallyActivity.this.db.updateLineNameAnswerStatus(PieceGarallyActivity.this.longClickSelectedLine);
                                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                                        PieceGarallyActivity.this.updateLineNameProgress();
                                    }
                                    break;
                                case 1:
                                    if(checkedItems[i]){
                                        Log.d(TAG,String.format("%s:敷設回答のクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                                        PieceGarallyActivity.this.longClickSelectedLine.resetLocationAnswerStatus();
                                        PieceGarallyActivity.this.db.updateLineLocationAnswerStatus(PieceGarallyActivity.this.longClickSelectedLine);
                                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                                        PieceGarallyActivity.this.updateLocationProgress();
                                    }
                                    break;
                                case 2:
                                    if(checkedItems[i]){
                                        Log.d(TAG,String.format("%s:駅回答のクリア", PieceGarallyActivity.this.longClickSelectedLine.getName()));
                                        // longClickSelectedLine.getLineId()で指定される路線の初ターミナルを除くすべて駅の回答ステータスを変更する
                                        PieceGarallyActivity.this.db.updateStationsAnswerStatusInLine(PieceGarallyActivity.this.longClickSelectedLine.getLineId(),false);
                                        PieceGarallyActivity.this.lineListAdapter.notifyDataSetChanged();
                                        PieceGarallyActivity.this.updateStationsProgress();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                                    final Snackbar sb = Snackbar.make(PieceGarallyActivity.this.listView,
                                            longClickSelectedLine.getRawName()+"("+longClickSelectedLine.getRawKana()+")",
                                            Snackbar.LENGTH_SHORT);
                                    sb.setActionTextColor(ContextCompat.getColor(PieceGarallyActivity.this, R.color.background1));
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(PieceGarallyActivity.this, R.color.color_10));
                                    sb.show();
                                    previewAnswerCount++;
                                }
                                else{
                                    final Snackbar sb = Snackbar.make(PieceGarallyActivity.this.listView,
                                            "回数制限一杯!!　広告クリックを促す",
                                            Snackbar.LENGTH_SHORT);
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(PieceGarallyActivity.this, R.color.color_10));
                                    TextView textView = (TextView) sb.getView().findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(PieceGarallyActivity.this.getApplicationContext(), R.color.color_RED));
                                    sb.show();
                                }
                                break;
                            case 2: // Webを検索する
                                if(longClickSelectedLine.isNameCompleted()){
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, longClickSelectedLine.getName()); // query contains search string
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(PieceGarallyActivity.this,"路線名が未回答です。\n路線名を先に回答してください", Toast.LENGTH_SHORT).show();
                                }
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
