package com.example.locationtracker2.utils

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import java.nio.file.Path

typealias PathPoint=MutableList<Location>
typealias PathPoints=MutableList<PathPoint>

class LocationService: Service() {
    private var locationClient:FusedLocationProviderClient?=null
    private var locationClientProvider:LocationUpdateClient?=null
    private val TAG="LOCATION SERVICE"
    companion object{
        var pathPoints=MutableLiveData<PathPoints>()
        var tracking = MutableLiveData<Boolean>().apply {
            this.value=false
        }
    }

    private fun initializeData(){
        pathPoints.let{
            it.value= mutableListOf()
            it.value?.add(mutableListOf())
        }
        tracking.value=false
    }

    private fun addLocationToList(location:Location){
        location ?: return
        pathPoints?.value.apply{
            this?.last()?.add(location)
            pathPoints.postValue(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG,"location service created")
        initializeData()
        locationClientProvider= LocationUpdateClient()
        locationClient=locationClientProvider?.initializeLocationClient(this,locationCallback)
    }

    private val locationCallback= object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            addLocationToList(locationResult.lastLocation)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            val action = intent.action
            when(action){
                Constant.START_OR_RESUME_LOCATION_SERVICE -> {
                    startTracking()
                    locationClientProvider?.startLocationUpdates(this)
                    Log.i(TAG,"location service started")
                }
                Constant.STOP_LOCATION_SERVICE -> {
                    stopLocationService()
                }
                Constant.PAUSE_LOCATION_SERVICE->{
                    stopLocationService()
                    pathPoints.value?.add(mutableListOf())
                    Log.i(TAG,"location service paused")
                }
                else -> stopLocationService()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
       return null
    }

    private fun stopTracking(){
        tracking.value=false
    }

    private fun startTracking(){
        tracking.value=true
    }

    private fun stopLocationService(){
       locationClientProvider?.stopLocationUpdates()
        stopTracking()
        stopSelf()
    }
}