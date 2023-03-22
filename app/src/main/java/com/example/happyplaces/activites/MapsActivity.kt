package com.example.happyplaces.activites

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happyplaces.BuildConfig
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class MapsActivity : AppCompatActivity(), LoadingCallbacks {

    lateinit var binding: ActivityMapsBinding
    var defaultLatLng:LatLng?=null
    lateinit var mapsFragment: MapsFragment

    private val placesAutoCompleteLauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK) {
            it.data?.let { it1 ->
                val place = Autocomplete.getPlaceFromIntent(it1)
                finishActivityWithData(place.latLng, place.address)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if(!Places.isInitialized()){
            Places.initialize(applicationContext,BuildConfig.MAPS_API_KEY)
        }
        intent.extras?.let {
            if(it.getDouble("lat")!=0.0 && it.getDouble("long")!=0.0){
                defaultLatLng = LatLng(it.getDouble("lat"),it.getDouble("long"))
            }
        }
        mapsFragment = MapsFragment(defaultLatLng)
        mapsFragment.setLoadingCallbacksListener(this)
        supportFragmentManager.beginTransaction().replace(R.id.maps,mapsFragment).commit()
        supportActionBar?.apply {
            title = "Pick Location"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.addressSubmitButton.setOnClickListener {
            mapsFragment.getCurrentAddress()?.let {
                finishActivityWithData(LatLng(it.latitude,it.longitude),it.getAddressLine(0))
            }
        }
    }

    private fun finishActivityWithData(latLng: LatLng?,address:String?){
        val intent = Intent()
        intent.putExtra("lat",latLng?.latitude)
        intent.putExtra("long",latLng?.longitude)
        intent.putExtra("address",address)
        setResult(Activity.RESULT_OK,intent)
        finish()

    }


    private fun disableSubmitButton(){
            binding.addressSubmitButton.setLoading(true)
    }
    private fun enableSubmitButton() {
           binding.addressSubmitButton.setLoading(false)
    }

    override fun onLoadingStart() {
        disableSubmitButton()
    }

    override fun onLoadingEnd() {
       enableSubmitButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.places_search_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.search){
           launchMapsAutoCompleteScreen()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun launchMapsAutoCompleteScreen(){
        try{
            val detailsRequired = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, detailsRequired)
            placesAutoCompleteLauncher.launch(intent.build(this@MapsActivity))
        }catch(e:java.lang.Exception){
            e.printStackTrace()
        }
    }
}