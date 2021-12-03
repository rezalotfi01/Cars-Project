package com.reza.carsproject.di.modules

import com.reza.carsproject.screens.detail.ui.DetailViewModel
import com.reza.carsproject.screens.maps.ui.MapsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        MapsViewModel(get())
    }

    viewModel {
        DetailViewModel(get())
    }

}