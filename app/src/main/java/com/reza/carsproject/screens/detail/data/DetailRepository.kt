package com.reza.carsproject.screens.detail.data

class DetailRepository(private val detailApiService: DetailApiService, private val rentApiService: RentApiService) {

    fun getCarDetail(carId: Int) = detailApiService.getCarDetail(carId)

    fun rentCar(carId: Int) = rentApiService.rentCar(RentRequest(carId))


}