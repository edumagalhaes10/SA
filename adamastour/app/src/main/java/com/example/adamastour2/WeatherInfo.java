package com.example.adamastour2;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherInfo {

    private static final String BASE_URL = "https://api.openweathermap.org/data/3.0/";
    private static final String API_KEY = "cb155ed8c4d500480bbe1030b521ada8";

    private WeatherService service;

    public WeatherInfo() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherService.class);
    }

    public void getWeatherData() {
        Call<WeatherResponse> call = service.getWeather("London", API_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    float temperature = weatherResponse.getMain().getTemp();
                    String description = weatherResponse.getWeather().get(0).getDescription();
                    // Handle the received data from the API
                    Log.d("WeatherResponse", "Response: " + response.body().toString());
                } else {
                    // Handle HTTP response errors
                    Log.d("ERROR", "Response: " + response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Handle request errors
            }
        });
    }
}