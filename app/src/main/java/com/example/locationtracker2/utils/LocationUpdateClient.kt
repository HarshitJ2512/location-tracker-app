package com.example.locationtracker2.utils


import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

class LocationUpdateClient {
    private var fusedLocationClient:FusedLocationProviderClient?=null
    private var locationCallback:LocationCallback?=null
    private var locationRequest:LocationRequest?=null
    fun initializeLocationClient(context:Context,lc: LocationCallback):FusedLocationProviderClient?{
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(context)
        locationRequest=createLocationRequest()
        locationCallback=lc
        return fusedLocationClient
    }
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context){
        Log.i("from location client","starting updates")
        if(PermissionUtility.hasLocationPermissions(context)){
            fusedLocationClient?.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
        else{
            Log.i("from location client","You dont have permissons")
        }
    }

    fun stopLocationUpdates(){
        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
    }

    private fun createLocationRequest():LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = Constant.LOCATION_UPDATE_INTERVAL
            fastestInterval = Constant.LOCATION_UPDATE_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }
}