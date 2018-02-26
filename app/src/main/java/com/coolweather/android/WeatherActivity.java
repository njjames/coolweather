package com.coolweather.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.AQI;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Lifestyle;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView mTitleCity;
    private TextView mTitleUpdateTime;
    private TextView mDegressText;
    private TextView mWeatherInfoText;
    private LinearLayout mForecastLayout;
    private TextView mAqiText;
    private TextView mPm25Text;
    private TextView mComfortText;
    private TextView mSportText;
    private TextView mCarWashText;
    private ScrollView mWeatherLayout;
    private ImageView mBingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mWeatherLayout = findViewById(R.id.weather_layout);
        mTitleCity = findViewById(R.id.title_city);
        mTitleUpdateTime = findViewById(R.id.title_update_time);
        mDegressText = findViewById(R.id.degress_text);
        mWeatherInfoText = findViewById(R.id.weather_info_text);
        mForecastLayout = findViewById(R.id.forecast_layout);
        mAqiText = findViewById(R.id.aqi_text);
        mPm25Text = findViewById(R.id.pm25_text);
        mComfortText = findViewById(R.id.comfort_text);
        mSportText = findViewById(R.id.sport_text);
        mCarWashText = findViewById(R.id.car_wash_text);
        mBingPicImg = findViewById(R.id.bing_pic_img);

        //获取sp，先充sp中获取天气信息
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather", null);
        String aqiString = sharedPreferences.getString("aqi", null);
        //从SP中获取背景图的地址
        String bingaddress = sharedPreferences.getString("bingaddress", null);
        //如果有就直接加载为背景图，否则去服务器上请求
        if (bingaddress != null) {
            Glide.with(this).load(bingaddress).into(mBingPicImg);
        }else {
            loadBingPic();
        }
        //如果可以获取到，就解析，然后显示出来
        if (weatherString != null && aqiString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            AQI aqi = Utility.handleAQIResponse(aqiString);
            showWeatherInfo(weather);
            showAQIInfo(aqi);
        }else {
            //否则就去服务器获取天气信息
            String weatherId = getIntent().getStringExtra("weatherId");
            //请求数据时把ScrollView隐藏
            mWeatherLayout.setVisibility(View.GONE);
            requestWeather(weatherId);
            requestAQI(weatherId);
        }

    }

    /**
     * 加载必应背景图片
     */
    private void loadBingPic() {
        String bingUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "获取背景图片失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingAddress = response.body().string();
                if (bingAddress != null) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("bingaddress", bingAddress);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(bingAddress).into(mBingPicImg);
                        }
                    });
                }else {
                    Toast.makeText(WeatherActivity.this, "获取背景图片失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 根据天气ID获取空气质量信息
     * @param weatherId
     */
    private void requestAQI(String weatherId) {
        final String aqiAddress = "https://free-api.heweather.com/s6/air/now?location=" + weatherId + "&key=e01a58275e4c4248a2ea86c17e994b64";
        HttpUtil.sendOkHttpRequest(aqiAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取空气质量信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String aqiString = response.body().string();
                final AQI aqi = Utility.handleAQIResponse(aqiString);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (aqi != null) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("aqi", aqiString);
                            editor.apply();
                            showAQIInfo(aqi);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取空气质量信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 从服务器上，根据天气id，获取天气信息
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        String weatherAddress = "https://free-api.heweather.com/s6/weather?location=" + weatherId + "&key=e01a58275e4c4248a2ea86c17e994b64";
        HttpUtil.sendOkHttpRequest(weatherAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherString = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(weatherString);
                //必须在主线程中更新UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("weather", weatherString);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        //在请求天气信息时也会得到最新的图片
        loadBingPic();
    }

    /**
     * 把天气信息显示出来
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime;
        String temperature = weather.now.temperature + "℃";
        String condtxt = weather.now.condtxt;

        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegressText.setText(temperature);
        mWeatherInfoText.setText(condtxt);

        //先清除所有的控件
        mForecastLayout.removeAllViews();
        List<Forecast> forecastList = weather.forecastList;
        for (Forecast forecast : forecastList) {
            //必须放在循环内，否则就显示的都一样了
            View view = View.inflate(this, R.layout.forecast_item, null);
            TextView dataText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.condtxt);
            maxText.setText(forecast.temperatureMax);
            minText.setText(forecast.temperatureMin);
            mForecastLayout.addView(view);
        }

        List<Lifestyle> lifestyleList = weather.lifestyleList;
        for (Lifestyle lifestyle : lifestyleList) {
            switch (lifestyle.type) {
                case "comf":
                    mComfortText.setText("舒适度：" + lifestyle.lifttext);
                    break;
                case "cw":
                    mCarWashText.setText("洗车指数：" + lifestyle.lifttext);
                    break;
                case "sport":
                    mSportText.setText("运动建议：" + lifestyle.lifttext);
                    break;
                default:

            }
        }
        //显示数据的时候把ScrollView显示出来
        mWeatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 把空气质量信息显示出来
     * @param aqi
     */
    private void showAQIInfo(AQI aqi) {
        if (aqi != null) {
            mAqiText.setText(aqi.aqi);
            mPm25Text.setText(aqi.pm25);
        }
    }
}
