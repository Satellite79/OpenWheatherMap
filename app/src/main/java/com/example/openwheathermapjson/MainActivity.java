package com.example.openwheathermapjson;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openwheathermapjson.db.CitiesDBHelper;
import com.example.openwheathermapjson.db.CitiesTable;
import com.example.openwheathermapjson.CurWeatherModel.*;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String BASE_URL = "https://api.openweathermap.org";
    public static final String API_KEY = "73f5c3fa5369224d66e51b031c56a14e";
    public static final String PICTURE_URL = "http://openweathermap.org/img/w/%1$s.png";
    public static final String UNITS = "metric";
    public static final String CITY_PARM = "CITY_PARM";
    public static final String POS_PARM = "POS_PARM";
    public static final String JSON_PATH = "";  //предполагался url

    private SQLiteStatement statement;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private CurWeatherApi weatherApi = retrofit.create(CurWeatherApi.class);

    public CurWeatherModel weatherModel;

    public Clouds clouds;
    public Coord coord;
    public Main main;
    public Sys sys;
    public Weather weather;
    public Wind wind;

    TextView cityName, description, temperature, wind_view, clouds_view, pressure, geo_coord;
    ImageView picture_view;
    Cursor cursor;
    SpinnerAdapter spinAdapter;
    Spinner spinner;
    Button forecastButton, getCitiesButton;

    CitiesDBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.city_name_view);
        description = findViewById(R.id.description_view);
        temperature = findViewById(R.id.temperature_view);
        wind_view = findViewById(R.id.wind_view);
        clouds_view = findViewById(R.id.clouds_view);
        pressure = findViewById(R.id.pressure_view);
        geo_coord = findViewById(R.id.coord_view);

        picture_view = findViewById(R.id.picture_view);

        forecastButton = findViewById(R.id.forecast_button);
        forecastButton.setOnClickListener(this);

        getCitiesButton = findViewById(R.id.getCities_button);
        getCitiesButton.setOnClickListener(this);

        dbHelper = new CitiesDBHelper(this);
        statement = dbHelper.getWritableDatabase().compileStatement(CitiesTable.TABLE_INSERT);

        cursor = getCityCursor();

        spinner = findViewById(R.id.city_spinner);

        /*  1 adapter version
         spinAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                cursor, new String[]{"name"},
                new int[] {android.R.id.text1} ,
                0);
        */
        spinAdapter = new SpinnerAdapter(this,
                R.layout.spinner_item,
                cursor,
                new String[] {CitiesTable.COLUMN_NAME, CitiesTable.COLUMN_COUNTRY},
                new int[] {R.id.spinner_city, R.id.spinner_country});

        // spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  1 adapter version
        spinner.setAdapter(spinAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    private Cursor getCityCursor() {
        return dbHelper.getReadableDatabase()
                .query(CitiesTable.CITIES_TABLE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String city, countryCode;
        //((TextView) parent.getChildAt(0)).setTextSize(28);  1 adapter version

        cursor.moveToPosition(position);
        city = cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_NAME));
        countryCode = cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_COUNTRY));

        getCurWeather(city, countryCode);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getCurWeather(String city, String countryCode) {

        weatherApi.getData(city + "," + countryCode, API_KEY, UNITS).enqueue(new Callback<CurWeatherModel>() {
            @Override
            public void onResponse(Call<CurWeatherModel> call, Response<CurWeatherModel> response) {
                if(response.body() != null){
                    weatherModel = response.body();
                    coord = weatherModel.getCoord();
                    weather = weatherModel.getWeather().get(0); // непонятно, в каких случаях не 0 будет
                    sys = weatherModel.getSys();
                    main = weatherModel.getMain();
                    wind = weatherModel.getWind();
                    clouds = weatherModel.getClouds();
                    coord = weatherModel.getCoord();
                    String icon = weather.getIcon();
                    String pictureUrl = String.format(PICTURE_URL, icon);

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
                Toast.makeText(MainActivity.this,
                        getString(R.string.requestFailureText) + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void searchViewClick(View view) { // для разнообразия ( в onClick)
        searchViewShow();
    }

    private void searchViewShow() {
        Intent intent = new Intent(this, SearchViewActivity.class);
        intent.putExtra(CITY_PARM, cursor.getString(cursor.getColumnIndex(CitiesTable.COLUMN_NAME)));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forecast_button:
                showForecast();
                break;
            case R.id.getCities_button:
                try {
                     getCities();
                     // new ParseTask().execute();  пример получения json с внешнего источника
                }
                catch (Exception e){
                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showForecast()
    {
        Cursor cursor = spinAdapter.getCursor();

        Intent intent = new Intent(this, ForecastActivity.class);
        intent.putExtra(POS_PARM, cursor.getPosition());

        startActivity(intent);
    }

    private void getCities() throws IOException, JSONException {
        JSONArray   citiesArray;
        JSONObject  cityData;

        String jsonText = readJSONFile();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        db.execSQL(CitiesTable.TABLE_DELETE);

        citiesArray = new JSONArray(jsonText);
        for (int i = 0; i < citiesArray.length(); i++)
        {
            cityData = citiesArray.getJSONObject(i);
            Long city_id      = cityData.getLong("id");
            String cityName     = cityData.getString("name");
            String cityCountry  = cityData.getString("country");

            statement.bindString(1, cityName);
            statement.bindString(2, cityCountry);
            statement.bindString(3, String.valueOf(city_id));
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        cursor = getCityCursor();
        spinAdapter.swapCursor(cursor);
        spinner.setSelection(0);
    }

    private String readJSONFile() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        InputStream is = getResources().openRawResource(R.raw.city_list);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        while ((s = br.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }

    /* Пример получения json с внешнего источника
    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL(JSON_PATH);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            JSONObject dataJsonObj = null;
            ...
            dataJsonObj = new JSONObject(strJson);

            // разбор json по аналогии с getCities
            // ...
        }
    }
    */
}
