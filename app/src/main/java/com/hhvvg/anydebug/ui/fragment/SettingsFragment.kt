package com.hhvvg.anydebug.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.hhvvg.anydebug.IConfigurationService
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.receiver.EditControlReceiver
import com.hhvvg.anydebug.receiver.EditControlReceiver.Companion.ACTION_DISABLE
import com.hhvvg.anydebug.receiver.EditControlReceiver.Companion.ACTION_ENABLE
import com.hhvvg.anydebug.receiver.EditControlReceiver.Companion.EXTRA_CONTROL_ACTION
import com.hhvvg.anydebug.util.CONFIGURATION_SERVICE
import com.kaisar.xservicemanager.XServiceManager

class SettingsFragment : PreferenceFragmentCompat() {
    private val globalEditPreference by lazy { findPreference<SwitchPreferenceCompat>(getString(R.string.global_edit_enable_key)) }
    private val persistentPreference by lazy { findPreference<SwitchPreferenceCompat>(getString(R.string.edit_persistent_key)) }
    private val aboutPerf by lazy { findPreference<Preference>(getString(R.string.about_key)) }
    private val navController by lazy { findNavController() }
    private val confService: IConfigurationService by lazy {
        val binder = XServiceManager.getService(CONFIGURATION_SERVICE)
        IConfigurationService.Stub.asInterface(binder)
    }
    private val stateChangeReceiver by lazy {
        EditControlReceiver { enabled, source ->
            if (source != SOURCE) {
                globalEditPreference?.isChecked = enabled
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EditControlReceiver.register(stateChangeReceiver, requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(stateChangeReceiver)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        globalEditPreference?.isChecked = confService.isEditEnabled
        persistentPreference?.isChecked = confService.isPersistentEnabled
        globalEditPreference?.setOnPreferenceChangeListener { _, newValue ->
            EditControlReceiver.sendBroadcast(requireContext(), newValue as Boolean, SOURCE)
            confService.isEditEnabled = newValue
            true
        }
        persistentPreference?.setOnPreferenceChangeListener { _, newValue ->
            sendEditPersistentBroadcast(newValue as Boolean)
            confService.isPersistentEnabled = newValue
            true
        }
        aboutPerf?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingFragmentToAboutFragment()
            navController.navigate(action)
            false
        }
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
        private const val SOURCE = "SettingsFragmentSource"
        const val ACTION_PERSISTENT_ENABLE = "com.hhvvg.action.persistent.enable"
    }
}