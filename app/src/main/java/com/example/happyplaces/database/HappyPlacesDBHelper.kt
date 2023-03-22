package com.example.happyplaces.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HappyPlacesDBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val TABLE_NAME: String = "HappyPlaces"
        const val DB_NAME: String = "HappyPlacesDataBase"
        const val COLUMN_ID: String = "id"
        const val COLUMN_TITLE: String = "title"
        const val COLUMN_DESC: String = "desc"
        const val COLUMN_DATE: String = "date"
        const val COLUMN_LAT: String = "lat"
        const val COLUMN_LONG: String = "long"
        const val COLUMN_ADDRESS: String = "address"
        const val COLUMN_IMAGE_URI: String = "image"
        private const val DB_VERSION: Int = 1
        private const val CREATE_TABLE: String =
            "CREATE TABLE $TABLE_NAME ( $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT ," +
                    "$COLUMN_TITLE TEXT NOT NULL ," +
                    "$COLUMN_DESC TEXT NOT NULL ," +
                    "$COLUMN_DATE DATETIME NOT NULL," +
                    "$COLUMN_LAT REAL," +
                    "$COLUMN_LONG REAL," +
                    "$COLUMN_ADDRESS TEXT," +
                    "$COLUMN_IMAGE_URI TEXT )"

        private const val DROP_TABLE: String = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_TABLE)
        onCreate(db)
    }

}