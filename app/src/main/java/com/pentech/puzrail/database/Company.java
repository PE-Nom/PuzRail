package com.pentech.puzrail.database;

/**
 * Created by takashi on 2016/12/20.
 */

public class Company {

    private int companyId;
    private int companyCode;
    private String companyName;
    private String companyKana;
    private int companyTotalScore;
    private int silhouetteTotalScore;
    private int locationTotalScore;
    private int stationsTotalScore;

    public Company(int companyId, int companyCode, String companyName, String companyKana){
        this.companyId = companyId;
        this.companyCode = companyCode;
        this.companyName = companyName;
        this.companyKana = companyKana;
    }

    public int getId() { return this.companyId; }
    public int getCode(){
        return this.companyCode;
    }
    public String getName(){
        return this.companyName;
    }
    public String getKana() { return this.companyKana; }

    public int getCompanyTotalScore() { return this.companyTotalScore; }
    public int getSilhouetteTotalScore() { return this.silhouetteTotalScore; }
    public int getLocationTotalScore() { return this.locationTotalScore; }
    public int getStationsTotalScore() { return this.stationsTotalScore; }

    public void setCompanyTotalScore(int score){
        this.companyTotalScore = score;
    }
    public void setSilhouetteTotalScore(int score){
        this.silhouetteTotalScore = score;
    }
    public void setLocationTotalScore(int score){
        this.locationTotalScore = score;
    }
    public void setStationsTotalScore(int score){
        this.stationsTotalScore = score;
    }

}
