package com.ycz.lanhome.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ycz.lanhome.R
import kotlinx.android.synthetic.main.fragment_setup_wizard.*

/**
 * A simple [Fragment] subclass.
 */
class SetupWizardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup_wizard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginFragment.setOnClickListener {
            findNavController().navigate(R.id.action_setupWizardFragment_to_loginFragment)
        }
    }

}
