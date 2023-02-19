package com.bernhard.flugsucheapp.DetailRecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bernhard.flugsucheapp.R
import kotlinx.android.synthetic.main.list_item_flight.view.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DetailAdapter(var modelList: List<ModelDetail>) : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_flight, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val formatDate = DateTimeFormatter.ofPattern("dd.MM.yy")
        val formatTime = DateTimeFormatter.ofPattern("HH:mm")
        val data = modelList[position]
        holder.tvDepartureTime.text = LocalTime.parse(data.strDeparture.takeLast(8)).format(formatTime)
        holder.tvArrivalTime.text = LocalTime.parse(data.strArrival.takeLast(8)).format(formatTime)
        holder.tvDepartureDate.text = LocalDate.parse(data.strDeparture.take(10)).format(formatDate)
        holder.tvArrivalDate.text = LocalDate.parse(data.strArrival.take(10)).format(formatDate)
        holder.tvCarrier.text = data.strCarrier
        holder.tvPrice.text = data.dblPrice.toString().plus(" €")
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDepartureTime: TextView = itemView.tvDepartureTime
        var tvArrivalTime: TextView = itemView.tvArrivalTime
        var tvDepartureDate: TextView = itemView.tvDepartureDate
        var tvArrivalDate: TextView = itemView.tvArrivalDate
        var tvCarrier: TextView = itemView.tvCarrier
        var tvPrice: TextView = itemView.tvPrice
    }

}