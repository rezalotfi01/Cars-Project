package com.reza.carsproject.screens.maps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.birjuvachhani.locus.Locus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.reza.carsproject.R
import com.reza.carsproject.databinding.FragmentMapsBinding
import com.reza.carsproject.utils.extensions.toastError
import com.reza.carsproject.utils.extensions.toastInfo
import com.reza.carsproject.utils.permission.runWithPermissions
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapsFragment : Fragment(R.layout.fragment_maps) {

    private val binding: FragmentMapsBinding by viewBinding()
    private val viewModel: MapsViewModel by viewModel()

    private var googleMap: GoogleMap? = null
    private val mapMarkersList = mutableListOf<Marker>()

    private var myLiveLocation = LatLng(-1.0, -1.0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservables()

        initMap()
    }

    private fun initViews() {
        binding.fabLocation.setOnClickListener {
            if (myLiveLocation.latitude >= 0 && myLiveLocation.longitude >= 0){
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(myLiveLocation))
            }else{
                toastInfo(getString(R.string.finding_location))
            }
        }
    }

    private fun initObservables() {
        viewModel.carsListLiveData.observe(viewLifecycleOwner) { carsList ->
            mapMarkersList.clear()
            carsList.forEach {
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat, it.lon))
                        .title(it.title)
                        .snippet(it.address)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_car))
                )?.also { marker ->
                    mapMarkersList.add(marker)
                }
            }

            setZoomToShowAllMarkers()
        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            toastError(it, getString(R.string.error_title))
        }
    }


    private fun initMap() {
        val callback = OnMapReadyCallback { googleMap ->
            this.googleMap = googleMap
            onMapReady()
            viewModel.getAllCars()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to
     * install it inside the SupportMapFragment. This method will only be triggered once the
     * user has installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    private fun onMapReady() {

        googleMap?.setOnCameraMoveStartedListener {

        }
        googleMap?.setOnCameraIdleListener {

        }


        googleMap?.setOnMarkerClickListener { marker ->

            if (marker.isInfoWindowShown.not()) {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                marker.showInfoWindow()
            } else {
                marker.hideInfoWindow()
                goToDetailFragment()
            }
            return@setOnMarkerClickListener true
        }

        googleMap?.setOnInfoWindowClickListener { marker ->
            marker.hideInfoWindow()
            goToDetailFragment()
        }


        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        runWithPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) {
            googleMap?.isMyLocationEnabled = true
            Locus.startLocationUpdates(this@MapsFragment) { result ->

                result.location?.let {
                    myLiveLocation = LatLng(it.latitude, it.longitude)
                }
                result.error?.let {
                    toastError(getString(R.string.error_in_location) + it.localizedMessage
                        , getString(R.string.error_title))
                }
            }

        }

    }

    private fun setZoomToShowAllMarkers() {
        val builder = LatLngBounds.Builder()
        for (m in mapMarkersList) {
            builder.include(m.position)
        }
        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()

        // Zoom and animate the google map to show all markers
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap?.animateCamera(cu)
    }


    private fun goToDetailFragment() {
        toastInfo("going to second fragment")
    }
}