package com.example.locationtracker2.viewModels

import androidx.lifecycle.ViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng

class TrackerScreenViewModel(location:LatLng):ViewModel() {
    var destination:LatLng?=null
    val geofenceList:MutableList<Geofence>  = mutableListOf()
    val activityState=0
    init {
        destination= LatLng(location.latitude,location.longitude)
    }

    fun addGeofence(geofence: Geofence){
        geofenceList.add(geofence)
    }
}