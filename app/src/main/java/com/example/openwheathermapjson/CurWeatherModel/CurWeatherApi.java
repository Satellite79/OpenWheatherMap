package com.example.openwheathermapjson.CurWeatherModel;

//https://api.openweathermap.org
// /data/2.5/weather?
// q=Moscow,ru&
// appid=73f5c3fa5369224d66e51b031c56a14e&
// units=metric

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurWeatherApi {
    @GET("/data/2.5/weather")
    Call<CurWeatherModel> getData(@Query("q") String cityName, // с кодом страны
                                  @Query("appid") String apiKey,
                                  @Query("units") String units
    );
}
