package com.pentech.puzrail.database;

/**
 * Created by c0932 on 2017/05/11.
 */

public class SettingParameter {

    // レベル設定
    public final static int DIFFICULTY_PROFESSIONAL = 0;
    public final static int DIFFICULTY_AMATEUR = 1;
    public final static int DIFFICULTY_BEGINNER = 2;

    private int difficulty_mode = 0;
    private boolean vibrationMode = true;
    private boolean fabVisibility = true;

    public SettingParameter( int dif, boolean vib, boolean fabVisibility){
        this.difficulty_mode = dif;
        this.vibrationMode = vib;
        this.fabVisibility = fabVisibility;
    }

    public int getDifficultyMode(){
        return this.difficulty_mode;
    }

    public boolean isVibrate(){
        return this.vibrationMode;
    }

    public boolean isFabVisibility() { return this.fabVisibility; }

    public void setDifficultyMode(int dif){
        this.difficulty_mode = dif;
    }

    public void setVibrationMode(boolean mode){
        this.vibrationMode = mode;
    }

    public void setFabVisibility(boolean fabVisibility) { this.fabVisibility = fabVisibility; }
}
