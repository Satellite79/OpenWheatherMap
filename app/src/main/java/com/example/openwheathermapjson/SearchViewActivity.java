package com.example.openwheathermapjson;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openwheathermapjson.CurWeatherModel.*;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchViewActivity extends AppCompatActivity {

    SearchView searchView;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private CurWeatherApi weatherApi = retrofit.create(CurWeatherApi.class);
    private String cityParm;

    public CurWeatherModel weatherModel;

    public Clouds clouds;
    public Coord coord;
    public Main main;
    public Sys sys;
    public Weather weather;
    public Wind wind;

    TextView cityName, description, temperature, wind_view, clouds_view, pressure, geo_coord;
    ImageView picture_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);

        Intent intent = getIntent();
        cityParm = intent.getStringExtra(MainActivity.CITY_PARM);

        cityName = findViewById(R.id.city_name_view);
        description = findViewById(R.id.description_view);
        temperature = findViewById(R.id.temperature_view);
        wind_view = findViewById(R.id.wind_view);
        clouds_view = findViewById(R.id.clouds_view);
        pressure = findViewById(R.id.pressure_view);
        geo_coord = findViewById(R.id.coord_view);

        picture_view = findViewById(R.id.picture_view);

        getWeather(cityParm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_search) {
            handleSearch(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSearch(MenuItem item) {
        searchView = (SearchView) item.getActionView();

        item.expandActionView();

        searchView.setQuery(cityParm, false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getWeather(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void getWeather(String city)
    {
        weatherApi.getData(city, MainActivity.API_KEY, MainActivity.UNITS).enqueue(new Callback<CurWeatherModel>() {
            @Override
            public void onResponse(Call<CurWeatherModel> call, Response<CurWeatherModel> response) {
                if(response.body() != null){
                    weatherModel = response.body();
                    coord = weatherModel.getCoord();
                    weather = weatherModel.getWeather().get(0);
                    sys = weatherModel.getSys();
                    main = weatherModel.getMain();
                    wind = weatherModel.getWind();
                    clouds = weatherModel.getClouds();
                    coord = weatherModel.getCoord();
                    String icon = weather.getIcon();
                    String pictureUrl = String.format(MainActivity.PICTURE_URL, icon);

                    cityName.setText(String.format(getString(R.string.cityCountry), weatherModel.getName(), sys.getCountry()));
                    description.setText(weather.getDescription());
                    temperature.setText(String.format(getString(R.string.valueUnit), String.valueOf(main.getTemp().intValue()), getString(R.string.tempUnit)));
                    wind_view.setText(String.format(getString(R.string.valueUnit), wind.getSpeed().toString(), getString(R.string.windUnit)));
                    clouds_view.setText(String.format(getString(R.string.valueUnit), clouds.getAll().toString(),getString(R.string.percent)));
                    pressure.setText(String.format(getString(R.string.valueUnit), main.getPressure().toString(),getString(R.string.pressUnit)));
                    geo_coord.setText(String.format(getString(R.string.coordStr),coord.getLon().toString(),coord.getLat().toString()));

                    Picasso.get()
                            .load(pictureUrl)  //R.drawable.picture
                            .fit()
                            .centerCrop()
                            .into(picture_view);

                }
            }

            @Override
            public void onFailure(Call<CurWeatherModel> call, Throwable t) {
                Toast.makeText(SearchViewActivity.this,
                        getString(R.string.requestFailureText) + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
