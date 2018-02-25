package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 天气类
 * Created by nj on 2018/2/24.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public Update update;
    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
