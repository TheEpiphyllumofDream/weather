package com.weather.android.gson;

import com.google.gson.annotations.SerializedName;
import java.security.PublicKey;

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public  String updateTime;
    }
}
