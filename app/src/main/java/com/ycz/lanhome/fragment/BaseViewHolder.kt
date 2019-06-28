package com.ycz.lanhome.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntegerRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder(view: View):RecyclerView.ViewHolder(view) {
    constructor(@LayoutRes resId: Int, parent: ViewGroup):
            this(
                LayoutInflater
                    .from(parent.context)
                    .inflate(resId,parent,false)
            )
}