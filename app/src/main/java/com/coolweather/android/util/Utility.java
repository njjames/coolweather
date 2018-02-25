package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.AQI;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-02-23.
 */

public class Utility {

    /**
     * 解析并处理（保存到数据库中）服务器返回的省级数据
     * @param response 服务器返回的json字符串
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析并处理（保存到数据库中）服务器返回的市级数据
     * @param response 服务器返回的json字符串
     * @param provinceId 市所属的省
     * @return
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCitys = new JSONArray(response);
                for (int i = 0; i < allCitys.length(); i++) {
                    JSONObject cityObject = allCitys.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析并处理（保存到数据库中）服务器返回的县级数据
     * @param response 服务器返回的json字符串
     * @param cityId 县所属的市
     * @return
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCountys = new JSONArray(response);
                for (int i = 0; i < allCountys.length(); i++) {
                    JSONObject countyObject = allCountys.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 根据返回的json字符串，解析成为Weather对象
     * @param weatherResponse
     * @return
     */
    public static Weather handleWeatherResponse(String weatherResponse) {
        try {
            //先解析返回的json字符串为JSONObject
            JSONObject jsonObject = new JSONObject(weatherResponse);
            //获取key为HeWeather6的jsonArray
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            //获取JSONArray的第一个值(JSONObject)，并转化为json格式的字符串
            String weatherContent = jsonArray.getJSONObject(0).toString();
            //利用GSON来解析上面的字符串(这里是没有AQI的)
            Weather weather = new Gson().fromJson(weatherContent, Weather.class);
            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据返回的json字符串，解析成为AQI对象
     * @param aqiResponse
     * @return
     */
    public static AQI handleAQIResponse(String aqiResponse) {
        try {
            //解析aqiResponse获取AQI信息
            JSONObject aqijsonObject = new JSONObject(aqiResponse);
            JSONArray aqijsonArray = aqijsonObject.getJSONArray("HeWeather6");
            String air_now_city = aqijsonArray.getJSONObject(0).getJSONObject("air_now_city").toString();
            AQI aqi = new Gson().fromJson(air_now_city, AQI.class);
            return aqi;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
