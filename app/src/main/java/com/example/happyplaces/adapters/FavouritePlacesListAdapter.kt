package com.example.happyplaces.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.databinding.HappyPlacesListTileBinding
import com.example.happyplaces.models.HappyPlacesModel

class FavouritePlacesListAdapter(val onTap:(data:HappyPlacesModel)->Unit) : RecyclerView.Adapter<FavouritePlacesListAdapter.CustomViewHolder>() {
    private val happyPlaces:MutableList<HappyPlacesModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = HappyPlacesListTileBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding = binding)
    }

    override fun getItemCount(): Int {
        return happyPlaces.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.binding.root.setOnClickListener {
                onTap(happyPlaces[position])
            }
            holder.binding.placeImageView.setImageURI(Uri.parse(happyPlaces[position].imageUrl))
            holder.binding.dateTxt.text = (happyPlaces[position].date)
            holder.binding.titleTxt.text = (happyPlaces[position].title)
    }

    fun appendData(happyPlaces:List<HappyPlacesModel>){
        this.happyPlaces.addAll(happyPlaces)
        notifyDataSetChanged()
    }
    fun resetData(happyPlaces:List<HappyPlacesModel>){
        this.happyPlaces.clear()
        this.happyPlaces.addAll(happyPlaces)
        notifyDataSetChanged()
    }

    class CustomViewHolder(val binding: HappyPlacesListTileBinding):RecyclerView.ViewHolder(binding.root) {}
}