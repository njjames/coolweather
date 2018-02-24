package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 基本信息
 * Created by nj on 2018/2/24.
 */

public class Basic {
    @SerializedName("location")
    public String cityName;   //地区的名字
    @SerializedName("cid")
    public String weatherId;  //地区对应的天气id
}
