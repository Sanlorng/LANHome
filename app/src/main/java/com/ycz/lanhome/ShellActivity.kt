package com.ycz.lanhome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.viewmodel.ShellViewModel
import kotlinx.android.synthetic.main.activity_shell.*

class ShellActivity : AppCompatActivity(),Shell {
    lateinit var navController: NavController
    lateinit var shellViewModel: ShellViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell)
        shellViewModel = ViewModelProviders.of(this)[ShellViewModel::class.java]
        shellViewModel.canFinish.postValue(intent.getBooleanExtra("canFinish",true))
        window.translucentSystemUI(true)
        navController = findNavController(R.id.containerShell)
//        navController.setGraph(R.navigation.navigation_map)
        setSupportActionBar(toolbarShell)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarShell.setNavigationOnClickListener {
            finish()
        }
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        val navigateId = intent.getIntExtra("navigateId",R.id.shellFragment)
        val bundle = Bundle()
        bundle.putString(AppConfig.KEY_DEVICE_ADDRESS,intent.getStringExtra(AppConfig.KEY_DEVICE_ADDRESS))
        bundle.putString(AppConfig.KEY_DEVICE_NAME,intent.getStringExtra(AppConfig.KEY_DEVICE_NAME))
        bundle.putString(AppConfig.KEY_DEVICE_MAC,intent.getStringExtra(AppConfig.KEY_DEVICE_MAC))
        when(navigateId) {
            R.id.setupWizardFragment -> {
                toolbarShell.isVisible = false
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

        }
        navController.navigate(navigateId,bundle)
//        navController.addOnDestinationChangedListener { controller, destination, arguments ->
//            val isSetupWizard = destination.id == R.id.setupWizardFragment
//            toolbarShell.isVisible = isSetupWizard.not()
//            supportActionBar?.setDisplayHomeAsUpEnabled(isSetupWizard.not())
//        }
    }

    override fun showToolbar() {
        toolbarShell.isVisible = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        if (shellViewModel.canFinish.value == true)
            super.onBackPressed()
    }

    override fun onNavigateUp(): Boolean {
        return if (navController.currentDestination?.id != R.id.setupWizardFragment)
            super.onNavigateUp()
        else
            false
    }
}
