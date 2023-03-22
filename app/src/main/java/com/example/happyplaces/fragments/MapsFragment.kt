package com.example.happyplaces.activites

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.happyplaces.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.*

class MapsFragment(val defaultLatLng: LatLng?) : Fragment() {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var marker: Marker?= null
    private var geocoder: Geocoder?= null
    var map: GoogleMap? = null
    var callbacks: LoadingCallbacks?= null
    var myView:View?=null
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        if(defaultLatLng!=null){
            addMarkerOnMap(defaultLatLng)
        }
        requestLocationPermission()
    }
    private var locationServicesDisabled = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_maps, container, false)
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireContext(), Locale.ENGLISH)
    }

    override fun onResume() {
        super.onResume()
        if(locationServicesDisabled) {
            requestLocationPermission()
        }
    }

    fun getCurrentAddress():Address?{
        var address:Address?= null
        try{
            address = geocoder?.let {geo->
                val addresses = geo.getFromLocation(marker!!.position.latitude,marker!!.position.longitude,1)
                if(!addresses.isNullOrEmpty()){
                    return@let addresses.get(0)
                }
                return@let null
            }
        }catch (e: IOException){
            Toast.makeText(requireContext(),"Failed to fetch user address", Toast.LENGTH_SHORT).show()
        }
        return address
    }
    private fun addMarkerOnMap(newLatLng: LatLng?) {
        if(newLatLng==null)return
        map?.apply {
            val markerOptions = MarkerOptions().apply {
                this.position(newLatLng)
                draggable(true)
                setMarkerTitle(newLatLng)
            }
            marker = addMarker(markerOptions)
            map!!.setOnCameraMoveStartedListener {
                callbacks?.onLoadingStart()
            }
            map!!.setOnCameraMoveListener {
                marker!!.position = map!!.cameraPosition.target
            }
            map!!.setOnCameraIdleListener {
                setMarkerTitle(marker!!.position)
                callbacks?.onLoadingEnd()
            }
            map!!.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        marker!!.position,
                        18f
                    )
                )
            )
        }


    }

    private fun requestLocationPermission() {
        Dexter.withContext(requireContext())
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(permission: MultiplePermissionsReport?) {
                    if (permission?.grantedPermissionResponses?.isEmpty() == true) {
                        Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                            if(defaultLatLng==null && fusedLocationClient!=null) {
                               getCurrentLocation()
                            }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setCancelable(false)
                    dialog.setTitle("Permission!")
                    dialog.setMessage("Location permission is required to get your current location")
                    dialog.setNegativeButton(
                        "Cancel"
                    ) { myDialog, _ -> myDialog?.dismiss() }
                    dialog.show()
                }

            })
            .check()
    }

    private fun setMarkerTitle(newLatLng:LatLng){
        try {
            marker?.let {
                callbacks?.onLoadingStart()
                it.title = geocoder?.let {geo->
                    val addresses = geo.getFromLocation(newLatLng.latitude,newLatLng.longitude,1)
                    if(!addresses.isNullOrEmpty()){
                        return@let addresses.get(0).getAddressLine(0)
                    }
                    return@let null
                }
                it.showInfoWindow()
                callbacks?.onLoadingStart()
            }
        }catch (e:java.lang.Exception){ }
    }

    fun  setLoadingCallbacksListener(listener: LoadingCallbacks) {
        callbacks = listener
    }
    private fun isLocationEnabled():Boolean{
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    private fun getCurrentLocation(){
        if(!isLocationEnabled()){
            locationServicesDisabled = true
            Snackbar.make(myView!!,"Location is disabled",Snackbar.LENGTH_SHORT).setAction("Enable"){
                val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(locationSettingsIntent)
            }.show()
        }else {
            locationServicesDisabled = false
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callbacks?.onLoadingStart()
                fusedLocationClient!!.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnCompleteListener {
                        addMarkerOnMap(LatLng(it.result.latitude, it.result.longitude))
                        callbacks?.onLoadingEnd()
                    }
            }
        }
    }
}


interface  LoadingCallbacks{
    fun onLoadingStart();
    fun onLoadingEnd();
}