package com.example.adamastour2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeoBroadcastReceiver";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Geofence triggered ...", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }


        //Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                for (Geofence g: geofenceList) {
                    String name = g.getRequestId();
                    if (name.contains("_visited")) {
                        Toast.makeText(context, "Geofence visited", Toast.LENGTH_SHORT).show();
                        LocalDate date = LocalDate.now();

                        String firebase_id = name+"_"+date.toString();
                        Toast.makeText(context, firebase_id, Toast.LENGTH_LONG).show();

                        updatePlacesVisited(firebase_id);
                    } else {
                        Toast.makeText(context, "Geofence entered", Toast.LENGTH_SHORT).show();
                        notificationHelper.sendHighPriorityNotification("Geofence entered :)", "You are currently near " + name, MapActivity.class);
                    }
                }
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                //Toast.makeText(context, "On a Geofence", Toast.LENGTH_SHORT).show();
                //notificationHelper.sendHighPriorityNotification("On a Geofence :)","You are currently near a Point of Interest!",MapActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Exited Geofence", Toast.LENGTH_SHORT).show();
                //notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT","",MapActivity.class);
                break;
        }
    }



    public void updatePlacesVisited(String firebase_id) {
        FirebaseDatabase.getInstance().getReference("placesVisitedCount").child(firebase_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        DataSnapshot childSnapshot = dataSnapshot.child("visits_count");
                        if (childSnapshot.exists()) {
                            Map<String, Object> postValues = new HashMap<>();
                            postValues.put("visits_count", childSnapshot.getValue(Integer.class)+1);
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/placesVisitedCount/" + firebase_id, postValues);
                            FirebaseDatabase.getInstance().getReference().updateChildren((childUpdates));
                            //  Log.d("firebase", "Value: " + value);
                            Log.d("firebase", "Hello, updated user points");

                        } else {
                            Log.d("firebase", "Child does not exist");
                        }
                        // Data for the given user ID exists
                        // You can access the data using dataSnapshot.getValue() or iterate through its children
                    } else {
                        FirebaseDatabase.getInstance().getReference("placesVisitedCount").child(firebase_id).child("visits_count").setValue(1);
                        Log.d("firebase", "Created Entry in database");
                    }

                }
            }
        });
    }


}