package com.example.locationtracker2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.locationtracker2.PersonView
import com.example.locationtracker2.R
import com.example.locationtracker2.databinding.FragmentHomeScreenBinding
import com.example.locationtracker2.utils.Constant
import com.example.locationtracker2.utils.PermissionUtility
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeScreen.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeScreen : Fragment(),OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val TAG="HOME SCREEN"
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentHomeScreenBinding
    private lateinit var alertDialog: AlertDialog
    private var location: LatLng?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentHomeScreenBinding.inflate(layoutInflater,container,false)
        createDialog()
        enableLocationPermission()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.animateCamera(CameraUpdateFactory.zoomBy(2.0f))
        val settings = mMap.uiSettings
        settings.isZoomControlsEnabled=true
        setMapListeners(googleMap)
        enableCurrentLocationOnMap()
        Log.i(TAG,"map ready")
    }
    /** Map Listeners **/
    private fun setMapListeners(googleMap: GoogleMap){
        googleMap.setOnMapClickListener {
//            val custom_marker = PersonView(requireContext(),null)
//            val icon_generator=IconGenerator(context)
//            icon_generator.setContentView(custom_marker)

//            val markerOptions = MarkerOptions()
//                .position(it)
//            googleMap.addMarker(markerOptions)
            location= LatLng(it.latitude,it.longitude)
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it,11.0f))
            showDialog()
        }
    }


    /** Map Listeners **/

    /** Action Dialog **/
    private fun createDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.dialog_message)
            .setPositiveButton(R.string.dialog_positive,
                DialogInterface.OnClickListener { _, _ ->
                    val bundle=Bundle()
                    bundle.putParcelable("location",location)
                    findNavController().navigate(R.id.action_homeScreen_to_trackerScreen,bundle)
                })
            .setNegativeButton(R.string.dialog_cancel,
                DialogInterface.OnClickListener { _, _ ->
                    mMap.clear()
                })
        alertDialog=builder.create()
    }

    private fun showDialog(){
        alertDialog.show()
    }
    /** Action Dialog **/


    /** Location Permissions **/
    private fun enableLocationPermission(){
        requestLocationPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Constant.FINE_LOCATION_CODE)
        requestLocationPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Constant.BACKGROUND_LOCATION_CODE)
    }

    private fun requestLocationPermissions(permission:String,requestCode: Int){
        if(PermissionUtility.hasLocationPermissions(requireContext())) return
        Log.i(TAG,"not have perm")
        if(requestCode== Constant.FINE_LOCATION_CODE){
            if(shouldShowRequestPermissionRationale(permission)){
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode)

            }
            else{
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode)
            }
        }
        else if(requestCode== Constant.BACKGROUND_LOCATION_CODE){
            if(shouldShowRequestPermissionRationale(permission)){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    requestCode
                )
            }
            else{
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    requestCode
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG,"permisson result called")
        if(requestCode==Constant.FINE_LOCATION_CODE){
            Log.i(TAG,"fine location granted $grantResults")
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(),"Fine location granted", Toast.LENGTH_LONG).show()
                enableCurrentLocationOnMap()
            }
            else{
                Toast.makeText(requireContext(),"Fine location denied", Toast.LENGTH_LONG).show()
            }
        }
        else{
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(),"Background location granted", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(requireContext(),"Background location denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun enableCurrentLocationOnMap(){
        if(!PermissionUtility.hasLocationPermissions(requireContext())) return
        mMap.isMyLocationEnabled=true
    }
    /** Location Permissions **/
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeScreen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}