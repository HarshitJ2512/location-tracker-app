package com.example.locationtracker2.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng

class TrackerScreenViewModelFactory(private val destination:LatLng):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       return TrackerScreenViewModel(destination) as T
    }
}