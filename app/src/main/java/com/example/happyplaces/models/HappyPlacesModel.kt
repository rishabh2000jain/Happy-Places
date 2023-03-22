package com.example.happyplaces.models

import android.os.Parcel
import android.os.Parcelable

data class HappyPlacesModel(
    var id:Int?,
    var title: String?,
    var desc: String?,
    var date: String?,
    var address: String?,
    var imageUrl: String?,
    var latitude: Double?,
    var longitude: Double?,
):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeString(date)
        parcel.writeString(address)
        parcel.writeString(imageUrl)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HappyPlacesModel> {
        override fun createFromParcel(parcel: Parcel): HappyPlacesModel {
            return HappyPlacesModel(parcel)
        }

        override fun newArray(size: Int): Array<HappyPlacesModel?> {
            return arrayOfNulls(size)
        }
    }

}