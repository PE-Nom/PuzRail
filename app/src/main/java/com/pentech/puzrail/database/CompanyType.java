package com.pentech.puzrail.database;

/**
 * Created by takashi on 2017/01/06.
 */

public class CompanyType {
    private int companyCode;
    private String typeName;

    public CompanyType(int code,String name){
        this.companyCode = code;
        this.typeName = name;
    }

    public int getCode() { return this.companyCode; }
    public String getName() { return this.typeName; }
}
