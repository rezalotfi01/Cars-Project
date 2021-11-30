package com.reza.carsproject

import androidx.multidex.MultiDexApplication
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.facebook.stetho.Stetho
import com.reza.carsproject.di.appComponents
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initDI()

        initStetho()
        initImageLoader()
        initLogTimber()
    }

    private fun initDI() {
        startKoin{
            androidLogger()
            androidContext(this@App)
            modules(appComponents)
        }
    }

    private fun initStetho() {
        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }

    private fun initImageLoader() {
        val imageLoader = ImageLoader.Builder(applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(applicationContext))
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }

    private fun initLogTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }


}