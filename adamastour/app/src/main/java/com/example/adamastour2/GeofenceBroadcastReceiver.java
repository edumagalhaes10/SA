package com.example.adamastour2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

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
                Toast.makeText(context, "Geofence entered", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence entered :)","You are currently near " + geofencingEvent.getTriggeringGeofences().get(0).getRequestId(),MapActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "On a Geofence", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("On a Geofence :)","You are currently near a Point of Interest!",MapActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Exited Geofence", Toast.LENGTH_SHORT).show();
                //notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT","",MapActivity.class);
                break;
        }
    }
}