package com.reza.carsproject.screens.maps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.reza.carsproject.R
import com.reza.carsproject.databinding.FragmentMapsBinding
import com.reza.carsproject.utils.extensions.toastError
import com.reza.carsproject.utils.permission.runWithPermissions
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapsFragment : Fragment(R.layout.fragment_maps) {

    private val binding: FragmentMapsBinding by viewBinding()
    private val viewModel: MapsViewModel by viewModel()

    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<CarsMapItem>

    private var isMapInitialized = false
    var latestClickedCarId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservables()
        checkInternetAndInitMap()
        if (isMapInitialized.not()){
            initOnBackPressedCallback()
        }
    }


    private fun checkInternetAndInitMap(){
        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            requireActivity(),
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { 
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        if (hasActiveConnection && isMapInitialized.not())
                            initMap()
                    }
                }

                cancelable = false 
                noInternetConnectionTitle = "No Internet" 
                noInternetConnectionMessage = "Check your Internet connection and try again."

                showInternetOnButtons = true
                pleaseTurnOnText = "Please turn on" 
                wifiOnButtonText = "Wifi" 
                mobileDataOnButtonText = "Mobile data"

                onAirplaneModeTitle = "No Internet" 
                onAirplaneModeMessage = "You have turned on the airplane mode." 
                pleaseTurnOffText = "Please turn off" 
                airplaneModeOffButtonText = "Airplane mode" 
                showAirplaneModeOffButtons = true 
            }
        }.build()
    }

    /**
     * If we need to do something with views, should do it here
     */
    private fun initViews() {

    }

    /**
     * Initialize our livedata observables from viewModel
     */
    private fun initObservables() {
        viewModel.carsListLiveData.observe(viewLifecycleOwner) { carsList ->
            val mapItemsList = mutableListOf<CarsMapItem>()
            clusterManager.clearItems()
            carsList.forEach {
                clusterManager.addItem(
                    CarsMapItem(
                        it.lat,
                        it.lon,
                        if (it.title.isNullOrEmpty()) "No Name" else it.title,
                        it.address,
                        it.carId,
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_car)
                    )
                        .also { mapItem -> mapItemsList.add(mapItem) }
                )
            }
            clusterManager.setRenderer(
                OwnIconRendered(
                    requireContext(),
                    googleMap,
                    clusterManager
                )
            )


            setZoomToShowAllMarkers(mapItemsList)
        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            toastError(it, getString(R.string.error_title))
        }
    }


    /**
     * Initialize Map and cluster manager and then call to fetch Car items
     */
    private fun initMap() {
        val callback = OnMapReadyCallback { googleMap ->
            this.googleMap = googleMap
            clusterManager = ClusterManager(requireActivity(), this.googleMap)
            
            onMapReady()
            viewModel.getAllCars()

            isMapInitialized = true
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add listeners
     * If Google Play services is not installed on the device, the user will be prompted to
     * install it inside the SupportMapFragment. This method will only be triggered once the
     * user has installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    private fun onMapReady() {

        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)

        latestClickedCarId = null
        clusterManager.setOnClusterItemClickListener { item ->
            if (item.carId == latestClickedCarId){
                latestClickedCarId = null
                goToDetailFragment(item.carId)
            }else{
                latestClickedCarId = item.carId
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(item.position))

                // Hide all other Markers
                clusterManager.markerCollection.markers.forEach { marker ->
                    if (marker.position != item.position || marker.title != item.title){
                        marker.isVisible = false
                    }
                }
            }

            return@setOnClusterItemClickListener false
        }

        clusterManager.setOnClusterItemInfoWindowClickListener {
            latestClickedCarId = null
            goToDetailFragment(it.carId)
        }

        googleMap.setOnInfoWindowCloseListener {
            clusterManager.markerCollection.showAll()
            clusterManager.clusterMarkerCollection.showAll()
        }

        googleMap.setOnMapClickListener {
            latestClickedCarId = null
        }

        clusterManager.setOnClusterClickListener {
            openMapCluster(it)
            return@setOnClusterClickListener true
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.setAllGesturesEnabled(true)

        runWithPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            initMyLocationButton()
        }

    }

    /**
     * This is for when user clicks on back button and expect to deselect clicked map item/pin
     */
    private fun initOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (latestClickedCarId != null)
                    lifecycleScope.launch {
                        clusterManager.markerCollection.markers.forEach {
                            it.hideInfoWindow()
                        }
                        clusterManager.markerCollection.showAll()
                        clusterManager.clusterMarkerCollection.showAll()
                        latestClickedCarId = null
                    }
                else {
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, callback)
    }



    private fun setZoomToShowAllMarkers(mapItemsList: List<CarsMapItem>) {
        if (mapItemsList.isEmpty() || this::googleMap.isInitialized.not())
            return

        val builder = LatLngBounds.Builder()
        for (m in mapItemsList) {
            builder.include(m.position)
        }
        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()

        // Zoom and animate the google map to show all markers
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding))
    }

    /**
     * This method is for zoom and deCluster clustered map pins
     */
    private fun openMapCluster(cluster: Cluster<CarsMapItem>) {
        val builder = LatLngBounds.builder()
        for (item in cluster.items) {
            builder.include(item.position)
        }
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }


    /**
     * Configuring/Customizing location button of Google map view
     */
    private fun initMyLocationButton(){
        val locationButton= (binding.map.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp=locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom

        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)

        rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
        rlp.addRule(RelativeLayout.ALIGN_END, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        rlp.setMargins(20, 0, 0, 140)
        rlp.marginStart = 20
    }


    /**
     * This method used for Opening Detail Fragment
     * Because Navigation component is a bit delayed some times and for smoother experience,
     * I do it inside the lifecycleScope coroutine
     */
    private fun goToDetailFragment(carId: Int) {
        lifecycleScope.launch {
            clusterManager.markerCollection.markers.forEach {
                it.hideInfoWindow()
            }.apply {
                findNavController().navigate(
                    MapsFragmentDirections.actionMapsFragmentToDetailFragment(carId)
                )
            }
        }
    }
}