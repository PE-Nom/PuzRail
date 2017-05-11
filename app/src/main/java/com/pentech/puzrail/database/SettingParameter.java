package com.pentech.puzrail.database;

/**
 * Created by c0932 on 2017/05/11.
 */

public class SettingParameter {
    private int difficulty_mode = 0;
    private boolean vibrationMode = true;

    public SettingParameter( int dif, boolean vib){
        this.difficulty_mode = dif;
        this.vibrationMode = vib;
    }

    public int getDifficultyMode(){
        return this.difficulty_mode;
    }

    public boolean isVibrate(){
        return this.vibrationMode;
    }

    public void setDifficultyMode(int dif){
        this.difficulty_mode = dif;
    }

    public void setVibrationMode(boolean mode){
        this.vibrationMode = mode;
    }
}
