package com.pentech.puzrail.station;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pentech.puzrail.database.SettingParameter;
import com.pentech.puzrail.R;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.database.Line;
import com.pentech.puzrail.database.Station;
import com.pentech.puzrail.piecegarally.PieceGarallyActivity;
import com.pentech.puzrail.tutorial.TutorialActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pentech.puzrail.ui.OnePointTutorialDialog;
import com.pentech.puzrail.ui.SettingParameterDialog;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class StationPuzzleActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        AbsListView.OnScrollListener,
        OnMapReadyCallback,
        NendAdListener {

    private String TAG = "StationPuzzleActivity";
    private String lineNameNone = "------------";
    private String lineName;
    private String stationNameNone = "------------";
    private DBAdapter db;
    private Line line;
    private int companyId;
    private int selectedLineId;
    private ArrayList<Station> stations = new ArrayList<Station>();

    private GoogleMap mMap;
    private TextView stationsScore;
    private TextView progressTitle;
    private ProgressBar progress;
    private MapView mMapView;
    private GeoJsonLayer layer;
    private StationListAdapter stationsAdapter;
    private ListView stationListView;
    private AlertDialog mDialog;
    private int selectedStationIndex = -1;

    private ImageView separatorMove;
    private FrameLayout mapFrame;
    private LinearLayout transparentView;

    private int previewLineAnswerCount = 0;
    private final static long DISPLAY_ANSWER_TIME = 1000;
    private static final int showAnswerMax = 5;
    private int onReceiveAdCnt = 0;
    private int showAnswerCount = 0;

    private Timer mAnswerDisplayingTimer = null;
    private Handler mHandler = new Handler();

    private SettingParameter settingParameter;
    private FloatingActionButton mFab;
    private boolean fabVisible = true;

    private OnePointTutorialDialog onePointTutorial = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_station_puzzle);

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        Intent intent = getIntent();
        this.selectedLineId = intent.getIntExtra("SelectedLineId", 42); // デフォルトを紀勢線のlineIdにしておく
        this.previewLineAnswerCount = intent.getIntExtra("previewAnswerCount",0);

        this.db = new DBAdapter(this);
        this.db.open();
        this.line = db.getLine(this.selectedLineId);
        this.stations = db.getLineStationList(this.selectedLineId);

        String companyName = db.getCompany(line.getCompanyId()).getName();
        String lineName = line.getName();
        String linekana = line.getLineKana();
        this.lineName = lineName+"("+linekana+")";
        this.companyId = line.getCompanyId();

        actionBar.setTitle("線路と駅パズル：駅並べ");
        actionBar.setSubtitle(companyName+"／"+this.lineName);

        this.stationsScore = (TextView) findViewById(R.id.stationsScore);
        this.progressTitle = (TextView)findViewById(R.id.ProgressTitle);
        this.progress = (ProgressBar)findViewById(R.id.ProgressBar);
        updateProgressBar();

        this.mapFrame = (FrameLayout)findViewById(R.id.framelayout);
        this.transparentView = (LinearLayout)findViewById(R.id.linearlayout);

        this.mMapView = (MapView)findViewById(R.id.mapView);
        this.mMapView.onCreate(savedInstanceState);
        this.mMapView.getMapAsync(this);

        this.separatorMove = (ImageView)findViewById(R.id.separatorMove);
        this.separatorMove.setLongClickable(true);
        this.separatorMove.setOnTouchListener(new OnTouchListener(this));

        onePointTutorial = new OnePointTutorialDialog(this,OnePointTutorialDialog._STATION_,R.id.transparentView);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onePointTutorial.show();
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

        NendAdView nendAdView = (NendAdView) findViewById(R.id.nend);
        nendAdView.setListener(this);
        nendAdView.loadAd();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        UiSettings mUiSetting = this.mMap.getUiSettings();
        mUiSetting.setRotateGesturesEnabled(false);
        this.mMap.setMaxZoomPreference(this.line.getMaxZoomLevel());
        this.mMap.setMinZoomPreference(this.line.getMinZoomLevel());
        // 離島除く
        LatLng north_east = new LatLng(this.line.getScrollMaxLat(),this.line.getScrollMaxLng());
        LatLng south_west = new LatLng(this.line.getScrollMinLat(),this.line.getScrollMinLng());
        LatLngBounds JAPAN = new LatLngBounds(south_west,north_east);
        this.mMap.setLatLngBoundsForCameraTarget(JAPAN);

        //  初期表示位置
        double lineCenterLng = ( this.line.getCorrectLeftLng() + this.line.getCorrectRightLng() )/2.0;
        double lineCenterLat = ( this.line.getCorrectBottomLat() + this.line.getCorrectTopLat() )/2.0;
        Log.d(TAG,String.format("##### line center   : lng = %f, lat = %f",lineCenterLng,lineCenterLat));
        double zl = (this.line.getMaxZoomLevel() + this.line.getMinZoomLevel())/2.0;

        // 路線中心座標で位置設定
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(lineCenterLat,lineCenterLng),(float)zl));

        if(this.line.isLocationCompleted()){
            setGeoJsonVisible();
        }

        // mapオブジェクトが生成された後にMarkerのOverlay初期表示を行うため、
        // stationListAdapterをOnMapReadyの最後に生成する。
        this.stationListView = (ListView)findViewById(R.id.StationNameList);
        this.stationsAdapter = new StationListAdapter(this,this.stations,this.mMap);
        this.stationListView.setAdapter(this.stationsAdapter);
        this.stationListView.setOnItemClickListener(this);
        this.stationListView.setOnItemLongClickListener(this);
        this.stationListView.setOnScrollListener(this);
    }

    private void updateProgressBar(){

        int finishedCnt = this.db.countAnsweredStationsInLine(this.companyId,this.selectedLineId);
        this.progressTitle.setText(String.format("%s 駅名解答率 : %d/%d",this.line.getName(), finishedCnt, stations.size()));
        this.progress.setMax(stations.size());
        this.progress.setProgress(finishedCnt);

        int stationsTotalScore = this.db.sumStationsScoreInLine(this.companyId,this.selectedLineId);
        this.stationsScore.setText(String.format("%d",stationsTotalScore));
    }

    public DBAdapter getDb(){
        return this.db;
    }

    // --------------------
    // NendAdListener
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
        this.showAnswerCount = 0;
        this.onReceiveAdCnt = 0;
    }

    @Override
    public void onDismissScreen(NendAdView nendAdView) {
        Log.d(TAG,"onDismissScreen");
    }

    // --------------------
    // onScrollStateChangedの処理
    // --------------------
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    // --------------------
    // onScrollの処理
    // --------------------
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int remainingItemCount = totalItemCount - (firstVisibleItem + visibleItemCount);
        if (StationPuzzleActivity.this.fabVisible && totalItemCount > visibleItemCount) {
            if (remainingItemCount > 0) {
                // SHow FAB Here
                StationPuzzleActivity.this.mFab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            } else {
                // Hide FAB Here
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) StationPuzzleActivity.this.mFab.getLayoutParams();
                int fab_bottomMargin = layoutParams.bottomMargin;
                StationPuzzleActivity.this.mFab.animate().translationY(StationPuzzleActivity.this.mFab.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    // scroll1操作、single tap、double tap、long tap操作のイベントハンドラ
    private class SeparatorViewGestureListener
            implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{

        public SeparatorViewGestureListener(){

        }
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            ViewGroup.LayoutParams mapFrameParam = StationPuzzleActivity.this.mapFrame.getLayoutParams();
            int baseHeight = StationPuzzleActivity.this.transparentView.getHeight()
                    - StationPuzzleActivity.this.progressTitle.getHeight()
                    - StationPuzzleActivity.this.progress.getHeight();
            int maxHeight = baseHeight*4/5;
            int minHeight = baseHeight/5;
            int currentMapHeight = mapFrameParam.height;
            int changeMapHeight = currentMapHeight+(int)e2.getY();

            Drawable drawable;
            if( changeMapHeight < minHeight ) {
                changeMapHeight = minHeight;
                drawable = ResourcesCompat.getDrawable(StationPuzzleActivity.this.getResources(),R.drawable.ic_expandmapbutton,null);
            }
            else if( maxHeight < changeMapHeight ) {
                changeMapHeight = maxHeight;
                drawable = ResourcesCompat.getDrawable(StationPuzzleActivity.this.getResources(),R.drawable.ic_reducemapbutton,null);
            }
            else{
                drawable = ResourcesCompat.getDrawable(StationPuzzleActivity.this.getResources(),R.drawable.ic_changemapsizebutton,null);
            }
            mapFrameParam.height = changeMapHeight;
            StationPuzzleActivity.this.mapFrame.setLayoutParams(mapFrameParam);
            StationPuzzleActivity.this.separatorMove.setImageDrawable(drawable);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"onFling");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            Log.d(TAG,"onDoubleTap");
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            Log.d(TAG,"onDoubleTapEvent");
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            Log.d(TAG,"onSingleTapConfirmed");
            return false;
        }
    }

    private class OnTouchListener implements View.OnTouchListener{
        private GestureDetector gestureDetector;
        public OnTouchListener(Context context){
            this.gestureDetector = new GestureDetector(context, new SeparatorViewGestureListener());
        }
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            this.gestureDetector.onTouchEvent(motionEvent);
            return false;
        }
    }

    // --------------------
    // onItemClickの処理
    // --------------------
    public int computeScore(int remainStations,int showAnswerCount, int missingCount ) {
        int sc = remainStations - ( missingCount + showAnswerCount*2 );
        if (sc < 0) sc = 0;
        return sc;
    }
    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long l) {
        Log.d(TAG,String.format("onItemClick() position = %d,駅名=%s",position,stationsAdapter.getStationInfo(position).getRawName()));
        // 未正解のリストアイテムがクリックされたら駅名選択リストダイアログを表示する。
        Station station = stations.get(position);
        if(!station.isFinished()){

            // 正解の配列インデックスを保持
            this.selectedStationIndex = position;
            ArrayList<Station> selectOptionStations = new ArrayList<Station>();
            final ArrayList<String> randomizedOptionStationsName = new ArrayList<String>();

/*
            // 未正解アイテムリストの生成
            // 未正解のStationInfoを抽出(stations→remainStations)
            Iterator<Station> staIte = this.stations.iterator();
            while(staIte.hasNext()){
                Station sta = staIte.next();
                if(!sta.isFinished()){
                    selectOptionStations.add(sta);
                }
            }
            // 0～未正解件数までの整数をランダムに生成
            // それをindexとして
            // remainingの件数が未正解の件数に到達するまで
            // reimainsから駅名を取得しremainingに追加していく
            Random rnd = new Random();
            while(randomizedOptionStationsName.size()<selectOptionStations.size()){
                // 0～未正解件数までの整数をランダムに生成
                int idx = rnd.nextInt(selectOptionStations.size());
                Station sta = selectOptionStations.get(idx);
                // remaining走査用のイテレータを生成
                Iterator<String> strIte = randomizedOptionStationsName.iterator();
                boolean already = false;						// 登録済フラグ
                String name = "";								// 登録する駅名
                // remainingを走査し、既に登録済みか否かを判定
                while(strIte.hasNext()){
                    name = strIte.next();
                    if(name.equals(sta.getRawName()+"("+sta.getRawKana()+")")) already = true;
                }
                if(!already){
                    randomizedOptionStationsName.add(sta.getRawName()+"("+sta.getRawKana()+")");
                }
            }
            Log.d(TAG,String.format("remaining.size() = %d, remainCnt = %d\r\n",randomizedOptionStationsName.size(),selectOptionStations.size()));
*/

            int optionItemNum = 0;
            int level = this.settingParameter.getDifficultyMode();
            switch(level){
                case SettingParameter.DIFFICULTY_BEGINNER:
                    optionItemNum = 8;
                    break;
                case SettingParameter.DIFFICULTY_AMATEUR:
                    optionItemNum = 12;
                    break;
                case SettingParameter.DIFFICULTY_PROFESSIONAL:
                    optionItemNum = 16;
                    break;
            }
            selectOptionStations = this.db.getCompanyStationList(this.companyId);
            Random rnd = new Random();
            while(randomizedOptionStationsName.size()<optionItemNum){
                // 0～未正解件数までの整数をランダムに生成
                int idx = rnd.nextInt(selectOptionStations.size());
                Station sta = selectOptionStations.get(idx);
                // remaining走査用のイテレータを生成
                Iterator<String> strIte = randomizedOptionStationsName.iterator();
                boolean already = false;						// 登録済フラグ
                String name = "";								// 登録する駅名
                // remainingを走査し、既に登録済みか否かを判定
                while(strIte.hasNext()){
                    name = strIte.next();
                    if(name.equals(sta.getRawName()+"("+sta.getRawKana()+")")) already = true;
                }
                if(!already){
                    randomizedOptionStationsName.add(sta.getRawName()+"("+sta.getRawKana()+")");
                }
            }
            int correctPosition = rnd.nextInt(optionItemNum);
            Station answerStation = this.stationsAdapter.getStationInfo(this.selectedStationIndex);
            String answerStationName = answerStation.getRawName()+"("+answerStation.getRawKana()+")";
            randomizedOptionStationsName.set(correctPosition,answerStationName);

            ArrayAdapter<String> remainStationsAdapter
                    = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,randomizedOptionStationsName);

            // 未正解アイテムのリストビュー生成
            ListView remainingStationsListView = new ListView(this);
            remainingStationsListView.setAdapter(remainStationsAdapter);
            remainingStationsListView.setOnItemClickListener(
                    // ダイアログ上の未正解アイテムがクリックされたら答え合わせする
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            mDialog.dismiss();
                            int correctAnswerIndex = StationPuzzleActivity.this.selectedStationIndex;
                            StationListAdapter adapter = StationPuzzleActivity.this.stationsAdapter;

                            Station correctStationInfo = adapter.getStationInfo(correctAnswerIndex);
                            String correctName = correctStationInfo.getRawName() + "(" + correctStationInfo.getRawKana() + ")";
                            String answerName  = randomizedOptionStationsName.get(position);

                            Log.d(TAG,String.format("answerName = %s, correctName = %s\r\n",answerName,correctName));

                            if(answerName.equals(correctName)){ // 駅名が一致する？
//                                Toast.makeText(StationPuzzleActivity.this,"正解!!! v(￣Д￣)v ", Toast.LENGTH_SHORT).show();
                                Toast.makeText(StationPuzzleActivity.this,"正解!!!    \uD83D\uDE0A",Toast.LENGTH_SHORT).show();
                                correctStationInfo.setFinishStatus();
                                int sc = computeScore(randomizedOptionStationsName.size(),correctStationInfo.getMissingCount(),correctStationInfo.getShowAnswerCount());
                                correctStationInfo.setStationScore(sc);
                                StationPuzzleActivity.this.db.updateStationAnswerStatus(correctStationInfo);
                                StationPuzzleActivity.this.stationsAdapter.notifyDataSetChanged();
                                // 進捗バーの更新
                                StationPuzzleActivity.this.updateProgressBar();
                            }
                            else{
                                correctStationInfo.incrementStationMissingCount();
//                                Toast.makeText(StationPuzzleActivity.this,"残念･･･ Σ(￣ロ￣lll)", Toast.LENGTH_SHORT).show();
                                Toast.makeText(StationPuzzleActivity.this,"残念･･･    \uD83D\uDE23",Toast.LENGTH_SHORT).show();
                            }
                            StationPuzzleActivity.this.selectedStationIndex = -1;
                        }
                    }
            );

            // ダイアログ表示
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("駅リスト")
                    .setPositiveButton("Cancel",null)
                    .setView(remainingStationsListView)
                    .create();
            mDialog.show();
        }
    }

    // --------------------
    // onItemLongClickの処理
    // --------------------
    // --------------------
    // 「回答クリア」
    private Station longClickSelectedStation = null;
    private void answerClear(){
        new AlertDialog.Builder(this)
                .setTitle(longClickSelectedStation.getName()+" : 回答クリア")
                .setMessage("駅名をクリアします。"+"\n"+"　　よろしいですか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,String.format("%s:駅名クリア",StationPuzzleActivity.this.longClickSelectedStation.getRawName()));
                        StationPuzzleActivity.this.longClickSelectedStation.resetFinishStatus();
                        StationPuzzleActivity.this.db.updateStationAnswerStatus(StationPuzzleActivity.this.longClickSelectedStation);
                        StationPuzzleActivity.this.stationsAdapter.notifyDataSetChanged();
                        // 進捗バーの更新
                        StationPuzzleActivity.this.updateProgressBar();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --------------------
    // 「回答を見る」
    // GeoJsonLayerの生成とColorの指定、Mapへの登録
    private void retrieveFileFromResource() {
        try {
            // 路線図のGeoJsonファイル読込
            layer = new GeoJsonLayer(mMap, this.line.getRawResourceId(), this);

            // 路線図の色を変更
            GeoJsonLineStringStyle style = layer.getDefaultLineStringStyle();
            style.setWidth(5.0f);
            style.setColor(Color.BLUE);

        } catch (IOException e) {
            Log.e(TAG, "GeoJSON file could not be read");
        } catch (JSONException e) {
            Log.e(TAG, "GeoJSON file could not be converted to a JSONObject");
        }
    }
    private void setGeoJsonVisible(){
        retrieveFileFromResource();
        layer.addLayerToMap();
    }
    private void resetGeoJsonVisible(){
        if(layer!=null){
            layer.removeLayerFromMap();
            layer = null;
        }
    }
    // 回答表示の消去
    private class displayTimerElapse extends TimerTask {
        Station sta;
        public displayTimerElapse(Station station){
            this.sta = station;
        }
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
                    // 路線の表示
                    if(!StationPuzzleActivity.this.line.isLocationCompleted()){
                        resetGeoJsonVisible();
                    }
                    displayTimerElapse.this.sta.removeMarker();
                    StationPuzzleActivity.this.mAnswerDisplayingTimer = null;
                }
            });
        }
    }
    // 回答の表示と消去タイマ起動
    private void answerDisplay(){
        if (mAnswerDisplayingTimer == null) {
            // 路線の表示
            if(!StationPuzzleActivity.this.line.isLocationCompleted()){
                setGeoJsonVisible();
            }
            // 駅マーカーの表示
            LatLng latlng = new LatLng(longClickSelectedStation.getStationLat(), longClickSelectedStation.getStationLng());
            MarkerOptions options = new MarkerOptions().position(latlng).title(longClickSelectedStation.getRawName());
            Marker marker = this.mMap.addMarker(options);
            longClickSelectedStation.setMarker(marker);
            // 消去タイマー起動
            mAnswerDisplayingTimer = new Timer(true);
            mAnswerDisplayingTimer.schedule(new displayTimerElapse(longClickSelectedStation),DISPLAY_ANSWER_TIME);
        }
        // 駅名のSnackbar表示
        final Snackbar sb = Snackbar.make(StationPuzzleActivity.this.stationListView,
                longClickSelectedStation.getRawName()+"("+longClickSelectedStation.getRawKana()+")",
                Snackbar.LENGTH_SHORT);
        sb.setActionTextColor(ContextCompat.getColor(StationPuzzleActivity.this, R.color.background1));
        sb.getView().setBackgroundColor(ContextCompat.getColor(StationPuzzleActivity.this, R.color.color_10));
        sb.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        longClickSelectedStation = this.stations.get(position);

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
                                if(StationPuzzleActivity.this.longClickSelectedStation.getStationOrder()!=1 &&
                                        StationPuzzleActivity.this.longClickSelectedStation.isFinished())
                                answerClear();
                                break;
                            case 1: // 回答を見る
                                if( mAnswerDisplayingTimer == null){
                                    if( showAnswerCount < showAnswerMax ) {
                                        StationPuzzleActivity.this.longClickSelectedStation.incrementStationShowAnswerCount();
                                        answerDisplay();
                                        if(StationPuzzleActivity.this.onReceiveAdCnt > 1) {
                                            showAnswerCount++;
                                        }
                                    }
                                    else{
                                        final Snackbar sb = Snackbar.make(StationPuzzleActivity.this.stationListView,
                                                "広告クリックお願いしま～っす",
                                                Snackbar.LENGTH_SHORT);
                                        sb.getView().setBackgroundColor(ContextCompat.getColor(StationPuzzleActivity.this, R.color.color_10));
                                        TextView textView = (TextView) sb.getView().findViewById(android.support.design.R.id.snackbar_text);
                                        textView.setTextColor(ContextCompat.getColor(StationPuzzleActivity.this.getApplicationContext(), R.color.color_RED));
                                        sb.show();
                                    }
                                }
                                break;
                            case 2: // Webを検索する
                                if(longClickSelectedStation.isFinished()){
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, longClickSelectedStation.getName()+"駅"); // query contains search string
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(StationPuzzleActivity.this,"駅が開設されていません。\n駅名を回答し駅を開設してください", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                }
        );

        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", this.longClickSelectedStation.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;

    }

    // --------------------
    // 戻るボタンの処理
    // --------------------
    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this.getApplicationContext(), PieceGarallyActivity.class);
        intent.putExtra("SelectedCompanyId", this.companyId);
        intent.putExtra("previewAnswerCount", this.previewLineAnswerCount);
        startActivityForResult(intent, 1);
        // アニメーションの設定
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
        this.db.close();
        finish();

    }

    // --------------------
    // OptionMenuの処理
    // --------------------
    // レベル設定
    private void settingDifficulty(){
        SettingParameterDialog set = new SettingParameterDialog(this,this.settingParameter,this.db);
        set.show();
        Log.d(TAG,String.format("mode = %d, vib = %b",this.settingParameter.getDifficultyMode(),this.settingParameter.isVibrate()));
    }
    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            item.setTitle("ⓘボタンを消す");
        }
        else{
            item.setTitle("ⓘボタンを表示");
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_information) {
            if(fabVisible){
                fabVisible = false;
                mFab.hide();
                item.setTitle("ⓘボタンを表示");
                Log.d(TAG,String.format("visibility = %b",fabVisible));
            }
            else{
                fabVisible = true;
                mFab.show();
                item.setTitle("ⓘボタンを消す");
                Log.d(TAG,String.format("visibility = %b",fabVisible));
            }
            settingParameter.setFabVisibility(fabVisible);
            StationPuzzleActivity.this.db.updateFabVisibility(fabVisible);
            return true;
        }
        else if (id == R.id.action_level) {
            settingDifficulty();
            return true;
        }
        else if (id == R.id.action_AboutPuzzRail) {
            Intent intent = new Intent(StationPuzzleActivity.this, TutorialActivity.class);
            intent.putExtra("page", 0);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_Help) {
            Intent intent = new Intent(StationPuzzleActivity.this, TutorialActivity.class);
            intent.putExtra("page", 4);
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

    // --------------------
    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }
}
