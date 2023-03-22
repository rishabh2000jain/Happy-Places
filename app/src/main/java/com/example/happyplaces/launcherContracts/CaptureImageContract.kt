package com.example.happyplaces.launcherContracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContract

class CaptureImageContract : ActivityResultContract<Intent, Pair<Boolean, Bitmap?>>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }


    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, Bitmap?> {
       return  (resultCode == Activity.RESULT_OK) to (intent?.extras?.get("data") as Bitmap?)
    }

    override fun getSynchronousResult(
        context: Context,
        input: Intent
    ): SynchronousResult<Pair<Boolean, Bitmap?>>? {
        return null
    }
}