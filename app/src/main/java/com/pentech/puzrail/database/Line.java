package com.pentech.puzrail.database;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by takashi on 2016/11/24.
 */

public class Line {
    private Context context;
    private Resources res;

    private String TAG = "Line";
    private final String noneName = "------------";
    private int lineId;
    private int areaCode;
    private int companyId;
    private String lineName;
    private String lineKana;
    private int type;
    private String drawable_resource_name;
    private String raw_resource_name;
    private double correct_leftLng;
    private double correct_topLat;
    private double correct_rightLng;
    private double correct_bottomLat;
    private double scroll_max_lat;
    private double scroll_min_lat;
    private double scroll_max_lng;
    private double scroll_min_lng;
    private double init_campos_lat;
    private double init_campos_lng;
    private double max_zoom_level;
    private double min_zoom_level;
    private double init_zoom_level;

    private boolean silhouetteAnswerStatus;
    private boolean locationAnswerStatus;
    private boolean stationAnswerStatus;

    private int silhouetteScore;
    private int locationScore;

    private int totalStationsInLine = 0;
    private int answeredStationsInLine = 0;
    private int stationsTotalScore = 0;

    private int silhouetteMissingCount = 0;
    private int silhouetteShowAnswerCount = 0;
    private int locationShowAnswerCount =0;
    private long locationTime = 0;

    public Line(Context context,
                int lineId,
                int areaCode,
                int companyId,
                String lineName,
                String lineKana,
                int type,
                String drawable_resource_name,
                String raw_resource_name,
                double correct_leftLng,
                double correct_topLat,
                double correct_rightLng,
                double correct_bottomLat,
                double scroll_max_lat,
                double scroll_min_lat,
                double scroll_max_lng,
                double scroll_min_lng,
                double init_campos_lat,
                double init_campos_lng,
                double max_zoom_level,
                double min_zoom_level,
                double init_zoom_level,
                boolean silhouetteAnswerStatus,
                boolean locationAnswerStatus,
                boolean stationAnswerStatus,
                int silhouetteScore,
                int locationScore,
                int locationTime) {

        this.context = context;
        this.res = this.context.getResources();
        this.lineId = lineId;
        this.areaCode = areaCode;
        this.companyId = companyId;
        this.lineName = lineName;
        this.lineKana = lineKana;
        this.type = type;
        this.drawable_resource_name = drawable_resource_name;
        this.raw_resource_name = raw_resource_name;
        this.correct_leftLng = correct_leftLng;
        this.correct_topLat = correct_topLat;
        this.correct_rightLng = correct_rightLng;
        this.correct_bottomLat = correct_bottomLat;
        this.scroll_max_lat = scroll_max_lat;
        this.scroll_max_lng = scroll_max_lng;
        this.scroll_min_lat = scroll_min_lat;
        this.scroll_min_lng = scroll_min_lng;
        this.init_campos_lat = init_campos_lat;
        this.init_campos_lng = init_campos_lng;
        this.max_zoom_level = max_zoom_level;
        this.min_zoom_level = min_zoom_level;
        this.init_zoom_level = init_zoom_level;
        this.silhouetteAnswerStatus = silhouetteAnswerStatus;
        this.locationAnswerStatus = locationAnswerStatus;
        this.stationAnswerStatus = stationAnswerStatus;
        this.silhouetteScore = silhouetteScore;
        this.locationScore = locationScore;
        this.locationTime = (long)locationTime;
    }

    public int getCompanyId(){ return this.companyId; }
    public int getLineId(){ return this.lineId; }
    public String getRawName() { return this.lineName; }
    public String getName() {
        String name = this.noneName;
        if(isSilhouetteCompleted()){
            name = this.lineName;
        }
        return name;
    }
    public String getRawKana() { return this.lineKana; }
    public String getLineKana() {
        String name = this.noneName;
        if(isSilhouetteCompleted()){
            name = this.lineKana;
        }
        return name;
    }
    public int getDrawableResourceId() {
        return this.res.getIdentifier(this.drawable_resource_name, "drawable", this.context.getPackageName());
    }
    public int getRawResourceId() {
        return this.res.getIdentifier(this.raw_resource_name, "raw", this.context.getPackageName());
    }
    public float getMaxZoomLevel(){
        return (float)this.max_zoom_level;
    }
    public float getMinZoomLevel(){
        return (float)this.min_zoom_level;
    }
    public float getInitZoomLevel(){
        return (float)this.init_zoom_level;
    }
    public double getCorrectLeftLng(){
        return this.correct_leftLng;
    }
    public double getCorrectTopLat(){
        return this.correct_topLat;
    }
    public double getCorrectRightLng(){
        return this.correct_rightLng;
    }
    public double getCorrectBottomLat(){
        return this.correct_bottomLat;
    }
    public double getScrollMaxLat(){
        return this.scroll_max_lat;
    }
    public double getScrollMinLat(){
        return this.scroll_min_lat;
    }
    public double getScrollMaxLng(){
        return this.scroll_max_lng;
    }
    public double getScrollMinLng(){
        return this.scroll_min_lng;
    }
    public double getInitCamposLat(){
        return this.init_campos_lat;
    }
    public double getInitCamposLng(){
        return this.init_campos_lng;
    }

    public boolean isSilhouetteCompleted() {return this.silhouetteAnswerStatus; }
    public void setSilhouetteAnswerStatus(){ this.silhouetteAnswerStatus =true; }
    public void resetSilhouetteAnswerStatus() {
        this.silhouetteAnswerStatus =false;
        this.silhouetteScore = 0;
    }

    public boolean isLocationCompleted(){
        return this.locationAnswerStatus;
    }
    public void setLocationAnswerStatus(){
        this.locationAnswerStatus=true;
    }
    public void resetLocationAnswerStatus() {
        this.locationAnswerStatus=false;
        this.locationScore = 0;
        this.locationTime = 0;
    }

    public void incrementSilhouetteMissingCount(){ this.silhouetteMissingCount++; }
    public int getSilhouetteMissingCount() { return this.silhouetteMissingCount; }
    public void incrementSilhouetteShowAnswerCount() { this.silhouetteShowAnswerCount++; }
    public int getLocationShowAnswerCount() { return this.silhouetteShowAnswerCount; }
    public void setSilhouetteScore( int score ){ this.silhouetteScore = score; }
    public int getSilhouetteScore() { return this.silhouetteScore; }

    public void incrementLocationShowAnswerCount() { this.locationShowAnswerCount++; }
    public void setLocationScore(int score) { this.locationScore = score; }
    public int getLocationScore() { return this.locationScore; }

    public void setLocationTime(long time) { this.locationTime = time; }
    public long getLocationTime() { return this.locationTime; }

    // 路線の駅情報アクセッサ
    public int getTotalStationsInLine() {
        return totalStationsInLine;
    }
    public void setTotalStationsInLine(int totalStationsInLine) {
        this.totalStationsInLine = totalStationsInLine;
    }

    public int getAnsweredStationsInLine() {
        return answeredStationsInLine;
    }
    public void setAnsweredStationsInLine(int answeredStationsInLine) {
        this.answeredStationsInLine = answeredStationsInLine;
    }

    public int getStationsTotalScore() {
        return stationsTotalScore;
    }
    public void setStationsTotalScore(int stationsTotalScore) {
        this.stationsTotalScore = stationsTotalScore;
    }
}
