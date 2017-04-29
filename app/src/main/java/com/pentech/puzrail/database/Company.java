package com.pentech.puzrail.database;

/**
 * Created by takashi on 2016/12/20.
 */

public class Company {

    private int companyId;
    private int companyCode;
    private String companyName;
    private String companyKana;

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

}
