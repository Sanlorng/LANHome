package com.ycz.lanhome.fragment

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.preference.*
import com.ycz.lanhome.app.AppConfig
import com.ycz.lanhome.BuildConfig
import com.ycz.lanhome.R
import com.ycz.lanhome.ShellActivity
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
        val exitAccountPreference =
            findPreference<Preference>(getString(R.string.key_setting_exit_account))
        val versionNamePreference =
            findPreference<Preference>(getString(R.string.key_setting_version_name))
        val sdkVersionPreference =
            findPreference<Preference>(getString(R.string.key_setting_sdk_version))
        val checkUpdatePreference =
            findPreference<Preference>(getString(R.string.key_setting_check_update))
        val updateHistoryPreference =
            findPreference<Preference>(getString(R.string.key_setting_update_history))
        val accountCategory =
            findPreference<PreferenceCategory>(getString(R.string.key_account_category))
        val syncDataPreference =
            findPreference<ListPreference>(getString(R.string.key_setting_sync_data))
        versionNamePreference?.summary = BuildConfig.VERSION_NAME
        sdkVersionPreference?.summary = context?.applicationInfo?.targetSdkVersion.toString()
        if (AppConfig.userId == -1) {
            accountCategory?.isVisible = false
        }
        viewModel.updateInfo.observe(this, Observer {
            if (it.versionCode > BuildConfig.VERSION_CODE)
                checkUpdatePreference?.summary = "检查到新版本：${it.versionName}"
            else
                checkUpdatePreference?.summary = "暂无新版更新"
        })

        viewModel.updateState.observe(this, Observer {
            if (it.code != 200)
                checkUpdatePreference?.summary = "检查更新失败"
        })
        checkUpdatePreference?.setOnPreferenceClickListener {
            checkUpdatePreference.summary = "正在检查更新"
            viewModel.getAppUpdate()
            true
        }
        val array = resources.getStringArray(R.array.sync_frequency)
        syncDataPreference?.setOnPreferenceChangeListener { preference, newValue ->

            val value = (newValue as String).toInt()
            if (value < array.size && value > -1)
                syncDataPreference.summary = array[value]
            true
        }
        syncDataPreference?.onPreferenceChangeListener?.onPreferenceChange(
            syncDataPreference,
            syncDataPreference.value
        )
        findPreference<Preference>(getString(R.string.ket_setting_about_us))?.setOnPreferenceClickListener {
            startActivity(Intent(context,ShellActivity::class.java).apply {
                putExtra(AppConfig.KEY_NAVIGATE_ID,R.id.aboutFragment)
            })
            true
        }
        viewModel.getAppUpdate()
    }

}
