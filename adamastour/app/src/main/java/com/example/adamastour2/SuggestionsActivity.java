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
import android.content.pm.ApplicationInfo;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SuggestionsActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    String APP_ID;
    String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?";
    String POLLUTION_URL = "https://api.openweathermap.org/data/2.5/air_pollution?";

    FusedLocationProviderClient fusedClient;
    Location currentLocation;
    private static final String TAG = "WeatherActivity";

    TextView cityName, weatherState, temperature, aqi, drawerEmail;
    ImageView weatherIcon, pollutionIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            // Handle exception if package or metadata not found
        }

        Bundle metaData = applicationInfo.metaData;
        APP_ID = metaData.getString("WEATHER_POLLUTION_KEY");

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerEmail = navigationView.getHeaderView(0).findViewById(R.id.drawerEmail);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            drawerEmail.setText(email);
            //String name = user.getDisplayName();
            //Uri photoUrl = user.getPhotoUrl();
        }
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    return true;

            }
            return false;
        });

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
        aqi = findViewById(R.id.aqi);
        pollutionIcon = findViewById(R.id.pollutionicon);

    }

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
                    getDataForCurrentLocation(currentLocation);

                }
            }
        });
    }


    private void getDataForCurrentLocation(Location currentLocation) {
        String lat, lng;

        if(currentLocation != null) {
            lat = String.valueOf(currentLocation.getLatitude());
            lng = String.valueOf(currentLocation.getLongitude());
        } else {
            lat = "41.5518";
            lng = "-8.4229"; // coordenadas de braga - default
        }

        //RequestParams params = new RequestParams();
        //params.put("lat", lat);
        //params.put("lng", lng);
        //params.put("appid", APP_ID);
        //network(params);

        WEATHER_URL = WEATHER_URL + "lat=" + lat + "&lon=" + lng + "&appid=" + APP_ID;
        POLLUTION_URL = POLLUTION_URL + "lat=" + lat + "&lon=" + lng + "&appid=" + APP_ID;

        Log.d(TAG, "URL: " + WEATHER_URL);
        Log.d(TAG, "URL: " + POLLUTION_URL);
        network();
    }

    private void network () {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL,new JsonHttpResponseHandler() {
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

        client.get(POLLUTION_URL,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode, headers, response);
                Log.d(TAG, "Got data from Pollution API");

                PollutionData pollutionData = PollutionData.fromJson(response);
                updateUIPol(pollutionData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "Didn't get data from Pollution API");
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

    private void updateUIPol(PollutionData pollutionData) {

        aqi.setText(pollutionData.getAqi());
        int resourceID = getResources().getIdentifier(pollutionData.getIcon(), "drawable", getPackageName());
        pollutionIcon.setImageResource(resourceID);
    }

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