package com.pentech.puzrail.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by takashi on 2017/01/06.
 */

public class DBAdapter {
    static final String DATABASE_NAME = "Railway.db";
    static final int DATABASE_VERSION = 2;

    private String TAG = "DBAdapter";

    protected final Context context;
    protected DatabaseHelper dbHelper;
    protected SQLiteDatabase db;

    public DBAdapter(Context context){
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
        Log.d(TAG,"DBAdapter construct");
    }

    //
    // SQLiteOpenHelper
    //

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private String TAG = "DatabaseHelper";
        private Context context;
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            Log.d(TAG,"DatabaseHelper construct");
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            InputStream is;
            BufferedReader bfReader;
            Log.d(this.TAG,"onCreate DB");
            try {
                is = this.context.getAssets().open("init.sql");
                bfReader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while(( line = bfReader.readLine() ) != null){
                    if( line.charAt(0)=='#') continue;
                    if(! line.equals("") ){
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                sb.deleteCharAt(sb.length()-1);
                for(String sql: sb.toString().split(";")){
                    Log.d(TAG,sql);
                    db.execSQL(sql);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"onUpgrade");
            db.execSQL("DROP TABLE IF EXISTS settingParameter");
            db.execSQL("DROP TABLE IF EXISTS currentMode");
            db.execSQL("DROP TABLE IF EXISTS area");
            db.execSQL("DROP TABLE IF EXISTS companyType");
            db.execSQL("DROP TABLE IF EXISTS companies");
            db.execSQL("DROP TABLE IF EXISTS lines");
            db.execSQL("DROP TABLE IF EXISTS stations");
            onCreate(db);
        }
    }

    //
    // Adapter Methods
    //

    public DBAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    //
    // App Methods
    //

    // Setting Parameter
    private SettingParameter extractSettingParameter(Cursor c){
        int dif = c.getInt(c.getColumnIndex("difficultyMode"));
        boolean mode = (c.getInt(c.getColumnIndex("vibrationMode"))==1);
        SettingParameter setting = new SettingParameter(dif,mode);
        Log.d(TAG,String.format("setting : %d,%b",
                setting.getDifficultyMode(),setting.isVibrate()));
        return setting;
    }

    public SettingParameter getSettingParameter(){
        SettingParameter setting = null;
        Cursor cursor = db.rawQuery("SELECT * from settingParameter WHERE id = 0",null);
        try{
            if(cursor.moveToFirst()){
                setting = extractSettingParameter(cursor);
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return setting;
    }

    public boolean updateDifficultySetting(int difficultyMode){
        ContentValues cv = new ContentValues();
        cv.put("difficultyMode", difficultyMode);
        db.update("settingParameter", cv, "id = 0", null);
        return true;
    }

    public boolean updateVibrationSetting(boolean vibrationMode){
        ContentValues cv = new ContentValues();
        if(vibrationMode){
            cv.put("vibrationMode", 1);
        }
        else{
            cv.put("vibrationMode", 0);
        }
        db.update("settingParameter", cv, "id = 0", null);
        return true;
    }

    // companies table
    private Company extractCompany( Cursor c){
        int id = c.getInt(c.getColumnIndex("companyId"));
        int code = c.getInt(c.getColumnIndex("companyCode"));
        String name = c.getString(c.getColumnIndex("companyName"));
        String kana = c.getString(c.getColumnIndex("companyKana"));
        Company company= new Company(id,code,name,kana);
        Log.d(TAG,String.format("company : %d,%d,%s,%s",
                company.getId(),company.getCode(),
                company.getName(),company.getKana()));
        return company;
    }

    public Company getCompany(int companyId) {
        Company company = null;
        Cursor cursor = db.rawQuery("SELECT * from companies WHERE companyId=?",new String[]{String.valueOf(companyId)});
        try{
            if(cursor.moveToFirst()){
                company = extractCompany(cursor);
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return company;
    }

    public ArrayList<Company> getCompanies(){
        ArrayList<Company> companies = new ArrayList<Company>();
        Cursor cursor = db.rawQuery("SELECT * from companies",null);
        try{
            if(cursor.moveToFirst()){
                do{
                    companies.add(extractCompany(cursor));
                }while(cursor.moveToNext());
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return companies;
    }

    // lines table
    private Line extractLine(Cursor c){
        int lineId = c.getInt(c.getColumnIndex("lineId"));
        int areaCode = c.getInt(c.getColumnIndex("areaCode"));
        int companyId = c.getInt(c.getColumnIndex("companyId"));
        String lineName = c.getString(c.getColumnIndex("lineName"));
        String lineKana = c.getString(c.getColumnIndex("lineKana"));
        int type = c.getInt(c.getColumnIndex("type"));
        String drawable_resource_name = c.getString(c.getColumnIndex("drawable_resource_name"));
        String raw_resource_name = c.getString(c.getColumnIndex("raw_resource_name"));
        double correct_leftLng = c.getDouble(c.getColumnIndex("correct_leftLng"));
        double correct_topLat = c.getDouble(c.getColumnIndex("correct_topLat"));
        double correct_rightLng = c.getDouble(c.getColumnIndex("correct_rightLng"));
        double correct_bottomLat = c.getDouble(c.getColumnIndex("correct_bottomLat"));
        double scroll_max_lat = c.getDouble(c.getColumnIndex("scroll_max_lat"));
        double scroll_min_lat = c.getDouble(c.getColumnIndex("scroll_min_lat"));
        double scroll_max_lng = c.getDouble(c.getColumnIndex("scroll_max_lng"));
        double scroll_min_lng = c.getDouble(c.getColumnIndex("scroll_min_lng"));
        double init_campos_lat = c.getDouble(c.getColumnIndex("init_campos_lat"));
        double init_campos_lng = c.getDouble(c.getColumnIndex("init_campos_lng"));
        double max_zoom_level = c.getDouble(c.getColumnIndex("max_zoom_level"));
        double min_zoom_level = c.getDouble(c.getColumnIndex("min_zoom_level"));
        double init_zoom_level = c.getDouble(c.getColumnIndex("init_zoom_level"));
        boolean nameAnswerStatus = (c.getInt(c.getColumnIndex("nameAnswerStatus"))==1);
        boolean locationAnswerStatus = (c.getInt(c.getColumnIndex("locationAnswerStatus"))==1);
        boolean stationAnswerStatus = (c.getInt(c.getColumnIndex("stationAnswerStatus"))==1);;
        Line line = new Line(this.context,
                lineId,areaCode,companyId,
                lineName,lineKana,type,
                drawable_resource_name,raw_resource_name,
                correct_leftLng,correct_topLat,correct_rightLng,correct_bottomLat,
                scroll_max_lat,scroll_min_lat,scroll_max_lng,scroll_min_lng,init_campos_lat,init_campos_lng,
                max_zoom_level,min_zoom_level,init_zoom_level,
                nameAnswerStatus,locationAnswerStatus,stationAnswerStatus);
/*        Log.d(TAG,String.format("lines: %d,%d,%d," +
                        "%s,%s," +
                        "%d," +
                        "%s,%s," +
                        "%f,%f,%f,%f," +
                        "%f,%f,%f,%f," +
                        "%f,%f," +
                        "%f,%f,%f," +
                        "%b,%b,%b,"
                lineId,areaCode,companyId,
                lineName,lineKana,
                type,
                drawable_resource_name,raw_resource_name,
                correct_leftLng,correct_topLat,correct_rightLng,correct_bottomLat,
                scroll_max_lat,scroll_min_lat,scroll_max_lng,scroll_min_lng,
                init_campos_lat,init_campos_lng,
                max_zoom_level,min_zoom_level,init_zoom_level,
                nameAnswerStatus,locationAnswerStatus,stationAnswerStatus
        ));
*/
        return line;
    }

    public Line getLine(int lineId){
        Line line = null;
        Cursor cursor = db.rawQuery("SELECT * from lines WHERE lineId=?",new String[]{String.valueOf(lineId)});
        try{
            if(cursor.moveToFirst()){
                line = extractLine(cursor);
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return line;

    }

    public ArrayList<Line> getLineList(int companyId, boolean isRandomize){
        ArrayList<Line> returnLines = new ArrayList<Line>();
        ArrayList<Line> lines = new ArrayList<Line>();
        ArrayList<Line> randomizedLines = new ArrayList<Line>();

        Cursor cursor = db.rawQuery("SELECT * from lines WHERE companyId=?",new String[]{String.valueOf(companyId)});
        try{
            if(cursor.moveToFirst()){
                do{
                    lines.add(extractLine(cursor));
                }while(cursor.moveToNext());
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        returnLines = lines;

        if(isRandomize){
            Random rnd = new Random();
            while(randomizedLines.size() < lines.size()){
                int idx = rnd.nextInt(lines.size());
                Line srcLine = lines.get(idx);
                Iterator<Line> rndLinesIte = randomizedLines.iterator();
                boolean alreadyCopied = false;
                while(rndLinesIte.hasNext()){
                    Line ln = rndLinesIte.next();
                    if(ln.getLineId()==srcLine.getLineId()){
                        alreadyCopied = true;
                    }
                }
                if(!alreadyCopied){
                    randomizedLines.add(srcLine);
                }
            }
            returnLines = randomizedLines;
        }

        return returnLines;
    }

    /*
     * ロケーションセットのステータス更新
     */
    public boolean updateLineLocationAnswerStatusInCompany(int companyId,boolean status){
        ContentValues cv = new ContentValues();
        if(status){
            cv.put("locationAnswerStatus", 1);
        }
        else{
            cv.put("locationAnswerStatus", 0);
        }
        db.update("lines", cv, "companyId = "+companyId, null);
        return true;
    }

    public boolean updateLineLocationAnswerStatus(Line line){
        int lineId = line.getLineId();
        ContentValues cv = new ContentValues();
        if(line.isLocationCompleted()){
            cv.put("locationAnswerStatus", 1);
        }
        else{
            cv.put("locationAnswerStatus", 0);
        }
        db.update("lines", cv, "lineId = "+lineId, null);
        return true;
    }

    /*
     * 路線名のAnswerStatus更新
     */
    public boolean updateLineNameAnswerStatusInCompany(int companyId,boolean status){
        ContentValues cv = new ContentValues();
        if(status){
            cv.put("nameAnswerStatus", 1);
        }
        else{
            cv.put("nameAnswerStatus", 0);
        }
        db.update("lines", cv, "companyId = "+companyId, null);
        return true;
    }

    public boolean updateLineNameAnswerStatus(Line line){
        int lineId = line.getLineId();
        ContentValues cv = new ContentValues();
        if( line.isNameCompleted()){
            cv.put("nameAnswerStatus",1);
        }
        else{
            cv.put("nameAnswerStatus",0);
        }
        db.update("lines",cv,"lineId = "+lineId,null);
        return true;
    }

    /*
     * 総路線数の取得
     */
    public int countTotalLines(int companyId){
        Cursor cur = db.rawQuery("SELECT * from lines WHERE companyId=?",new String[]{String.valueOf(companyId)});
        return cur.getCount();
    }
    /*
     * 事業者ごとの路線名完了件数の取得
     */
    public int countLineNameAnsweredLines(int companyId){
        int cnt;
        Cursor cursor = db.rawQuery("SELECT * from lines WHERE companyId=? and nameAnswerStatus = 1",new String[]{String.valueOf(companyId)});
        cnt = cursor.getCount();
        return cnt;
    }
    /*
     * 事業者ごとの敷設完了路線数の取得
     */
    public int countLocationAnsweredLines(int companyId){
        int cnt;
        Cursor cursor = db.rawQuery("SELECT * from lines WHERE companyId=? and locationAnswerStatus = 1", new String[]{String.valueOf(companyId)});
        cnt = cursor.getCount();
        return cnt;
    }

    // stations table
    private Station extractStation(Cursor c){
        int companyId = c.getInt(c.getColumnIndex("companyId"));
        int lineId = c.getInt(c.getColumnIndex("lineId"));
        int stationOrder = c.getInt(c.getColumnIndex("stationOrder"));
        String stationName = c.getString(c.getColumnIndex("stationName"));
        String stationKana = c.getString(c.getColumnIndex("stationKana"));
        double stationLat = c.getDouble(c.getColumnIndex("stationLat"));
        double stationLng = c.getDouble(c.getColumnIndex("stationLng"));
        boolean overlaySw = (c.getInt(c.getColumnIndex("overlaySw"))==1);
        boolean answerStatus = (c.getInt(c.getColumnIndex("answerStatus"))==1);
        Station station = new Station(companyId,lineId,stationOrder,
                                        stationName,stationKana,
                                        stationLat,stationLng,
                                        overlaySw,answerStatus);
        Log.d(TAG,String.format("station: %d,%d,%d," +
                        "%s,%s," +
                        "%f,%f," +
                        "%b,%b",
                        companyId,lineId,stationOrder,
                        stationName,stationKana,
                        stationLat,stationLng,
                        overlaySw,answerStatus
        ));
        return station;
    }

    public ArrayList<Station> getStationList(int lineId){
        ArrayList<Station> stations = new ArrayList<Station>();
        Cursor cursor = db.rawQuery("SELECT * from stations WHERE lineId=? ORDER BY stationOrder",new String[]{String.valueOf(lineId)});
        try{
            if(cursor.moveToFirst()){
                do{
                    stations.add(extractStation(cursor));
                }while(cursor.moveToNext());
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return stations;

    }

    // companyIdで指定される事業者内全路線の初ターミナルを除くすべて駅の回答ステータスを変更する
    public boolean updateStationsAnswerStatusInCompany(int companyId,boolean status){
        ContentValues cv = new ContentValues();
        if(status){
            cv.put("answerStatus", 1);
        }
        else{
            cv.put("answerStatus", 0);
        }
        db.update("stations", cv, "companyId = "+companyId + " AND stationOrder != 1", null);
        return true;
    }

    // lineIdで指定される路線の初ターミナルを除くすべて駅の回答ステータスを変更する
    public boolean updateStationsAnswerStatusInLine(int lineId, boolean status){
        ContentValues cv = new ContentValues();
        if(status){
            cv.put("answerStatus", 1);
        }
        else{
            cv.put("answerStatus", 0);
        }
        db.update("stations", cv, "lineId = "+lineId + " AND stationOrder != 1", null);
        return true;
    }

    public boolean updateStationAnswerStatus(Station station){
        ContentValues cv = new ContentValues();
        if(station.isFinished()){
            cv.put("answerStatus", 1);
        }
        else{
            cv.put("answerStatus", 0);
        }
        db.update("stations", cv, "lineId = "+station.getLineId() + " AND stationOrder = " + station.getStationOrder(), null);
        return true;
    }


    public boolean updateStationMarkerOverlaySw(Station station){
        ContentValues cv = new ContentValues();
        if(station.isOverlaySw()){
            cv.put("overlaySw", 1);
        }
        else{
            cv.put("overlaySw", 0);
        }
        db.update("stations", cv, "lineId = "+station.getLineId() + " AND stationOrder = " + station.getStationOrder(), null);
        return true;
    }
    /*
     * 総駅数の取得
     */
    public int countTotalStations(){
        Cursor cur = db.rawQuery("SELECT * from stations",null);
        return cur.getCount();
    }
    /*
     * 事業者ごとの総駅数の取得
     */
    public int countTotalStationsInCompany(int companyId){
        Cursor cur = db.rawQuery("SELECT * from stations WHERE companyId=?", new String[]{String.valueOf(companyId)});
        return cur.getCount();
    }
    /*
     * 事業者ごとの開設完了駅数の取得
     */
    public int countAnsweredStationsInCompany(int companyId){
        int cnt;
        Cursor cursor = db.rawQuery("SELECT * from stations WHERE companyId=? and answerStatus = 1",
                new String[]{String.valueOf(companyId)});
        cnt = cursor.getCount();
        return cnt;
    }
    /*
     * 路線ごとの総駅数の取得
     */
    public int countTotalStationsInLine(int companyId,int lineId){
        Cursor cur = db.rawQuery("SELECT * from stations WHERE companyId=? and lineId=?",
                new String[]{String.valueOf(companyId), String.valueOf(lineId)});
        return cur.getCount();
    }
    /*
     * 路線ごとの開設完了駅数の取得
     */
    public int countAnsweredStationsInLine(int companyId,int lineId){
        int cnt;
        Cursor cur = db.rawQuery("SELECT * from stations WHERE companyId=? and lineId=? and answerStatus = 1",
                new String[]{String.valueOf(companyId),String.valueOf(lineId)});
        cnt =cur.getCount();
        return cnt;
    }

}
