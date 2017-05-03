package com.pentech.puzrail.location;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pentech.puzrail.piecegarally.PieceGarallyActivity;
import com.pentech.puzrail.R;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.database.Line;
import com.pentech.puzrail.ui.PopUp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LocationPuzzleActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        OnLineScrollEndListener,
        NendAdListener {

    private final static String TAG = "LocationPuzzleActivity";
    private String lineName;
    private DBAdapter db;
    private int companyId;
    private int selectedLineId;
    private Line line;
    private GoogleMap mMap;
    private MapView mMapView;
    private GeoJsonLayer layer;
    private LineMapOverlayView mImageView;
    private ImageView transparent;
    private ArrayList<View> views = new ArrayList<View>();

    private Drawable mDrawable;
    private AlertDialog mDialog;

    private int previewLineAnswerCount = 0;
    private final static long DISPLAY_ANSWER_TIME[] = {1000,1500,2500,4000};
    private static final int showAnswerMax = 4;
    private int showAnswerCount = 0;
    private Timer mAnswerDisplayingTimer = null;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_location_puzzle);

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        Intent intent = getIntent();
        this.selectedLineId = intent.getIntExtra("SelectedLineId", 42); // デフォルトを紀勢線のlineIdにしておく
        this.previewLineAnswerCount = intent.getIntExtra("previewAnswerCount",0);

        this.db = new DBAdapter(this);
        this.db.open();
        this.line = db.getLine(this.selectedLineId);
        Log.d(TAG,String.format("selected line is %s",line.getName()));
        setUpMap(savedInstanceState);

        String companyName = db.getCompany(line.getCompanyId()).getName();
        String lineName = line.getName();
        String linekana = line.getLineKana();
        this.lineName = lineName+"("+linekana+")";
        this.companyId = line.getCompanyId();

        actionBar.setTitle("線路と駅パズル：地図合わせ");
        actionBar.setSubtitle(companyName+"／"+this.lineName);

        NendAdView nendAdView = (NendAdView) findViewById(R.id.nend);
        nendAdView.setListener(this);
        nendAdView.loadAd();

    }

    @Override
    public void onReceiveAd(NendAdView nendAdView) {
        Log.d(TAG,"onReceiveAd");
    }

    @Override
    public void onFailedToReceiveAd(NendAdView nendAdView) {
        Log.d(TAG,"onFailedToReceiveAd");
    }

    @Override
    public void onClick(NendAdView nendAdView) {
        Log.d(TAG,"onClick");
        this.showAnswerCount = 0;
    }

    @Override
    public void onDismissScreen(NendAdView nendAdView) {
        Log.d(TAG,"onDismissScreen");
    }

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

    private void setUpMap(Bundle savedInstanceState){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        this.mMapView = (MapView)findViewById(R.id.mapView);
        this.mMapView.onCreate(savedInstanceState);
        this.mMapView.getMapAsync(this);

        mImageView = (LineMapOverlayView)findViewById(R.id.imageview);
        mImageView.setOnScrollEndListener(this);
        mImageView.setLine(this.line); //
        views.add(mImageView);

        transparent = (ImageView)findViewById(R.id.transparent);

        this.mMapView.addFocusables(views,View.FOCUS_FORWARD);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // マップ形式の設定
        this.mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // 表示エリアと縮尺の制限
        UiSettings mUiSetting = this.mMap.getUiSettings();
        mUiSetting.setRotateGesturesEnabled(false);
        this.mMap.setMaxZoomPreference(this.line.getMaxZoomLevel());
        this.mMap.setMinZoomPreference(this.line.getMinZoomLevel());
        // 離島除く
        LatLng north_east = new LatLng(this.line.getScrollMaxLat(),this.line.getScrollMaxLng());
        LatLng south_west = new LatLng(this.line.getScrollMinLat(),this.line.getScrollMinLng());
        LatLngBounds JAPAN = new LatLngBounds(south_west,north_east);
        this.mMap.setLatLngBoundsForCameraTarget(JAPAN);

        // EventListener
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);
        this.mMap.setOnCameraIdleListener(this);

        // 初期表示座標の計算
        double lineRangeLng  = this.line.getCorrectRightLng() - this.line.getCorrectLeftLng();
        double lineRangeLat  = this.line.getCorrectTopLat()   - this.line.getCorrectBottomLat();
        Log.d(TAG,String.format("##### line range    : lng = %f, lat = %f",lineRangeLng,lineRangeLat));

        double lineCenterLng = ( this.line.getCorrectLeftLng() + this.line.getCorrectRightLng() )/2.0;
        double lineCenterLat = ( this.line.getCorrectBottomLat() + this.line.getCorrectTopLat() )/2.0;
        Log.d(TAG,String.format("##### line center   : lng = %f, lat = %f",lineCenterLng,lineCenterLat));

        // 路線中心座標で位置設定
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lineCenterLat,lineCenterLng),
                this.line.getInitZoomLevel())
        );
        Projection proj = this.mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        // 北東 = top/right, 南西 = bottom/left
        double topLatitude = vRegion.latLngBounds.northeast.latitude;
        double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        double rightLongitude = vRegion.latLngBounds.northeast.longitude;
        Log.d(TAG, "地図表示範囲\n緯度:" + bottomLatitude + "～" + topLatitude + "\n経度:" + leftLongitude + "～" + rightLongitude);

        double displayLatRange = topLatitude - bottomLatitude;
        double displayLngRange = rightLongitude - leftLongitude;

        // 乱数による位置移動
        double camposLat = lineCenterLat + (displayLatRange-lineRangeLat)*(Math.random()-0.5);
        double camposLng = lineCenterLng + (displayLngRange-lineRangeLng)*(Math.random()-0.5);
        Log.d(TAG,String.format("##### camera position   : lng = %f, lat = %f",camposLng,camposLat));
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(camposLat,camposLng),
                this.line.getInitZoomLevel())
        );
