package com.ycz.lanhome.fragment

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.BuildConfig
import com.ycz.lanhome.R
import com.ycz.lanhome.service.DeviceService
import com.ycz.lanhome.viewmodel.SettingsViewModel


class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var viewModel: SettingsViewModel


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val accountPreference = findPreference<Preference>(getString(R.string.key_setting_account))
        val exitAccountPreference = findPreference<Preference>(getString(R.string.key_setting_exit_account))
        val versionNamePreference = findPreference<Preference>(getString(R.string.key_setting_version_name))
        val sdkVersionPreference = findPreference<Preference>(getString(R.string.key_setting_sdk_version))
        val checkUpdatePreference = findPreference<Preference>(getString(R.string.key_setting_check_update))
        val updateHistoryPreference = findPreference<Preference>(getString(R.string.key_setting_update_history))
        val accountCategory = findPreference<PreferenceCategory>(getString(R.string.key_account_category))

        versionNamePreference?.summary = BuildConfig.VERSION_NAME
        sdkVersionPreference?.summary = context?.applicationInfo?.targetSdkVersion.toString()
        if (AppConfig.userId == -1) {
            accountCategory?.isVisible = false
        }
    }

}
