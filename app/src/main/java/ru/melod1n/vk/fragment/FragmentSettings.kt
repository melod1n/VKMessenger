package ru.melod1n.vk.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MainActivity
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.database.DatabaseHelper

class FragmentSettings : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    private var currentPreferenceLayout = 0

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_settings)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) onResume()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
        currentPreferenceLayout = R.xml.fragment_settings
        init()
    }

    private fun init() {
        setTitle()
        setNavigationIcon()
        setPreferencesFromResource(currentPreferenceLayout, null)
        val account = findPreference<Preference>(CATEGORY_ACCOUNT)
        if (account != null) {
            account.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference -> changeRootLayout(preference) }
        }
        val logout = findPreference<Preference>(KEY_ACCOUNT_LOGOUT)
        if (logout != null) {
            logout.onPreferenceClickListener = this
        }
        val about = findPreference<Preference>(CATEGORY_ABOUT)
        if (about != null) {
            about.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference -> changeRootLayout(preference) }
        }
        applyTintInPreferenceScreen(preferenceScreen)
    }

    private fun setNavigationIcon() {
        val drawable = if (currentPreferenceLayout == R.xml.fragment_settings) null else requireContext().getDrawable(R.drawable.ic_arrow_back)
        drawable?.setTint(AppGlobal.colorAccent)
        (requireActivity() as MainActivity).setNavigationIcon(drawable)
        (requireActivity() as MainActivity).setNavigationClick(if (drawable == null) null else View.OnClickListener { onBackPressed() })
    }

    private fun setTitle() {
        var title = R.string.navigation_settings
        when (currentPreferenceLayout) {
            R.xml.fragment_settings_about -> title = R.string.prefs_about
            R.xml.fragment_settings_account -> title = R.string.prefs_account
        }
        requireActivity().setTitle(title)
    }

    private fun changeRootLayout(preference: Preference): Boolean {
        when (preference.key) {
            CATEGORY_ABOUT -> currentPreferenceLayout = R.xml.fragment_settings_about
            CATEGORY_ACCOUNT -> currentPreferenceLayout = R.xml.fragment_settings_account
        }
        init()
        return true
    }

    private fun applyTintInPreferenceScreen(rootScreen: PreferenceScreen) {
        if (rootScreen.preferenceCount > 0) {
            for (i in 0 until rootScreen.preferenceCount) {
                val preference = rootScreen.getPreference(i)
                tintPreference(preference)
            }
        }
    }

    private fun tintPreference(preference: Preference) {
        if (preference.icon != null && context != null) {
            preference.icon.setTint(AppGlobal.colorAccent)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            KEY_ACCOUNT_LOGOUT -> {
                logout()
                return true
            }
        }
        return false
    }

    fun onBackPressed(): Boolean {
        return if (currentPreferenceLayout == R.xml.fragment_settings) {
            true
        } else {
            currentPreferenceLayout = R.xml.fragment_settings
            init()
            false
        }
    }

    private fun logout() {
        UserConfig.clear()
        DatabaseHelper.getInstance(requireContext()).clear(AppGlobal.database)
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    companion object {
        private const val CATEGORY_ABOUT = "about"
        private const val CATEGORY_ACCOUNT = "account"
        private const val KEY_ACCOUNT_LOGOUT = "account_logout"
    }
}