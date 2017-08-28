package com.pentech.puzrail.database;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

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

    private boolean nameAnswerStatus;
    private boolean locationAnswerStatus;
    private boolean stationAnswerStatus;

    private int silhouetteScore;
    private int locationScore;

    private int silhouetteMissingCount = 0;
    private int silhouetteShowAnswerCount = 0;
    private int locationShowAnswerCount = 0;

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
                boolean nameAnswerStatus,
                boolean locationAnswerStatus,
                boolean stationAnswerStatus,
                int silhouetteScore,
                int locationScore) {

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
        this.nameAnswerStatus = nameAnswerStatus;
        this.locationAnswerStatus = locationAnswerStatus;
        this.stationAnswerStatus = stationAnswerStatus;
        this.silhouetteScore = silhouetteScore;
        this.locationScore = locationScore;
    }

    public int getCompanyId(){ return this.companyId; }
    public int getLineId(){ return this.lineId; }
    public String getRawName() { return this.lineName; }
    public String getName() {
        String name = this.noneName;
        if(isNameCompleted()){
            name = this.lineName;
        }
        return name;
    }
    public String getRawKana() { return this.lineKana; }
    public String getLineKana() {
        String name = this.noneName;
        if(isNameCompleted()){
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

    public boolean isNameCompleted() {return this.nameAnswerStatus; }
    public void setNameAnswerStatus(){ this.nameAnswerStatus=true; }
    public void resetNameAnswerStatus() { this.nameAnswerStatus=false; }

    public boolean isLocationCompleted(){
        return this.locationAnswerStatus;
    }
    public void setLocationAnswerStatus(){
        this.locationAnswerStatus=true;
    }
    public void resetLocationAnswerStatus(){
        this.locationAnswerStatus=false;
    }

    public boolean isStationCompleted(){
        return this.stationAnswerStatus;
    }
    public void setStationAnswerStatus() { this.stationAnswerStatus=true; }
    public void resetStationAnswerStatus() { this.stationAnswerStatus=false; }

    public void incrementSilhouetteMissingCount(){ this.silhouetteMissingCount++; }
    public void incrementSilhouetteShowAnswerCount() { this.silhouetteShowAnswerCount++; }
    public int computeSilhouetteScore(int remainLineCount){
        int sc = remainLineCount - ( this.silhouetteMissingCount + this.silhouetteShowAnswerCount*2 );
        if( sc < 0 ) this.silhouetteScore = 0;
        else         this.silhouetteScore = sc;
        return this.silhouetteScore;
    }
    public int getSilhouetteScore() { return this.silhouetteScore; }

    public void incrementLocationShowAnswerCount() { this.locationShowAnswerCount++; }
    public int computeLocationScore(int remainTime){
        int sc = remainTime - this.locationShowAnswerCount*5;
        if( sc < 0 ) this.locationScore = 0;
        else         this.locationScore = sc;
        return this.locationScore;
    }
    public int getLocationScore() { return this.locationScore; }
}
