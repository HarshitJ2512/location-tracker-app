package com.example.locationtracker2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.locationtracker2.R
import com.example.locationtracker2.broadcastReceivers.GeofenceBroadcastReceiver
import com.example.locationtracker2.databinding.FragmentTrackerScreenBinding
import com.example.locationtracker2.utils.*
import com.example.locationtracker2.viewModels.TrackerScreenViewModel
import com.example.locationtracker2.viewModels.TrackerScreenViewModelFactory
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.security.Permission
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val req_id = "geo-xx-1"

/**
 * A simple [Fragment] subclass.
 * Use the [TrackerScreen.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrackerScreen : Fragment(),OnMapReadyCallback,TimerChangeListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val TAG="TRACKER SCREEN"
    private var userLocationMarker:Marker? = null
    private var destinationLocationMarker:Marker?=null
    private lateinit var geofencingHelper:GeofenceHelper
   private lateinit var binding:FragmentTrackerScreenBinding
   private lateinit var viewModel:TrackerScreenViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var client:GeofencingClient
    private lateinit var pendingIntent: PendingIntent
    private var stopWatch:StopWatch?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentTrackerScreenBinding.inflate(layoutInflater,container,false)
        val loc=arguments?.getParcelable("location") as LatLng?
        val viewModelFactory =TrackerScreenViewModelFactory(LatLng(loc!!.latitude,loc.longitude))
        viewModel=ViewModelProvider(this,viewModelFactory).get(TrackerScreenViewModel::class.java)
        val mapFragment=childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        geofencingHelper= GeofenceHelper()
        stopWatch = StopWatch(this)
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
      mMap=googleMap
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(viewModel.destination!!,12.0f))
        initializeGeofence()
        addActionButtonListeners()
        setLocationChangeObservers()
        addPolylinesToMap()
    }


    /** Geofencing **/
    private fun initializeGeofence(){
       addDestinationToMap(viewModel.destination?.latitude!!,viewModel.destination?.longitude!!)
        createGeofence(viewModel.destination?.latitude!!,viewModel.destination?.longitude!!)
    }


    @SuppressLint("MissingPermission")
    private fun createGeofence(latitude:Double, longitude:Double){
        val location = LatLng(latitude,longitude)
       val geofence= geofencingHelper.createGeofence(location, Geofence.GEOFENCE_TRANSITION_ENTER or
        Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL
        )
        val geofenceRequest=geofencingHelper.getGeofencingRequest(geofence)
        client=geofencingHelper.getGeofencingClient(requireContext())
        pendingIntent = geofencingHelper.getGeofencePendingIntent(requireContext())
        if(PermissionUtility.hasLocationPermissions(requireContext())){
            client.addGeofences(geofenceRequest,pendingIntent).run {
                addOnSuccessListener {
                    Log.i(TAG,"geofence added succesfully")
                }
                addOnFailureListener {
                    Log.i(TAG,"geofence addition failed - $it")
                }
            }
            addGeofenceTransitionObservers()
        }
    }

    private fun addGeofenceTransitionObservers(){
        GeofenceBroadcastReceiver.geofenceResult.observe(viewLifecycleOwner, Observer { result ->
            if(result!=null){
                Toast.makeText(requireContext(),"Destination Reached",Toast.LENGTH_LONG).show()
                pauseTrackingUser()
                client.removeGeofences(pendingIntent)
            }
        })
    }
    /** Geofencing **/

    /** Tracking actions **/
    private fun addActionButtonListeners(){
        binding.apply {
            actionBtn.setOnClickListener {
                if(!LocationService.tracking.value!!){
                    startTrackingUser()
                }
                else{
                  pauseTrackingUser()
                }
            }
        }
    }

    private fun startTrackingUser(){
     Intent(requireContext(),LocationService::class.java).also { intent->
         intent.action=Constant.START_OR_RESUME_LOCATION_SERVICE
         requireContext().startService(intent)
     }
        binding.actionBtn.text=requireContext().getString(R.string.stop_tracking)
        stopWatch?.startTimer()
    }

    private fun pauseTrackingUser(){
        stopWatch?.pauseTimer()
        if(LocationService.tracking.value!!){
            Intent(requireContext(),LocationService::class.java).also { intent->
                intent.action=Constant.PAUSE_LOCATION_SERVICE
                requireContext().startService(intent)
                binding.actionBtn.text=requireContext().getString(R.string.start_tracking)
            }
        }
    }
    /** Tracking actions **/

    /** Map actions **/
    private fun addDestinationToMap(latitude:Double,longitude:Double){
        val loc=LatLng(latitude,longitude)
        val markerOptions = MarkerOptions()
            .position(loc)
        destinationLocationMarker=mMap.addMarker(markerOptions)
        val circleOptions=CircleOptions()
            .fillColor(Color.argb(64,255,0,0))
            .strokeColor(Color.argb(255,255,0,0))
            .strokeWidth(4f)
            .radius(Constant.GEOFENCE_RADIUS*1.0)
            .center(loc)
        mMap.addCircle(circleOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,14f))
    }


    private fun setLocationChangeObservers(){
        LocationService.pathPoints.observe(viewLifecycleOwner, Observer {
           addPolylineToMap(it)
            if(it.isNotEmpty() && it.last().isNotEmpty()) updateMarkerOnTheMap(it?.last()?.last())
        })
    }

    private fun updateMarkerOnTheMap(loc:Location?) {
        loc ?: return
        val location= LatLng(loc.latitude,loc.longitude)
        if(userLocationMarker==null){
            val markerOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                .position(location)
                .anchor(0.5f,0.5f)
                .rotation(loc.bearing)
            userLocationMarker=mMap.addMarker(markerOptions)
        }
        else{
            userLocationMarker?.position=location
            userLocationMarker?.rotation = loc.bearing
        }
    }

    private fun addPolylineToMap(pathPoints:PathPoints){
        if(pathPoints.size>0 && pathPoints.last().size>1){
            val size=pathPoints.last().size
            val p1=pathPoints.last()[size-2]
            val p2=pathPoints.last()[size-1]
            val polylineOptions= PolylineOptions()
                .clickable(false)
                .color(Color.RED)
                .width(12f)
                .add(LatLng(p1.latitude,p1.longitude), LatLng(p2.latitude,p2.longitude))
            mMap.addPolyline(polylineOptions)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p2.latitude,p2.longitude),18f))
        }
    }
    private fun addPolylinesToMap(){
        val pathPoints = LocationService.pathPoints.value
        pathPoints ?: return
        for(list in pathPoints){
            for(i in list.indices){
                if(i-1>0){
                    val loc2=list[i]
                    val loc1=list[i-1]
                    val polylineOptions= PolylineOptions()
                        .clickable(false)
                        .color(Color.RED)
                        .width(12f)
                        .add(LatLng(loc1.latitude,loc1.longitude), LatLng(loc2.latitude,loc2.longitude))
                    mMap.addPolyline(polylineOptions)
                }
            }
        }
        val size=pathPoints.size
        if(pathPoints.isNotEmpty() && size>1 && pathPoints[size-2].isNotEmpty()){
            Log.i(TAG,"adding zoom on start")
            val lastLocation = LatLng(pathPoints[size-2].last().latitude,pathPoints[size-2].last().longitude)
            updateMarkerOnTheMap(pathPoints[size-2].last())
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude,lastLocation.longitude),18f))
        }
    }
    /** Map actions **/


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TrackerScreen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TrackerScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    /** Timer **/
    override fun onTimerChange(time: Long) {
        val res=getTimeString(time)
        binding.timer.text = res
    }

    private fun getTimeString(time:Long):String{
        var rem = time
        val msInHour=1000*60*60
        val hours = rem / msInHour
        rem-= (hours * msInHour)
        val msInMin=1000*60
        val min = rem / msInMin
        rem -= (min * msInMin)
        val sec = rem / 1000

        val hs:String=if(hours>9) hours.toString()
                     else "0$hours"
        val ms = if(min>9) min.toString()
        else "0$min"
        val ss= if(sec>9) sec.toString()
        else "0$sec"

        return getString(R.string.stop_watch_time,hs,ms,ss)
    }
    /** Timer **/



}