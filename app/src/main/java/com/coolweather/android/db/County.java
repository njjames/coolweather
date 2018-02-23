package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * 县
 * Created by Administrator on 2018-02-23.
 */

public class County extends DataSupport {
    private int id;
    private String countyName;
    private int cityId;
    private String weatherId;  //县所对应的天气id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
}
