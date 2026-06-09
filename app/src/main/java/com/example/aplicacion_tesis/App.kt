package com.example.aplicacion_tesis

import android.app.Application
import com.example.aplicacion_tesis.network.TokenStore

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
