package com.example.adamastour2;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class PollutionData {
    private String icon, aqi;
    private int condition;
    private static final String TAG = "PollutionData";

    public static PollutionData fromJson(JSONObject jsonObject){
        try {
            PollutionData pollutionData = new PollutionData();
            pollutionData.condition=jsonObject.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi");
            Log.d(TAG, "AQI - " + pollutionData.condition);
            pollutionData.icon=updatePollutionIcon(pollutionData.condition);

            pollutionData.aqi = Integer.toString(pollutionData.condition);

            return pollutionData;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String updatePollutionIcon(int condition) {
        if(condition == 1) {
            return "smile1";
        }
        else if(condition == 2) {
            return "smile2";
        }
        else if(condition == 3) {
            return "smile3";
        }
        else if(condition == 4) {
            return "smile4";
        }
        else if(condition == 5) {
            return "smile5";
        }

        return "adamastour_logo";
    }

    public String getIcon() {
        return icon;
    }

    public String getAqi() {
        return aqi + "/5";
    }
}
