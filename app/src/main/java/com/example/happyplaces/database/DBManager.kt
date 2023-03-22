package com.example.happyplaces.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.happyplaces.models.HappyPlacesModel
import java.sql.SQLException

class HappyPlacesDBManager private constructor() {

    companion object {
        @Volatile
        private var databaseHelper: HappyPlacesDBHelper? = null

        @Volatile
        private var database: SQLiteDatabase? = null

        @Volatile
        private var INSTANCE: HappyPlacesDBManager? = null

        //Should be called before starting of application
        @Throws(SQLException::class)
        fun init(context: Context) {
            synchronized(this) {
                databaseHelper = databaseHelper ?: HappyPlacesDBHelper(context)
                database = databaseHelper!!.writableDatabase
            }
        }

        fun instance(): HappyPlacesDBManager {
            synchronized(this) {
                INSTANCE = INSTANCE ?: HappyPlacesDBManager()
                return INSTANCE!!
            }
        }

    }

    private val listeners:MutableList<DatasetUpdateListener> = mutableListOf()

    suspend fun insertHappyPlace(data: HappyPlacesModel): Boolean {
        val contentValue = ContentValues()
        contentValue.apply {
            put(HappyPlacesDBHelper.COLUMN_TITLE,data.title)
            put(HappyPlacesDBHelper.COLUMN_DESC,data.desc)
            put(HappyPlacesDBHelper.COLUMN_DATE,data.date)
            put(HappyPlacesDBHelper.COLUMN_ADDRESS,data.address)
            put(HappyPlacesDBHelper.COLUMN_IMAGE_URI,data.imageUrl)
            put(HappyPlacesDBHelper.COLUMN_LAT,data.latitude)
            put(HappyPlacesDBHelper.COLUMN_LONG,data.longitude)
        }
        val updateSuccess:Boolean =  database?.insert(HappyPlacesDBHelper.TABLE_NAME, null, contentValue) != -1L
        if (updateSuccess){
            notifyListeners()
        }
        return updateSuccess
    }
    suspend fun updateHappyPlace(data: HappyPlacesModel): Boolean {
        val contentValue = ContentValues()
        contentValue.apply {
            put(HappyPlacesDBHelper.COLUMN_TITLE,data.title)
            put(HappyPlacesDBHelper.COLUMN_DESC,data.desc)
            put(HappyPlacesDBHelper.COLUMN_DATE,data.date)
            put(HappyPlacesDBHelper.COLUMN_ADDRESS,data.address)
            put(HappyPlacesDBHelper.COLUMN_IMAGE_URI,data.imageUrl)
            put(HappyPlacesDBHelper.COLUMN_LAT,data.latitude)
            put(HappyPlacesDBHelper.COLUMN_LONG,data.longitude)
        }
        val updateSuccess:Boolean =  (database?.update(
            HappyPlacesDBHelper.TABLE_NAME, contentValue, "${HappyPlacesDBHelper.COLUMN_ID}=?",
            arrayOf(data.id.toString())
        ) ?: 0) > 0
        if (updateSuccess){
            notifyListeners()
        }
        return updateSuccess
    }

    suspend fun deleteHappyPlace(id: String): Boolean {
        val updateSuccess:Boolean =   (database?.delete(
            HappyPlacesDBHelper.TABLE_NAME,
            "${HappyPlacesDBHelper.COLUMN_ID}=?",
            arrayOf<String>(id)
        ) ?: 0) > 0
        if (updateSuccess){
            notifyListeners()
        }
        return updateSuccess
    }

    @SuppressLint("Range")
    suspend fun fetchHappyPlacesList(): List<HappyPlacesModel> {
        val placesList: MutableList<HappyPlacesModel> = mutableListOf<HappyPlacesModel>()
        val cursor = database?.query(
            HappyPlacesDBHelper.TABLE_NAME, null, null, null, null, null,
            HappyPlacesDBHelper.COLUMN_DATE,
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                placesList.add(
                    HappyPlacesModel(
                        cursor.getInt(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_DESC)),
                        cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_ADDRESS)),
                        cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_IMAGE_URI)),
                        cursor.getDouble(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_LAT)),
                        cursor.getDouble(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_LONG)),
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return placesList
    }
    @SuppressLint("Range")
    suspend fun getHappyPlace(id: Int): HappyPlacesModel? {
        var cursor:Cursor? = null
        var happyPlacesModel:HappyPlacesModel?=null
        try {
             cursor = database?.rawQuery(
                "SELECT * FROM ${HappyPlacesDBHelper.TABLE_NAME} WHERE ${HappyPlacesDBHelper.COLUMN_ID}=${id}",
                null
            )

            if(cursor!=null && cursor.moveToFirst()){
                happyPlacesModel = HappyPlacesModel(
                    cursor.getInt(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_DESC)),
                    cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_IMAGE_URI)),
                    cursor.getDouble(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(HappyPlacesDBHelper.COLUMN_LONG)),
                )
            }

        }catch (exception:Exception){ }finally {
            cursor?.close()
        }
        return happyPlacesModel
    }

    fun addDatasetChangedListener(listener: DatasetUpdateListener){
        listeners.add(listener)
    }

    fun removeListener(listener: DatasetUpdateListener){
        listeners.removeIf { it==listener }
    }

    private fun notifyListeners(){
        listeners.forEach(){
            it.databaseDatasetsUpdated()
        }
    }


    interface DatasetUpdateListener{
        fun databaseDatasetsUpdated()
    }

}

