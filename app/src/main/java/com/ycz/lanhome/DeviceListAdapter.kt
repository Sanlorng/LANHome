package com.ycz.lanhome

import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ycz.lanhome.fragment.BaseViewHolder
import com.ycz.lanhome.model.Device
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceListAdapter(private val list: ArrayList<Device>): RecyclerView.Adapter<BaseViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.itemView.itemDeviceTitle.text = list[position].name
        holder.itemView.itemDeviceSubtitle.text = list[position].ip
        holder.itemView.setOnClickListener {
            it.context.startActivity(Intent(it.context,ShellActivity::class.java).apply {
                putExtra("navigateId",R.id.deviceDetailsFragment)
                putExtra("deviceAddress",list[position].ip)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(R.layout.item_device,parent)
    }
}