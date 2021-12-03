package com.reza.carsproject.screens.detail.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reza.carsproject.databinding.RecyclerDetailBinding

class DetailListAdapter(private val itemsList: HashMap<String,String>) : RecyclerView.Adapter<DetailListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        RecyclerDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemsList.toList()[position])
    }

    override fun getItemCount(): Int = itemsList.size

    class ViewHolder(private val binding: RecyclerDetailBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, String>) {
            with(binding){
                txtItemName.text = item.first
                txtItemValue.text = item.second
            }
        }

    }


}