package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.coolweather.android.db.County;
import com.coolweather.android.gson.AQI;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        Log.d(TAG, "服务更新时间：" + format);
        updateWeather();
        updateAqi();
        updateBingPic();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //更新一次
        int anHour = 8 * 60 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + anHour;
        Intent serviceIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        alarmManager.cancel(pendingIntent);
        //设置定时任务，执行服务
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新AQI信息
     */
    private void updateAqi() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weather = sharedPreferences.getString("weather", null);
        if (weather != null) {
            String weatherId = Utility.handleWeatherResponse(weather).basic.weatherId;
            String cityWeatherId = getCityWeatherId(weatherId);
            String aqiAddress = "https://free-api.heweather.com/s6/air/now?location=" + cityWeatherId + "&key=e01a58275e4c4248a2ea86c17e994b64";
            HttpUtil.sendOkHttpRequest(aqiAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String aqiString = response.body().string();
                    AQI aqi = Utility.handleAQIResponse(aqiString);
                    if (aqi != null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("aqi", aqiString);
                        editor.apply();
                    }
                }
            });
        }
    }

    /**
     * 需要根据当前的weatherId获取到县所属市中的第一个县的weatherId
     * @param weatherId
     * @return
     */
    private String getCityWeatherId(String weatherId) {
        //获取weatherId对应的county
        County county = DataSupport.where("weatherId = ?", weatherId).findFirst(County.class);
        //获取县所属的市
        int cityId = county.getCityId();
        //获取该市所属的第一个县
        County firstCounty = DataSupport.where("cityId = ?", String.valueOf(cityId)).findFirst(County.class);
        return firstCounty.getWeatherId();
    }

    /**
     * 更新背景图的地址到SP中
     */
    private void updateBingPic() {
        String bingUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingAddress = response.body().string();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("bingaddress", bingAddress);
                editor.apply();
            }
        });


    }

    /**
     * 更新天气信息，就是更新到SP中
     */
    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weather = sharedPreferences.getString("weather", null);
        if (weather != null) {
            String weatherId = Utility.handleWeatherResponse(weather).basic.weatherId;
            String weatherAddress = "https://free-api.heweather.com/s6/weather?location=" + weatherId + "&key=e01a58275e4c4248a2ea86c17e994b64";
            HttpUtil.sendOkHttpRequest(weatherAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherString = response.body().string();
                    Weather weather1 = Utility.handleWeatherResponse(weatherString);
                    if (weather1 != null && "ok".equals(weather1.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", weatherString);
                        editor.apply();
                    }
                }
            });
        }
    }

}
