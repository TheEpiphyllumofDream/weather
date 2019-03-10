package com.weather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.weather.android.gson.Weather;
import com.weather.android.util.HttpUtil;
import com.weather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        updateWeather();
        updateBingPic();
        AlarmManager manager =(AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*100;//这是8小时的毫秒数
        long triggerAtTime =SystemClock.elapsedRealtime()+anHour;
        Intent i =new Intent(this,AutoUpdateService.class);
        PendingIntent pi =PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }
    /**
     * 更新天气信息
     */
    private void  updateWeather(){
        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString =prefs.getString("weather",null);
        if (weatherString !=null){
            Weather weather =Utility.handleWeatherResponse(weatherString);
            String weatherId =weather.basic.weatherId;//String weatherId = weather.getHeWeather6().get(0).getBasicX().getCid();

            //String weatherUrl="http://guolin.tech/api/weather?cityid"+weatherId +"&key=5dd51f969671494795cd0d0c3539c128";
            String weatherUrl="http://guolin.tech/api/weather?cityid"+weatherId +"&key=45dd25f63300445e967b461d2037e4f9";


            //String weatherId = weather.getHeWeather6().get(0).getBasicX().getCid();

            //String weatherUrl =  "https://free-api.heweather.com/s6/weather?cityid=" + weatherId.toString() + "&key=5cfa71f0523045cbbc2a915848c89ad4";

            //String weatherUrl="https://free-api.heweather.net/s6/weather?cityid="+weatherId+"&key=33966af0ac0541d094401177cf4bbf7b";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText =response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }

                }
            });
        }
    }
    /**
     * 更新每日一图
     */
    private void updateBingPic(){
        String requestBingPic ="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic =response.body().string();
                SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });

    }

}
