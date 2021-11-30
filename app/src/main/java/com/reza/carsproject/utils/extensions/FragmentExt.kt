package com.reza.carsproject.utils.extensions

import androidx.fragment.app.Fragment

fun Fragment.toastError(message: String, title: String? = null) = requireActivity().toastError(message, title)

fun Fragment.toastInfo(message: String, title: String? = null) = requireActivity().toastInfo(message, title)