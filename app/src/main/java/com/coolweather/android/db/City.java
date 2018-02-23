package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * 市
 * Created by Administrator on 2018-02-23.
 */

public class City extends DataSupport {
    private int id;
    private String cityName; //市名称
    private int cityCode;    //市代码
    private int provinceId;  //市所属省的id

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }
}
