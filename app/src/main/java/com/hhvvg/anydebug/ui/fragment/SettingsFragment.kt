package com.hhvvg.anydebug.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.config.ConfigDbHelper
import com.hhvvg.anydebug.config.ConfigPreferences

class SettingsFragment : PreferenceFragmentCompat() {
    private val globalEditPreference by lazy { findPreference<SwitchPreferenceCompat>(getString(R.string.global_edit_enable_key)) }
    private val persistentPreference by lazy { findPreference<SwitchPreferenceCompat>(getString(R.string.edit_persistent_key)) }
    private val aboutPerf by lazy { findPreference<Preference>(getString(R.string.about_key)) }
    private val navController by lazy { findNavController() }
    private val localSp by lazy {
        ConfigPreferences(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        globalEditPreference?.setOnPreferenceChangeListener { _, newValue ->
            sendEditEnableBroadcast(newValue as Boolean)
            val edit = localSp.edit()
            edit.putBoolean(ConfigDbHelper.CONFIG_EDIT_ENABLED_COLUMN, newValue)
            edit.apply()
            true
        }
        persistentPreference?.setOnPreferenceChangeListener { _, newValue ->
            sendEditPersistentBroadcast(newValue as Boolean)
            val edit = localSp.edit()
            edit.putBoolean(ConfigDbHelper.CONFIG_PERSISTENT_ENABLED_COLUMN, newValue)
            edit.apply()
            true
        }
        aboutPerf?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingFragmentToAboutFragment()
            navController.navigate(action)
            false
        }
    }

    private fun sendEditEnableBroadcast(enabled: Boolean) {
        val intent = Intent(ACTION_GLOBAL_ENABLE).apply {
            val enableAction = if (enabled) {
                ACTION_ENABLE
            } else {
                ACTION_DISABLE
            }
            putExtra(EXTRA_CONTROL_ACTION, enableAction)
        }
        context?.sendBroadcast(intent)
    }

    private fun sendEditPersistentBroadcast(enabled: Boolean) {
        val intent = Intent(ACTION_PERSISTENT_ENABLE).apply {
            val enableAction = if (enabled) {
                ACTION_ENABLE
            } else {
                ACTION_DISABLE
            }
            putExtra(EXTRA_CONTROL_ACTION, enableAction)
        }
        context?.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_GLOBAL_ENABLE = "com.hhvvg.action.control.enable"
        const val ACTION_PERSISTENT_ENABLE = "com.hhvvg.action.persistent.enable"

        const val EXTRA_CONTROL_ACTION = "EXTRA_CONTROL_ACTION"

        const val ACTION_ENABLE = 0
        const val ACTION_DISABLE = 1
    }
}