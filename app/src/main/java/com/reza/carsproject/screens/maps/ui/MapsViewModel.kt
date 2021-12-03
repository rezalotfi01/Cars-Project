package com.reza.carsproject.screens.maps.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reza.carsproject.screens.maps.data.Car
import com.reza.carsproject.screens.maps.data.MapsRepository
import com.reza.carsproject.utils.reactive.LiveEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsViewModel(private val repository: MapsRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    /**
     * This Live data is from LiveEvent type and will be called only when value sets
     * other states handled
     */
    private val _errorLiveData = LiveEvent<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData


    private val _carsListLiveData = LiveEvent<List<Car>>()
    val carsListLiveData: LiveData<List<Car>> get() = _carsListLiveData

    fun getAllCars() {
      compositeDisposable += repository.getAllCars()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    _carsListLiveData.value = it
                },
                onError = {
                    _errorLiveData.value = it.localizedMessage
                }
            )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}