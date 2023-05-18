package com.example.adamastour2;

import static com.example.adamastour2.MapActivity.FINE_LOCATION_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SuggestionsActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    String APP_ID = "3d6bd889ce3a72e1937ebe70f328d97a";
    String URL = "https://api.openweathermap.org/data/2.5/weather?";

    FusedLocationProviderClient fusedClient;
    Location currentLocation;
    private static final long MIN_TIME = 5000;
    private static final float MIN_DISTANCE = 1000;
    private static final int WEATHER_REQUEST_CODE = 1001;
    private static final String TAG = "WeatherActivity";


    String Location_Provider = LocationManager.GPS_PROVIDER;
    TextView cityName, weatherState, temperature;
    ImageView weatherIcon;


    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        drawerLayout = findViewById(R.id.drawer_layout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_suggestions);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                case R.id.bottom_map:
                    startActivity(new Intent(getApplicationContext(), MapActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                    finish();
                    return true;
                case R.id.bottom_suggestions:
                    return true;
                case R.id.bottom_wishlist:
                    startActivity(new Intent(getApplicationContext(), WishlistActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                    finish();
                    return true;
            }
            return false;
        });

        weatherState = findViewById(R.id.weatherCondition);
        temperature = findViewById(R.id.temperature);
        weatherIcon = findViewById(R.id.weathericon);
        cityName = findViewById(R.id.city);

        getWeatherForCurrentLocation(currentLocation);
    }

    //@Override
    //protected void onResume() {
    //    super.onResume();
    //    getWeatherForCurrentLocation();
    //}

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
        }
        Task<Location> task = fusedClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.d(TAG, currentLocation.toString());
                }
            }
        });
    }


    private void getWeatherForCurrentLocation(Location currentLocation) {
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationListener = new LocationListener() {
        //    @Override
        //    public void onLocationChanged(@NonNull Location location) {
        //        String lat = String.valueOf(location.getLatitude());
        //        String lng = String.valueOf(location.getLongitude());
//
        //        RequestParams params = new RequestParams();
        //        params.put("lat", lat);
        //        params.put("lng", lng);
        //        params.put("appid", APP_ID);
        //        network(params);
        //    }
        //};
//
        //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_REQUEST_CODE);
        //    return;
        //}
        //locationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, locationListener);

        String lat, lng;

        if(currentLocation != null) {
            lat = String.valueOf(currentLocation.getLatitude());
            lng = String.valueOf(currentLocation.getLongitude());
        } else {
            lat = "41.5518";
            lng = "-8.4229"; // coordenadas de braga
        }



        RequestParams params = new RequestParams();
        params.put("lat", lat);
        params.put("lng", lng);
        params.put("appid", APP_ID);
        network(params);

        URL = URL + "lat=" + lat + "&lon=" + lng + "&appid=" + APP_ID;

        Log.d(TAG, "URL: " + URL);
    }

    private void network (RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://api.openweathermap.org/data/2.5/weather?lat=41.5518&lon=-8.4229&appid=3d6bd889ce3a72e1937ebe70f328d97a",new JsonHttpResponseHandler() { //URL,params
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode, headers, response);
                Log.d(TAG, "Got data from Weather API");

                WeatherData weatherData = WeatherData.fromJson(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "Didn't get data from Weather API");
            }
        });
    }

    private void updateUI(WeatherData weatherData) {

        temperature.setText(weatherData.getTemperature());
        cityName.setText(weatherData.getCity());
        weatherState.setText(weatherData.getWeatherType());
        int resourceID = getResources().getIdentifier(weatherData.getIcon(), "drawable", getPackageName());
        weatherIcon.setImageResource(resourceID);
    }

    //@Override
    //protected void onPause() {
    //    super.onPause();
    //    if(locationManager != null) {
    //        locationManager.removeUpdates(locationListener);
    //    }
    //}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}