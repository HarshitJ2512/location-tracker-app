package com.example.locationtracker2.utils

import com.google.android.gms.location.Geofence

data class GeofenceResult(
    val geoFenceEvent:Int,
    val triggeringGeofences:List<Geofence>
) {
}