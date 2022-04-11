package com.example.locationtracker2.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.locationtracker2.utils.GeofenceResult
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver:BroadcastReceiver() {
    private val TAG="GEOFENCE BR"
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.i(TAG,"geofence triggered")
        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val result = GeofenceResult(geofenceTransition,triggeringGeofences)
            updateResult(result)

        }
    }

    private fun updateResult(result:GeofenceResult?){
        geofenceResult.value= result
    }

    companion object{
        var geofenceResult=MutableLiveData<GeofenceResult?>()
    }
}