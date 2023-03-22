package com.example.happyplaces

import com.google.android.gms.maps.model.LatLng

object MapsUtility {
    fun getStaticMapUrl(latLng: LatLng,apiKey:String):String{
       return "https://maps.googleapis.com/maps/api/staticmap?size=330x240&maptype=terrain&markers=color:red|${latLng.latitude},${latLng.longitude}&key=${apiKey}"
    }
}