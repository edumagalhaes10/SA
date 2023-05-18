package com.example.adamastour2;


import org.json.JSONObject;
import org.json.JSONException;

public class WeatherData {
    private String temperature, icon, city, weatherType;
    private int condition;

    public static WeatherData fromJson(JSONObject jsonObject){
        try {
            WeatherData weatherData = new WeatherData();
            weatherData.city=jsonObject.getString("name");
            weatherData.condition=jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherData.weatherType=jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            weatherData.icon=updateWeatherIcon(weatherData.condition);

            double tempResult = jsonObject.getJSONObject("main").getDouble("temp")-273.15;
            int roundedValue = (int) Math.rint(tempResult);
            weatherData.temperature = Integer.toString(roundedValue);

            return weatherData;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String updateWeatherIcon(int condition) {
        if(condition >= 210 && condition <= 221) {
            return "thunderstorm";
        }
        else if((condition >= 200 && condition <= 202) || (condition >= 230 && condition <= 232) ) {
            return "thunderstorm2";
        }
        else if(condition >= 300 && condition < 500) {
            return "lightrain";
        }
        else if(condition >= 500 && condition < 600) {
            return "shower";
        }
        else if(condition >= 600 && condition <= 700) {
            return "snow";
        }
        else if(condition >= 701 && condition <= 771) {
            return "fog";
        }
        else if(condition == 781) {
            return "tornado";
        }
        else if(condition == 800) {
            return "sunny";
        }
        else if(condition == 801) {
            return "cloudy";
        }
        else if(condition == 803 || condition == 802) {
            return "cloud";
        }
        else if(condition == 804) {
            return "overcast";
        }

        return "dunno";
    }

    public String getTemperature() {
        return temperature + "ºC";
    }

    public String getIcon() {
        return icon;
    }

    public String getCity() {
        return city;
    }

    public String getWeatherType() {
        return weatherType;
    }
}
