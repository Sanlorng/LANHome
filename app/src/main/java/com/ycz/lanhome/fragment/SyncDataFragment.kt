package com.ycz.lanhome.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ycz.lanhome.R
import com.ycz.lanhome.viewmodel.SyncDataViewModel

class SyncDataFragment : Fragment() {

    companion object {
        fun newInstance() = SyncDataFragment()
    }

    private lateinit var viewModel: SyncDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sync_data_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SyncDataViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
