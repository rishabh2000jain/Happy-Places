package com.example.happyplaces.activites

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.happyplaces.BuildConfig
import com.example.happyplaces.MapsUtility
import com.example.happyplaces.database.HappyPlacesDBManager
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.models.HappyPlacesModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HappyPlaceDetailActivity : AppCompatActivity() {
    companion object {
        const val HAPPY_PLACE_DATA = "DataHappyPlace"
    }

    private val launchUpdatePlaceActivityWithResultContracts: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    refreshDetailScreen()
                }
            }
        }
    lateinit var binding: ActivityHappyPlaceDetailBinding
    var happyPlacesModel: HappyPlacesModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = "Place Detail"
            it.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        intent?.also {
            happyPlacesModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra(MainActivity.HAPPY_PLACE_DATA, HappyPlacesModel::class.java)
            } else {
                it.getParcelableExtra(MainActivity.HAPPY_PLACE_DATA) as? HappyPlacesModel
            }
            updateDataOnUI()
        }
        if (happyPlacesModel == null) {
            binding.editBtn.isEnabled = false
            binding.editBtn.isClickable = false
        } else {
            binding.editBtn.setOnClickListener {
                val intent = Intent(this@HappyPlaceDetailActivity, HappyPlaceActivity::class.java)
                intent.putExtra(HAPPY_PLACE_DATA, happyPlacesModel)
                launchUpdatePlaceActivityWithResultContracts.launch(intent)
            }
            binding.deleteBtn.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    HappyPlacesDBManager.instance()
                        .deleteHappyPlace(happyPlacesModel!!.id!!.toString())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HappyPlaceDetailActivity,
                            "Successfully deleted ${happyPlacesModel!!.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun updateDataOnUI() {
        happyPlacesModel?.apply {
            binding.descTxt.text = desc
            binding.titleTxt.text = title
            binding.placeImgView.setImageURI(Uri.parse(imageUrl))
            Glide.with(this@HappyPlaceDetailActivity).load(
                Uri.parse(
                    MapsUtility.getStaticMapUrl(
                        latLng = LatLng(
                            latitude ?: 0.0,
                            longitude ?: 0.0
                        ), BuildConfig.MAPS_API_KEY
                    )
                )
            ).into(binding.staticMapImg)
        }
    }

    private suspend fun refreshDetailScreen() {
        withContext(Dispatchers.IO) {
            if (happyPlacesModel != null) {
                val data = HappyPlacesDBManager.instance().getHappyPlace(happyPlacesModel!!.id!!)
                if (data != null) {
                    happyPlacesModel = data
                }
            }
        }
        updateDataOnUI()
    }
}