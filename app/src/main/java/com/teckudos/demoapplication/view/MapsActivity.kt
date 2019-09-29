package com.teckudos.demoapplication.view

import android.Manifest.permission
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.teckudos.demoapplication.R
import com.teckudos.demoapplication.databinding.ActivityMapsBinding
import com.teckudos.demoapplication.network.PlacesApiFilter
import com.teckudos.demoapplication.viewmodel.MapsViewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    companion object {
        private val defaultLocation = LatLng(28.5355, 77.3910)
        private const val DEFAULT_ZOOM = 12f
        private const val KEY_LOCATION = "location"
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val MAX_PLACES = 5
        private val perms = permission.ACCESS_FINE_LOCATION
    }

    private lateinit var dataBinding: ActivityMapsBinding

    private val viewModel: MapsViewModel by lazy {
        ViewModelProviders.of(this).get(MapsViewModel::class.java)
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var mCameraPosition: CameraPosition
    private lateinit var mGeoDataClient: GeoDataClient
    private lateinit var mPlaceDetectionClient: PlaceDetectionClient
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLastKnownLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)!!
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)!!
        }

        init()
        setListeners()
        initObservers()
        getCurrentLocation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        googleMap?.let {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.cameraPosition)
            if (::mLastKnownLocation.isInitialized)
                outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
        }
        super.onSaveInstanceState(outState);
    }

    private fun init() {
        dataBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_maps
        )
        dataBinding.lifecycleOwner = this
        dataBinding.viewModel = viewModel

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setListeners() {
        dataBinding.cbHotel.setOnCheckedChangeListener { cbSchool, isChecked ->
            if(isChecked){
                viewModel.updateFilter(PlacesApiFilter.SHOW_HOTEL)
            }
        }
        dataBinding.cbSchool.setOnCheckedChangeListener { cbSchool, isChecked ->
            if(isChecked){
                viewModel.updateFilter(PlacesApiFilter.SHOW_SCHOOL)
            }
        }
        dataBinding.cbHospital.setOnCheckedChangeListener { cbSchool, isChecked ->
            if(isChecked){
                viewModel.updateFilter(PlacesApiFilter.SHOW_HOSPITAL)
            }
        }
    }

    private fun initObservers() {
        viewModel.places.observe(this, Observer { placesDTO ->
            placesDTO?.let {
                placesDTO.results.forEach {
//                    googleMap.clear()
                    googleMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                it.geometry.location.lat,
                                it.geometry.location.lng
                            )
                        )
                    ).title = it.name
                    it.geometry.location
                }
            }
        })
    }

    @AfterPermissionGranted(REQUEST_LOCATION_CODE)
    private fun getCurrentLocation() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            // TODO
        } else {
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.rational_message),
                REQUEST_LOCATION_CODE,
                perms
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            getCurrentLocation()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        with(googleMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM))
            addMarker(MarkerOptions().position(defaultLocation)).title = "Marker In Noida"
        }
        updateLocationUI()
        getDeviceLocation()
        viewModel.updateFilter(PlacesApiFilter.SHOW_ALL)
    }

    private fun updateLocationUI() {
        try {
            if (EasyPermissions.hasPermissions(this, perms)) {
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                googleMap.isMyLocationEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                getCurrentLocation()
            }
        } catch (e: SecurityException) {
            Timber.e("Exception: %s", e.message)
        }
    }

    private fun getDeviceLocation() {
        try {
            val locationResult = mFusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mLastKnownLocation = task.result!!
                    googleMap.clear()
                    googleMap.apply {
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(mLastKnownLocation.latitude, mLastKnownLocation.longitude),
                                DEFAULT_ZOOM
                            )
                        )
                        addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    mLastKnownLocation.latitude,
                                    mLastKnownLocation.longitude
                                )
                            )
                        ).title =
                            "Current Location"
                    }
                } else {
                    Timber.i("current location is null using default")
                    Timber.e("${task.exception}")
                    googleMap.clear()
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
                    )
                    googleMap.addMarker(MarkerOptions().position(defaultLocation)).title =
                        "Default Location"
                }
            }
        } catch (exception: SecurityException) {
            Timber.e("$exception")
        }
    }

    private fun getCurrentPlaces() {
        val placeResult = mPlaceDetectionClient.getCurrentPlace(null)
        placeResult.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val places = task.result
                var count = if (places?.count!! < MAX_PLACES) {
                    places?.count
                } else {
                    MAX_PLACES
                }

                val i = 0
                val mLikelyPlaceNames = emptyArray<String>()
                val mLikelyPlaceAddresses = emptyArray<String>()
                val mLikelyPlaceAttributions = emptyArray<String>()
                val mLikelyPlaceLatLngs = emptyArray<LatLng>()

                places.forEach {
                    mLikelyPlaceNames[i] = it.place.name as String
                    mLikelyPlaceAddresses[i] = it.place.address as String
                    mLikelyPlaceAttributions[i] = it.place.attributions as String
                    mLikelyPlaceLatLngs[i] = it.place.latLng

                    googleMap.addMarker(
                        MarkerOptions()
                            .title(mLikelyPlaceNames[i])
                            .position(mLikelyPlaceLatLngs[i])
                            .snippet("${mLikelyPlaceAddresses[i]}\n${mLikelyPlaceAttributions[i]}}")
                    )

                    /*googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            mLikelyPlaceLatLngs[i],
                            DEFAULT_ZOOM
                        )
                    )*/

                    i.plus(1)
                    if (i > (count - 1))
                        return@forEach
                }
                places.release();
            }

        }
    }
}

const val REQUEST_LOCATION_CODE = 101
const val DEFAULT_LOCATION = "28.6452,77.3554"
