package com.reza.carsproject.utils.extensions
import com.reza.carsproject.BuildConfig
import timber.log.Timber


/**
 * Used to determine whether the logging is enabled or not
 */
private var isLoggingEnabled: Boolean = BuildConfig.DEBUG


fun Throwable.logError() {
    if (isLoggingEnabled) {
        Timber.e(message.toString())
    }
}

fun logError(log: String) {
    if (isLoggingEnabled) {
        Timber.e(log)
    }
}

fun logDebug(log: String) {
    if (isLoggingEnabled) {
        Timber.e(log)
    }
}