package com.example.happyplaces.application

import android.app.Application
import com.example.happyplaces.database.HappyPlacesDBManager

class HappyPlacesApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        HappyPlacesDBManager.init(applicationContext)
    }
}