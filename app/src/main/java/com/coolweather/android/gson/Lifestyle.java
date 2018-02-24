package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 生活指数
 * Created by nj on 2018/2/24.
 */

public class Lifestyle {
    @SerializedName("brf")
    public String lifebrf;  //生活指数简介

    @SerializedName("txt")
    public String lifttext; //生活指数信息内容

    public String type;  //生活指数类型
}
