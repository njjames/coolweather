package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 实况天气
 * Created by nj on 2018/2/24.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;  //温度

    @SerializedName("cond_txt")
    public String condtxt;     //天气状态
}