//
        mImageView.setMap(this.mMap);
        mImageView.setImageDrawable();
        if(hasAlreadyLocated()) setGeoJsonVisible();

    }

    private boolean hasAlreadyLocated(){
        return this.line.isLocationCompleted();
    }

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
                    resetGeoJsonVisible();
                    mAnswerDisplayingTimer = null;
                }
            });
        }
    }

    // 回答の表示と消去タイマ起動
    private void answerDisplay(){
        if (mAnswerDisplayingTimer == null) {
            setGeoJsonVisible();
            mAnswerDisplayingTimer = new Timer(true);
            mAnswerDisplayingTimer.schedule(new displayTimerElapse(),DISPLAY_ANSWER_TIME[showAnswerCount]);
        }
    }

    // 回答クリア
    private void answerClear(){
        new AlertDialog.Builder(this)
                .setTitle(this.line.getName()+" : 回答クリア")
                .setMessage("地図合わせの回答をクリアします。"+"\n"+"　　よろしいですか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,String.format("%s:敷設回答クリア",LocationPuzzleActivity.this.line.getName()));
                        LocationPuzzleActivity.this.line.resetLocationAnswerStatus();
                        LocationPuzzleActivity.this.db.updateLineLocationAnswerStatus(LocationPuzzleActivity.this.line);
                        LocationPuzzleActivity.this.resetGeoJsonVisible();
                        LocationPuzzleActivity.this.mImageView.resetImageDrawable();
                        LocationPuzzleActivity.this.mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(LocationPuzzleActivity.this.line.getInitCamposLat(),
                                        LocationPuzzleActivity.this.line.getInitCamposLng()),
                                        LocationPuzzleActivity.this.line.getInitZoomLevel())
                                );
                        LocationPuzzleActivity.this.mImageView.setImageDrawable();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(TAG,"onMapLongClick");

        final ArrayList<String> contextMenuList = new ArrayList<String>();
        contextMenuList.add("回答クリア");
        contextMenuList.add("回答を見る");
        contextMenuList.add("最初の位置に戻す");
        contextMenuList.add("Webを検索する");

        ArrayAdapter<String> contextMenuAdapter
                = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,contextMenuList);

        // 未正解アイテムのリストビュー生成
        ListView contextMenuListView = new ListView(this);
        contextMenuListView.setAdapter(contextMenuAdapter);
        contextMenuListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        LocationPuzzleActivity.this.mDialog.dismiss();
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>)adapterView.getAdapter();
                        switch(position){
                            case 0: // 回答をクリア（回答済みの場合）
                                if(LocationPuzzleActivity.this.hasAlreadyLocated()){
                                    answerClear();
                                }
                                break;
                            case 1: // 回答を見る（未回答の場合）
                                if(!LocationPuzzleActivity.this.hasAlreadyLocated() && mAnswerDisplayingTimer == null ){
                                    if( showAnswerCount < showAnswerMax ){
                                        answerDisplay();
                                        showAnswerCount++;
                                    }
                                    else{
                                        final Snackbar sb = Snackbar.make(LocationPuzzleActivity.this.transparent,
                                                "広告クリックお願いしま～っす",
                                                Snackbar.LENGTH_SHORT);
                                        sb.getView().setBackgroundColor(ContextCompat.getColor(LocationPuzzleActivity.this, R.color.color_10));
                                        TextView textView = (TextView) sb.getView().findViewById(android.support.design.R.id.snackbar_text);
                                        textView.setTextColor(ContextCompat.getColor(LocationPuzzleActivity.this.getApplicationContext(), R.color.color_RED));
                                        sb.show();
                                    }
                                }
                                break;
                            case 2: // 最初の位置に戻す
                                LocationPuzzleActivity.this.mMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                new LatLng(LocationPuzzleActivity.this.line.getInitCamposLat(),
                                                        LocationPuzzleActivity.this.line.getInitCamposLng()),
                                                LocationPuzzleActivity.this.line.getInitZoomLevel())
                                );
                                LocationPuzzleActivity.this.mImageView.resetImageDrawable();
                                LocationPuzzleActivity.this.mImageView.setImageDrawable();;
                                break;
                            case 3: // Webを検索する
                                if(LocationPuzzleActivity.this.line.isNameCompleted()){
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, LocationPuzzleActivity.this.line.getName()); // query contains search string
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(LocationPuzzleActivity.this,"路線名が未回答です。\n路線名を先に回答してください", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                }
        );

        // ダイアログ表示
        this.mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", this.line.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        this.mDialog.show();

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG,"onMapClick");
        // ToDo
        // タイムトライアルのタイマー開始／停止操作実装
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG,"onCamelaIdel");
        if(mImageView.getDrawable()!=null){
            checkLocation();
        }
        CameraPosition campos = mMap.getCameraPosition();
        Log.d(TAG,String.format("カメラ現在位置 lat = %f, Lng = %f, zoom = %f", campos.target.latitude,campos.target.longitude,campos.zoom));
        Projection proj = mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        // 北東 = top/right, 南西 = bottom/left
        double topLatitude = vRegion.latLngBounds.northeast.latitude;
        double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        double rightLongitude = vRegion.latLngBounds.northeast.longitude;
        Log.d(TAG, "地図表示範囲\n緯度:" + bottomLatitude + "～" + topLatitude + "\n経度:" + leftLongitude + "～" + rightLongitude);
    }

    @Override
    public void onScrollEnd() {
        Log.d(TAG,"onScrollEnd");
        checkLocation();
    }

    private boolean checkLocation(){
        mImageView.displayCorrectCoordinate(TAG);
        int err = mImageView.computeLocationError();
        Log.d(TAG,String.format("error = %d",err));
        if( err < LineMapOverlayView.ERR_RANGE_LEVEL0 ){
            // 正解
            mImageView.resetImageDrawable();
            setGeoJsonVisible();
            this.line.setLocationAnswerStatus();
            db.updateLineLocationAnswerStatus(this.line);
            Toast.makeText(LocationPuzzleActivity.this,"正解!!! v(￣Д￣)v ", Toast.LENGTH_SHORT).show();
        }
        else{
        }
        return true;

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

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_AboutPuzzRail) {
            PopUp.makePopup(this,this.mImageView,"file:///android_asset/about_puzrail.html");
            return true;
        }
        else if (id == R.id.action_Help) {
            PopUp.makePopup(this,this.mImageView,"file:///android_asset/help_puzrail.html");
            return true;
        }
        else if(id == R.id.action_Ask) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "puzrail@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "パズレールについてのお問い合わせ");
            startActivity(Intent.createChooser(intent, ""));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
