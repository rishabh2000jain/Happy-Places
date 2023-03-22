package com.example.happyplaces.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.happyplaces.adapters.FavouritePlacesListAdapter
import com.example.happyplaces.database.HappyPlacesDBManager
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlacesModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val databaseUpdatesListener = object : HappyPlacesDBManager.DatasetUpdateListener {
        override fun databaseDatasetsUpdated() {
            lifecycleScope.launch {
                loadHappyPlaces(appendData = false)
            }
        }

    }

    companion object{
        const val HAPPY_PLACE_DATA = "DataHappyPlace"
    }

    private lateinit var binding:ActivityMainBinding
    private lateinit var favouritePlacesListAdapter:FavouritePlacesListAdapter

    private val launchAddPlaceActivityWithResultContracts:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    override fun onStart() {
        super.onStart()
        HappyPlacesDBManager.instance().addDatasetChangedListener(databaseUpdatesListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        HappyPlacesDBManager.instance().removeListener(databaseUpdatesListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if(supportActionBar!=null){
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.setNavigationOnClickListener {
               onBackPressedDispatcher.onBackPressed();
            }

        }
        binding.addHappyPlaceBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, HappyPlaceActivity::class.java)
            launchAddPlaceActivityWithResultContracts.launch(intent)
        }
        favouritePlacesListAdapter = FavouritePlacesListAdapter(){data->
            val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
            intent.putExtra(HAPPY_PLACE_DATA,data)
            startActivity(intent)
        }
        binding.happyPlacesList.adapter = favouritePlacesListAdapter

        lifecycleScope.launch {
            loadHappyPlaces()
        }

    }

    private suspend fun loadHappyPlaces(appendData: Boolean = true){
        handleLoader(true)
        var happyPlaces: List<HappyPlacesModel>?
        withContext(Dispatchers.IO){
            happyPlaces = HappyPlacesDBManager.instance().fetchHappyPlacesList()
        }
        if(appendData) {
            favouritePlacesListAdapter.appendData(happyPlaces!!)
        }else{
            favouritePlacesListAdapter.resetData(happyPlaces!!)
        }
        if(happyPlaces.isNullOrEmpty()){
            binding.noPlacesTxt.visibility = View.VISIBLE
        }else{
            binding.noPlacesTxt.visibility = View.GONE
        }
        handleLoader(false)
    }

    private fun handleLoader(show:Boolean){
        if(show){
            binding.progressIndicator.visibility = View.VISIBLE
        }else{
            binding.progressIndicator.visibility = View.GONE
        }
    }

}