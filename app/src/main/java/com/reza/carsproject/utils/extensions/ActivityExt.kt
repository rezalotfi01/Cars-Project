package com.reza.carsproject.utils.extensions

import android.app.Activity
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

private fun Activity.createToast(style: MotionToastStyle, message: String, title: String? = null){
    MotionToast.createToast(this,title,message,style,MotionToast.GRAVITY_BOTTOM,MotionToast.LONG_DURATION,null)
}

fun Activity.toastError(message: String, title: String? = null) = createToast(MotionToastStyle.ERROR,message, title)

fun Activity.toastInfo(message: String, title: String? = null) = createToast(MotionToastStyle.INFO,message, title)