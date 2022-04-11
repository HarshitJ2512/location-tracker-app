package com.example.locationtracker2.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaBrowserCompat
import androidx.core.view.ContentInfoCompat
import com.example.locationtracker2.broadcastReceivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class GeofenceHelper {
    private val request_key="3022"
    private val pi_request_code=543
    fun getGeofencingClient(context: Context):GeofencingClient{
       return LocationServices.getGeofencingClient(context)
    }

    fun getGeofencingRequest(geoFence:Geofence):GeofencingRequest{
        val request = GeofencingRequest.Builder()
            .apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geoFence)
            }
            .build()
        return request
    }

    fun createGeofence(location:LatLng,transitions:Int):Geofence{
        return Geofence.Builder()
            .setRequestId(request_key)
            .setCircularRegion(
                location.latitude,
                location.longitude,
                Constant.GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitions)
            .setLoiteringDelay(3000)
            .build()
    }

    fun getGeofencePendingIntent(context: Context):PendingIntent{
        val intent = Intent(context,GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context,pi_request_code,intent,PendingIntent.FLAG_MUTABLE)
    }
}