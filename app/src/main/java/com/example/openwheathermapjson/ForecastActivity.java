package com.example.openwheathermapjson;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openwheathermapjson.ForecastModel.*;
import com.example.openwheathermapjson.db.CitiesDBHelper;
import com.example.openwheathermapjson.db.CitiesTable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForecastActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private ForecastApi forecastApi = retrofit.create(ForecastApi.class);

    public City city;
    public ForecastModel forecastModel;
    public List list;
    public Rain rain;
    public Snow snow;

    public Clouds clouds;
    public Coord coord;
    public Main main;
    public Sys sys;
    public Weather weather;
    public Wind wind;

    TextView cityName, description, temperature, wind_view, clouds_view, pressure, geo_coord;
    Cursor cursor;
    SpinnerAdapter spinAdapter;
    Spinner spinner;
    java.util.List<ForecastItem> forecastItemList;
    ForecastAdapter forecastAdapter;
    RecyclerView recyclerView;

    CitiesDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Intent intent = getIntent();
        int posParm = intent.getIntExtra(MainActivity.POS_PARM, 0);

        dbHelper = new CitiesDBHelper(this);
        cursor = dbHelper.getReadableDatabase()
                .query(CitiesTable.CITIES_TABLE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        spinner = findViewById(R.id.city_spinner);

        spinAdapter = new SpinnerAdapter(this,
                R.layout.spinner_item,
                cursor,
                new String[]{CitiesTable.COLUMN_NAME, CitiesTable.COLUMN_COUNTRY},
                new int[]{R.id.spinner_city, R.id.spinner_country});

        spinner.setAdapter(spinAdapter);
        spinner.setSelection(posParm);
        spinner.setOnItemSelectedListener(this);

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String city, countryCode;

        cursor.moveToPosition(position);
        city = cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_NAME));
        countryCode = cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_COUNTRY));

        getForecastWeather(city, countryCode);
    }

    private void getForecastWeather(String cityName, String countryCode) {
        forecastApi.getData(cityName + "," + countryCode, MainActivity.API_KEY, MainActivity.UNITS).enqueue(new Callback<ForecastModel>() {
            @Override
            public void onResponse(Call<ForecastModel> call, Response<ForecastModel> response) {
                if (response.body() != null) {
                    int i = 0;
                    double snow3h = 0;
                    double rain3h = 0;
                    String precipitationStr = "";
                    String rainStr = "";
                    String snowStr = "";

                    forecastItemList = new ArrayList<>();
                    for (List list : response.body().getList()) {
                        main = list.getMain();
                        wind = list.getWind();
                        try {
                            rain = list.getRain();
                            if (rain != null)
                                rain3h = rain.get3h();
                        }
                        catch(Throwable exceptionRain) { rain3h = 0; }
                        try {
                            snow = list.getSnow();
                            if (snow != null)
                                snow3h = snow.get3h();
                        }
                        catch(Throwable exceptionSnow) { snow3h = 0; }

                        String dtText = list.getDtTxt();

                        if (rain3h > 0) {
                            rain3h = new BigDecimal(rain3h).setScale(2, RoundingMode.UP).doubleValue(); // округление до десятых !
                            rainStr = String.format(getString(R.string.rain), String.valueOf(rain3h));
                        }
                        if (snow3h > 0) {
                            snow3h = new BigDecimal(snow3h).setScale(2,RoundingMode.UP).doubleValue();
                            snowStr = String.format(getString(R.string.snow), String.valueOf(snow3h));
                        }
                        if (rain3h == 0 && snow3h == 0)   // жаль, не поддерживается switch(true)
                            precipitationStr = getString(R.string.noPrecipitation);
                        else if (rain3h > 0 && snow3h == 0)
                            precipitationStr = rainStr;
                        else if (rain3h == 0 && snow3h > 0)
                            precipitationStr = snowStr;
                        else // предположим и дождь и снег могут быть на выходе
                            precipitationStr = String.format(getString(R.string.precipitation), rainStr, snowStr);

                        String description = String.format(getString(R.string.forecastStr),
                            String.valueOf(main.getTemp().intValue()), getString(R.string.tempUnit),
                            wind.getSpeed().toString(), getString(R.string.windUnit),
                            main.getHumidity().toString(), getString(R.string.percent),
                            String.valueOf(main.getPressure().intValue()), getString(R.string.pressUnit),
                            precipitationStr);

                        forecastItemList.add(i, new ForecastItem(dtText, description));
                        i++;
                    }

                    forecastAdapter = new ForecastAdapter(forecastItemList);
                    recyclerView.setAdapter(forecastAdapter);
                }
            }

            @Override
            public void onFailure(Call<ForecastModel> call, Throwable t) {

            }
        });
    }

    @Override
    public void onNothingSelected (AdapterView < ? > parent){

    }
}
