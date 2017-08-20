package com.pentech.puzrail;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.pentech.puzrail.database.Company;
import com.pentech.puzrail.database.DBAdapter;
import com.pentech.puzrail.piecegarally.PieceGarallyActivity;
import com.pentech.puzrail.tutorial.TutorialActivity;

import net.nend.android.NendAdListener;
import net.nend.android.NendAdView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by takashi on 2017/01/11.
 */

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,NendAdListener{

    private static final int RESULTCODE = 1;

    private String TAG = "MainActivity";
    private Context mContext;
    private ListView listView;
    private DBAdapter db;
    private ArrayList<Company> companies = new ArrayList<Company>();
    private ArrayList<String> names = new ArrayList<String>();
    private CompanyListAdapter adapter;
    private AlertDialog mDialog;

    private LinearLayout rootLayout;

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
            names.add(company.getName());
            Log.d(TAG,String.format("names : %s",company.getName()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("線路と駅パズル：");
        actionBar.setSubtitle("鉄道事業者選択");

        this.listView = (ListView) findViewById(R.id.company_list_view);
        this.adapter = new CompanyListAdapter(this,this.companies,this.db);
        this.listView.setAdapter(this.adapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInformation();
            }
        });

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
    }

    @Override
    public void onDismissScreen(NendAdView nendAdView) {
        Log.d(TAG,"onDismissScreen");
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
            information.setText("運営会社をタップしてね");

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
                    Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
                    intent.putExtra("page", 1);
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
//                        onePointTutorial.dismiss();
                        onePointTutorial = null;
                        mOnePointTutorialDisplayingTimer.cancel();
                        mOnePointTutorialDisplayingTimer = null;
                    }
                }
            });

            // 画面中央に表示
            onePointTutorial.showAtLocation(findViewById(R.id.company_list_view), Gravity.BOTTOM, 0, 0);

            mOnePointTutorialDisplayingTimer = new Timer(true);
            mOnePointTutorialDisplayingTimer.schedule(new MainActivity.tutorialDisplayTimerElapse(),TUTORIAL_DISPLAY_TIME);
        }
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
                                        MainActivity.this.db.updateLineNameAnswerStatusInCompany(MainActivity.this.longClickSelectedCompany.getId(),false);
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
                            case 0: // 回答をクリア
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
}
