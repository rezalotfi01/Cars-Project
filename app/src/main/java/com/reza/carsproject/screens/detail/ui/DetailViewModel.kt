package com.reza.carsproject.screens.detail.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.reza.carsproject.screens.detail.data.CarDetail
import com.reza.carsproject.screens.detail.data.DetailRepository
import com.reza.carsproject.screens.detail.data.RentDetail
import com.reza.carsproject.utils.reactive.LiveEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

class DetailViewModel(private val repository: DetailRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    /**
     * This Live data is from LiveEvent type and will be called only when value sets
     * other states handled
     */
    private val _errorLiveData = LiveEvent<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData

    private val _carDetailLiveData = LiveEvent<CarDetail>()
    val carDetailLiveData: LiveData<CarDetail>get() = _carDetailLiveData

    private val _carDetailHashmapLiveData = LiveEvent<LinkedHashMap<String,String>>()
    val carDetailHashmapLiveData: LiveData<LinkedHashMap<String,String>> get() = _carDetailHashmapLiveData

    private val _rentDetail = LiveEvent<RentDetail>()
    val rentDetail: LiveData<RentDetail> get() = _rentDetail

    fun getCarDetail(carId: Int){
      compositeDisposable += repository.getCarDetail(carId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    _carDetailLiveData.value = it
                    _carDetailHashmapLiveData.value = carDetailToMap(it)
                },
                onError = {
                    _errorLiveData.value = it.localizedMessage
                }
            )
    }

    fun rentCar(carId: Int){
        compositeDisposable += repository.rentCar(carId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    _rentDetail.value = it
                },
                onError = {
                    _errorLiveData.value = it.localizedMessage
                }
            )
    }

    private fun carDetailToMap(detail: CarDetail): LinkedHashMap<String,String> = linkedMapOf(
        "Title" to (if (detail.title.isNullOrEmpty()) "No Name" else detail.title),
        "Car ID" to detail.carId.toString(),
        "City" to detail.city,
        "Damage Description" to detail.damageDescription,
        "Fuel Level" to detail.fuelLevel.toString(),
        "hardware Id" to detail.hardwareId.toString(),
        "is Activated By Hardware" to detail.isActivatedByHardware.toString(),
        "is Clean" to detail.isClean.toString(),
        "is Damaged" to detail.isDamaged.toString(),
        "latitude" to detail.lat.toString().toString(),
        "longitude" to detail.lon.toString().toString(),
        "licence Plate" to detail.licencePlate.toString(),
        "location Id" to detail.locationId.toString().toString(),
        "pricing Parking" to detail.pricingParking.toString(),
        "pricing Time" to detail.pricingTime.toString(),
        "reservation State" to detail.reservationState.toString(),
        "vehicle State Id" to  detail.vehicleStateId.toString(),
        "vehicle Type Id" to detail.vehicleTypeId.toString(),
        "zipCode" to detail.zipCode.toString()
    )

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}