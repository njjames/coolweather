package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 天气预报
 * Created by nj on 2018/2/24.
 */

public class Forecast {
    public String date; //预报的日期

    @SerializedName("tmp_max")
    public String temperatureMax; //最高温度

    @SerializedName("tmp_min")
    public String temperatureMin; //最低温度

    @SerializedName("cond_txt_d")
    public String condtxt;   //白天天气状况
}
