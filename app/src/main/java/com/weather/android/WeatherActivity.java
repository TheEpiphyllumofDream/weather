package com.weather.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.weather.android.gson.Forecast;
import com.weather.android.gson.Weather;
import com.weather.android.util.HttpUtil;
import com.weather.android.service.AutoUpdateService;
import com.weather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class WeatherActivity extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;

   // @SuppressLint("ResourceAsColor")

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        drawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout);
        navButton =(Button) findViewById(R.id.nav_button);
        swipeRefresh =(SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        //初始化各控件
        bingPicImg =(ImageView) findViewById(R.id.bing_pic_img);

        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);


        if(weatherString!=null){//尝试从本地缓存中读取天气数据
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId =weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气

            //String weatherId=getIntent().getStringExtra("weather_id");
            //weatherLayout.setVisibility(View .INVISIBLE);
            mWeatherId =getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
           requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        String bingPic =prefs.getString("bing_pic",null);
        if (bingPic !=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     *根据城市id请求城市信息
     */
    public void requestWeather(final String weatherId) {
        //String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=5dd51f969671494795cd0d0c3539c128";
        String weatherUrl ="http://guolin.tech/api/weather?cityid="+weatherId+"&key=45dd25f63300445e967b461d2037e4f9";
        //String weatherUrl="https://free-api.heweather.net/s6/weather?cityid="+weatherId+"&key=33966af0ac0541d094401177cf4bbf7b ";
        //final String aqiUrl = "https://free-api.heweather.com/s6/air/now?location=" + weatherId.toString() + "&key=5cfa71f0523045cbbc2a915848c89ad4";
        //这是对基本天气的访问,但是缺了aqi这一项
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WeatherActivity.this,"获得天气的信息失败",Toast.LENGTH_SHORT).show();
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText=response.body().string();
                        final Weather weather =Utility.handleWeatherResponse(responseText);//调用Utility.handleWeatherResponse()方法将JSON转换成Weather对象
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {//切回主线程进行判断
                                if (weather !=null&&"ok".equals(weather.status)){
                                    SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                    editor.putString("Weather",responseText);
                                    editor.apply();
                                    mWeatherId = weather.basic.weatherId;
                                    showWeatherInfo(weather);

                                }else {
                                    Toast.makeText(WeatherActivity.this,"获得天气的信息失败",Toast.LENGTH_SHORT).show();
                                }
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
                loadBingPic();
    }

    /**
     * 加载每日一图
     */
    private void loadBingPic(){
        String requestBingPic ="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic =response.body().string();
                SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }

    /*
     * 处理并展示Weather实体类中的数据
     * */
    private void showWeatherInfo(Weather weather) {
        /**
         *
         */
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);//从Weather对象中获取数据 然后显示到相应的控件上
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash="洗车指数：" + weather.suggestion.carWash.info;
        String sport="运动指数：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent =new Intent(this ,AutoUpdateService.class);
        startService(intent);
    }
}
