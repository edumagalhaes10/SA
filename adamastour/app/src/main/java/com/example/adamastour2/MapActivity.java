package com.example.adamastour2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, NavigationView.OnNavigationItemSelectedListener {

    // layout variables
    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    FrameLayout map;
    ImageView gps;

    // maps and location service variables
    GoogleMap gMap;
    FusedLocationProviderClient fusedClient;
    Location currentLocation;
    Marker marker;
    private static final float DEFAULT_ZOOM = 15f;

    // geofencing variables
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private float GEOFENCE_RADIUS = 200;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";


    static final int FINE_LOCATION_REQUEST_CODE = 101;
    private static final int BACKGROUND_LOCATION_REQUEST_CODE = 102;
    private static final int NOTIFICATION_REQUEST_CODE = 103;
    private static final String TAG = "MapActivity";
    private DatabaseReference database;
    PlacesClient placesClient;

    TextView placeName, placeAddress, placeRating, drawerEmail;
    ImageView placeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // to notify user of geofences (points of interest)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST_CODE);
        }


        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerEmail = navigationView.getHeaderView(0).findViewById(R.id.drawerEmail);
        MenuItem drawerPoints = navigationView.getMenu().findItem(R.id.nav_points);
        //drawerPoints = navigationView.findViewById(R.id.nav_points);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String sanitizedEmail = email.replaceAll("[.#$\\[\\]]", "");
            updateGamificationPoints(sanitizedEmail, new OnPointsUpdatedListener() {
                @Override
                public void onPointsUpdated(int points) {
                    // Handle the updated points value here
                    // You can use the 'points' value returned from the callback
                    String pontos = points + " Points";
                    drawerPoints.setTitle(pontos);
                    Log.d("firebase", "Updated points: " + points);
                }
            });
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
        bottomNavigationView.setSelectedItemId(R.id.bottom_map);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                case R.id.bottom_map:
                    return true;
                case R.id.bottom_suggestions:
                    startActivity(new Intent(getApplicationContext(), SuggestionsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                    finish();
                    return true;
                case R.id.bottom_wishlist:
                    startActivity(new Intent(getApplicationContext(), WishlistActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                    finish();
                    return true;
            }
            return false;
        });

        map = findViewById(R.id.map);
        gps = findViewById(R.id.icon_gps);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        // geofencing
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);


        // for searchbar autocomplete
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                if (latLng == null) {
                    Toast.makeText(MapActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (marker != null) {
                    marker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(place.getName());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(217.0f)); // @color/myblue - 217.0f; @color/mygold - 46.0f
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
                gMap.animateCamera(cameraUpdate);
                marker = gMap.addMarker(markerOptions);
            }

        });


        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    //private void updateUIInfo(Place place) {
//
    //    placeName.setText(place.getName());
    //    placeAddress.setText(place.getAddress());
    //    placeRating.setText(place.getIconUrl());
    //    place.
    //    int resourceID = getResources().getIdentifier(weatherData.getIcon(), "drawable", getPackageName());
    //    weatherIcon.setImageResource(resourceID);
    //}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout nomeLayout = dialog.findViewById(R.id.layoutNome);
        LinearLayout moradaLayout = dialog.findViewById(R.id.layoutMorada);
        LinearLayout outrosLayout = dialog.findViewById(R.id.layoutOutros);

        // TODO : PREENCHER COM API PLACES - info sítios


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
            //return;
        }
        Task<Location> task = fusedClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    //Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapActivity.this);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Current Location");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        googleMap.addMarker(markerOptions);
        googleMap.setPadding(0, 0, 0, 200); // to make zoom controls visible
        gMap.getUiSettings().setZoomControlsEnabled(true);
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    return;
        //}
        gMap.setMyLocationEnabled(true);
        //gMap.getUiSettings().setMyLocationButtonEnabled(true);

        //gMap.setOnMapLongClickListener(this);

        database = FirebaseDatabase.getInstance().getReference("Points of Interest");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String name = childSnapshot.child("name").getValue(String.class);
                    String city = childSnapshot.child("city").getValue(String.class);
                    String lat = childSnapshot.child("lat").getValue(String.class);
                    String lng = childSnapshot.child("long").getValue(String.class);

                    String place = name + "," + city;

                    LatLng newgeo = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    tryAddingGeofence(newgeo);
                    Log.d(TAG, name);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Unable to load geofences", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
                //gMap.setMyLocationEnabled(true);
            }
        }
        if (requestCode == BACKGROUND_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can add geofences", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Background location access is necessary for geofences to trigger!", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You will get notified of geofences", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

        if (Build.VERSION.SDK_INT >= 29) {
            // We need background permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                tryAddingGeofence(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_REQUEST_CODE);
                } else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_REQUEST_CODE);
            }

        } else {
            tryAddingGeofence(latLng);
        }
    }

    @SuppressLint("NewApi")
    private void tryAddingGeofence(LatLng latLng) {
        //gMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);
    }


    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius) {
        //TODO mudar id para o nome dos monumentos
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

         // list.forEach(coordinate -> tryAddingGeofence(coordinate))
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Geofence added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }

    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(46.0f));
        gMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 212, 175, 55)); //myblue: R-35|G-57|B-93 ;  mygold: R-212|G-175|B-55
        circleOptions.fillColor(Color.argb(64, 212, 175, 55));
        circleOptions.strokeWidth(4);
        gMap.addCircle(circleOptions);
    }

    public void updateGamificationPoints(String sanitizedEmail, OnPointsUpdatedListener listener) {

        FirebaseDatabase.getInstance().getReference("gamification_points").child(sanitizedEmail).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Points", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    Log.d("points", "oi");
                    if (dataSnapshot.exists()) {
                        Log.d("points", "oi");
                        DataSnapshot childSnapshot = dataSnapshot.child("points");
                        if (childSnapshot.exists()) {
                            int points = childSnapshot.getValue(Integer.class);
                            listener.onPointsUpdated(points);
                        } else {
                            Log.d("Points", "Child does not exist");
                        }
                    } else {
                        Log.d("Points", "Error updating user points");
                    }

                }
            }
        });
    }

    public interface OnPointsUpdatedListener {
        void onPointsUpdated(int points);
    }
}



