package com.reza.carsproject.screens.maps.data

class MapsRepository(private val mapsApiService: MapsApiService) {

    fun getAllCars() = mapsApiService.getAllCars()
}