package com.example.openwheathermapjson;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;

import com.example.openwheathermapjson.ForecastModel.ForecastApi;
import com.example.openwheathermapjson.ForecastModel.ForecastModel;
import com.example.openwheathermapjson.ForecastModel.List;
import com.example.openwheathermapjson.ForecastModel.Rain;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.ALARM_SERVICE;

public class WeatherBootReceiver extends BroadcastReceiver {
    private Intent mIntent;
    private PendingIntent mPendingIntent;
    public boolean isRain;
    private Calendar now = Calendar.getInstance();
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            WeatherBootReceiver.setAlarm(context); // установить слетевшую сигнализацию
        }
        else
        {
            this.context = context;
            this.checkRainForecast();
        }
    }

    public void checkRainForecast() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ForecastApi forecastApi = retrofit.create(ForecastApi.class);
        forecastApi.getData(MainActivity.DEFAULT_CITY + "," + MainActivity.DEFAULT_COUNTRY,
                  MainActivity.API_KEY,
                  MainActivity.UNITS).enqueue(new Callback<ForecastModel>() {
            @Override
            public void onResponse(Call<ForecastModel> call, Response<ForecastModel> response) {
                Rain rain;
                double rain3h = 0;
                int dt = 0;
                Calendar calendar = Calendar.getInstance();

                if (response.body() != null) {
                    for (List list : response.body().getList()) {
                        try {
                            dt = list.getDt();
                            calendar.setTimeInMillis((long)dt*1000);

                            rain = list.getRain();
                            if (rain != null)
                                rain3h = rain.get3h();

                            if (rain3h > 0) isRain = true;
                        }
                        catch(Throwable exceptionRain) { rain3h = 0;}

                        if (isRain || (calendar.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)))
                            break;
                    }
                    if (!isRain) {  // send notify, ! - special for debugging
                        mIntent = new Intent(WeatherBootReceiver.this.context, ForecastActivity.class);

                        mPendingIntent = PendingIntent.getActivity(WeatherBootReceiver.this.context, 0,
                                mIntent, 0);

                        NotificationManager notificationManager = (NotificationManager)
                                WeatherBootReceiver.this.context.getSystemService(Context.NOTIFICATION_SERVICE);

                        Notification notification = new Notification.Builder(WeatherBootReceiver.this.context)
                                .setContentIntent(mPendingIntent)
                                .setContentText(WeatherBootReceiver.this.context.getString(R.string.contentText))
                                .setContentTitle(WeatherBootReceiver.this.context.getString(R.string.contentTitle))
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .setPriority(Notification.PRIORITY_HIGH)
                                //.setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .build();

                        notificationManager.notify(1, notification);
                    }
                }
            }

            @Override
            public void onFailure(Call<ForecastModel> call, Throwable t) {

            }
        });
    }


    public static void setAlarm(Context context)
    {
        Intent intent = new Intent(context, WeatherBootReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }
}