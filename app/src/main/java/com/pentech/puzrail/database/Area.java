package com.pentech.puzrail.database;

/**
 * Created by takashi on 2017/01/06.
 */

public class Area {
    private int code = 0;
    private String name = "";

    public Area(int code,String name){
        this.code = code;
        this.name = name;
    }

    public int getCode(){
        return this.code;
    }

    public String getName() {
        return this.name;
    }
}
