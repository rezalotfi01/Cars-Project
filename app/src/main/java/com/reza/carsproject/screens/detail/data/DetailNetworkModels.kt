package com.reza.carsproject.screens.detail.data

data class CarDetail(
    val address: String,
    val carId: Int,
    val city: String,
    val damageDescription: String,
    val fuelLevel: Int,
    val hardwareId: String,
    val isActivatedByHardware: Boolean,
    val isClean: Boolean,
    val isDamaged: Boolean,
    val lat: Double,
    val licencePlate: String,
    val locationId: Int,
    val lon: Double,
    val pricingParking: String,
    val pricingTime: String,
    val reservationState: Int,
    val title: String? = "",
    val vehicleStateId: Int,
    val vehicleTypeId: Int,
    val vehicleTypeImageUrl: String,
    val zipCode: String
)

data class RentDetail(
    val carId: Int,
    val cost: Int,
    val damageDescription: String,
    val drivenDistance: Int,
    val endTime: Int,
    val fuelCardPin: String,
    val isParkModeEnabled: Boolean,
    val licencePlate: String,
    val reservationId: Int,
    val startAddress: String,
    val startTime: Int,
    val userId: Int
)

data class RentRequest(
    val carId: Int
)


