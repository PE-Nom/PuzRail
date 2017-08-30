package com.pentech.puzrail;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pentech.puzrail.database.Company;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.database.SettingParameter;
import com.pentech.puzrail.piecegarally.PieceGarallyActivity;
import com.pentech.puzrail.tutorial.TutorialActivity;
import com.pentech.puzrail.ui.OnePointTutorialDialog;
import com.pentech.puzrail.ui.SettingParameterDialog;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;

import java.util.ArrayList;
import java.util.Iterator;

import static net.nend.android.NendAdInterstitial.NendAdInterstitialStatusCode.INVALID_RESPONSE_TYPE;
import static net.nend.android.NendAdView.NendError.AD_SIZE_DIFFERENCES;
import static net.nend.android.NendAdView.NendError.AD_SIZE_TOO_LARGE;
import static net.nend.android.NendAdView.NendError.FAILED_AD_DOWNLOAD;
import static net.nend.android.NendAdView.NendError.FAILED_AD_REQUEST;

/**
 * Created by takashi on 2017/01/11.
 */

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        AbsListView.OnScrollListener,
        NendAdListener {

    private static final int RESULTCODE = 1;

    private String TAG = "MainActivity";
    private Context mContext;
    private ListView listView;
    private DBAdapter db;
    private ArrayList<Company> companies = new ArrayList<Company>();
    private CompanyListAdapter adapter;
    private AlertDialog mDialog;
    private SettingParameter settingParameter;
    private FloatingActionButton mFab;
    private boolean fabVisible = true;
    private int companyTotalScore = 0;

    private OnePointTutorialDialog onePointTutorial = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
        setContentView(R.layout.activity_company_select_list);

        db = new DBAdapter(this);
        db.open();
        this.companies = db.getCompanies();
        Iterator<Company> ite = companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            // totalScoreの計算
            int silhouetteTotalScore = db.sumSilhouetteScoreInLine(company.getId());
            int locationTotalScore   = db.sumLocationScoreInLine(company.getId());
            int stationsTotalScore   = db.sumStationsScoreInLine(company.getId());
            int companyScore = silhouetteTotalScore + locationTotalScore + stationsTotalScore;
            company.setCompanyTotalScore(companyScore);
            company.setSilhouetteTotalScore(silhouetteTotalScore);
            company.setLocationTotalScore(locationTotalScore);
            company.setStationsTotalScore(stationsTotalScore);
            this.companyTotalScore += companyScore;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("線路と駅パズル：");
        actionBar.setSubtitle("鉄道事業者選択");

        TextView totalScoreView = (TextView) findViewById(R.id.totalScoreValue);
        totalScoreView.setText(String.format("%d",this.companyTotalScore));

        this.listView = (ListView) findViewById(R.id.company_list_view);
        this.adapter = new CompanyListAdapter(this,this.companies,this.db);
        this.listView.setAdapter(this.adapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);

        onePointTutorial = new OnePointTutorialDialog(this,OnePointTutorialDialog._COMPANY_,R.id.company_list_view);
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
        this.listView.setOnScrollListener(this);

        NendAdView nendAdView = (NendAdView) findViewById(R.id.nend);
        nendAdView.setListener(this);
        nendAdView.loadAd();
    }

    // --------------------
    // NendAdListener
    @Override
    public void onReceiveAd(NendAdView nendAdView) {
        Log.d(TAG,"onReceiveAd");
    }

    @Override
    public void onFailedToReceiveAd(NendAdView nendAdView) {
        NendAdView.NendError nendError = nendAdView.getNendError();
        switch (nendError) {
            case INVALID_RESPONSE_TYPE:
                // 不明な広告ビュータイプ
                break;
            case FAILED_AD_DOWNLOAD:
                // 広告画像の取得失敗
                break;
            case FAILED_AD_REQUEST:
                // 広告取得失敗
                break;
            case AD_SIZE_TOO_LARGE:
                // 広告サイズがディスプレイサイズよりも大きい
                break;
            case AD_SIZE_DIFFERENCES:
                // リクエストしたサイズと取得したサイズが異なる
                break;
        }
        // エラーメッセージをログに出力
        Log.e(TAG, nendError.getMessage());
        Log.d(TAG,"onFailedToReceiveAd");
    }

    @Override
    public void onClick(NendAdView nendAdView) {
        Log.d(TAG,"onClick");
    }
    @Override
    public void onDismissScreen(NendAdView nendAdView) { Log.d(TAG,"onDismissScreen"); }

    // --------------------
    // onScrollStateChangedの処理
    // --------------------
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) { }

    // --------------------
    // onScrollの処理
    // --------------------
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int remainingItemCount = totalItemCount - (firstVisibleItem + visibleItemCount);
        Log.d(TAG,String.format("totalItemCount = %d,visibleItemCount = %d",totalItemCount,visibleItemCount));
        if (MainActivity.this.fabVisible && totalItemCount > visibleItemCount) {
            if (remainingItemCount > 0) {
                // SHow FAB Here
                MainActivity.this.mFab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            } else {
                // Hide FAB Here
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) MainActivity.this.mFab.getLayoutParams();
                int fab_bottomMargin = layoutParams.bottomMargin;
                MainActivity.this.mFab.animate().translationY(MainActivity.this.mFab.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    // --------------------
    // onItemClickの処理
    // --------------------
    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, PieceGarallyActivity.class);
        intent.putExtra("SelectedCompanyId", this.companies.get(position).getId());
        startActivityForResult(intent, RESULTCODE);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        db.close();
        finish();
    }

    // --------------------
    // onItemLongClickの処理
    // --------------------
    // クリア対象の回答データ選択
    private Company longClickSelectedCompany = null;
    private void answerClear(){
        final String[] items = {"路線シルエット（全路線）", "地図合わせ（全路線）", "駅並べ（全駅）"};
        final Boolean[] checkedItems = {false,false,false};
        new AlertDialog.Builder(this)
                .setTitle(longClickSelectedCompany.getName()+" : 回答クリア")
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
                                        Log.d(TAG,String.format("%s:路線名回答のクリア",MainActivity.this.longClickSelectedCompany.getName()));
                                        MainActivity.this.db.updateLineSilhouetteAnswerStatusInCompany(MainActivity.this.longClickSelectedCompany.getId(),false);
                                        MainActivity.this.adapter.notifyDataSetChanged();
                                    }
                                    break;
                                case 1:
                                    if(checkedItems[i]){
                                        Log.d(TAG,String.format("%s:敷設回答のクリア",MainActivity.this.longClickSelectedCompany.getName()));
                                        MainActivity.this.db.updateLineLocationAnswerStatusInCompany(MainActivity.this.longClickSelectedCompany.getId(),false);
                                        MainActivity.this.adapter.notifyDataSetChanged();
                                    }
                                    break;
                                case 2:
                                    if(checkedItems[i]){
                                        Log.d(TAG,String.format("%s:駅回答のクリア",MainActivity.this.longClickSelectedCompany.getName()));
                                        MainActivity.this.db.updateStationsAnswerStatusInCompany(MainActivity.this.longClickSelectedCompany.getId(),false);
                                        MainActivity.this.adapter.notifyDataSetChanged();
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

    /**
     * Callback method to be invoked when an item in this view has been
     * clicked and held.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access
     * the data associated with the selected item.
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        longClickSelectedCompany = this.companies.get(position);

        final ArrayList<String> contextMenuList = new ArrayList<String>();
        contextMenuList.add("回答クリア");
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
                            case 0:// 回答をクリア
                                answerClear();
                                break;
                            case 1: // Webを検索する
                                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                intent.putExtra(SearchManager.QUERY, longClickSelectedCompany.getName()); // query contains search string
                                startActivity(intent);
                                break;
                        }
                    }
                }
        );

        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", this.longClickSelectedCompany.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;
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
            MainActivity.this.db.updateFabVisibility(fabVisible);
            return true;
        }
        else if (id == R.id.action_level) {
            settingDifficulty();
            return true;
        }
        else if (id == R.id.action_AboutPuzzRail) {
            Intent intent = new Intent(mContext, TutorialActivity.class);
            intent.putExtra("page", 0);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_Help) {
            Intent intent = new Intent(mContext, TutorialActivity.class);
            intent.putExtra("page", 1);
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

}
