package com.reza.carsproject.screens.maps.ui

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.android.gms.maps.model.MarkerOptions

import com.google.maps.android.clustering.ClusterManager

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor

import com.google.maps.android.clustering.view.DefaultClusterRenderer


internal class OwnIconRendered(
    context: Context?, map: GoogleMap?,
    clusterManager: ClusterManager<CarsMapItem>?
) :
    DefaultClusterRenderer<CarsMapItem>(context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: CarsMapItem, markerOptions: MarkerOptions) {
        markerOptions.icon(item.icon)
        markerOptions.snippet(item.snippet)
        markerOptions.title(item.title)
        super.onBeforeClusterItemRendered(item, markerOptions)
    }
}

class CarsMapItem(
    lat: Double,
    lng: Double,
    private val title: String,
    private val snippet: String,
    var carId:Int,
    val icon: BitmapDescriptor
) : ClusterItem {

    private val position: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String {
        return snippet
    }

}