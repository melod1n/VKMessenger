package ru.melod1n.vk.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MainActivity
import ru.melod1n.vk.activity.UpdateActivity
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.EventInfo
import ru.melod1n.vk.database.DatabaseHelper
import java.util.*

class FragmentSettings : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    companion object {
        private val listeners = ArrayList<OnEventListener>()

        fun addOnEventListener(onEventListener: OnEventListener) {
            listeners.add(onEventListener)
        }

        fun removeOnEventListener(onEventListener: OnEventListener?) {
            onEventListener ?: return

            listeners.remove(onEventListener)
        }

        const val CATEGORY_GENERAL = "general"
        const val KEY_HIDE_KEYBOARD_ON_SCROLL_UP = "hide_keyboard_on_scroll_up"

        const val CATEGORY_APPEARANCE = "appearance"
        const val KEY_DARK_THEME = "appearance_dark_theme"
        const val KEY_EXTENDED_CONVERSATIONS = "appearance_extended_conversations"

        const val CATEGORY_ABOUT = "about"
        const val KEY_APP_VERSION = "app_version"

        const val CATEGORY_ACCOUNT = "account"
        const val KEY_ACCOUNT_LOGOUT = "account_logout"
    }

    interface OnEventListener {
        fun onNewEvent(event: EventInfo<*>)
    }

    private var currentLayout = 0
    private var currentTitle = ""

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

        currentLayout = R.xml.fragment_settings
        currentTitle = getString(R.string.navigation_settings)

        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("currentLayout", currentLayout)
        outState.putString("currentTitle", currentTitle)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            currentLayout = it.getInt("currentLayout", R.xml.fragment_settings)
            currentTitle = it.getString("currentTitle", getString(R.string.navigation_settings))

            requireActivity().title = currentTitle

            init(it)
        }
    }

    private fun init(savedInstanceState: Bundle? = null) {
        if (savedInstanceState == null)
            setTitle()

        setNavigationIcon()
        setPreferencesFromResource(currentLayout, null)

        val general = findPreference<Preference>(CATEGORY_GENERAL)
        general?.onPreferenceClickListener = Preference.OnPreferenceClickListener { changeRootLayout(it) }

        val account = findPreference<Preference>(CATEGORY_ACCOUNT)
        account?.onPreferenceClickListener = Preference.OnPreferenceClickListener { changeRootLayout(it) }

        val logout = findPreference<Preference>(KEY_ACCOUNT_LOGOUT)
        logout?.onPreferenceClickListener = this

        val about = findPreference<Preference>(CATEGORY_ABOUT)
        about?.onPreferenceClickListener = Preference.OnPreferenceClickListener { changeRootLayout(it) }

        val appVersion = findPreference<Preference>(KEY_APP_VERSION)
        appVersion?.onPreferenceClickListener = this

        val appearance = findPreference<Preference>(CATEGORY_APPEARANCE)
        appearance?.onPreferenceClickListener = Preference.OnPreferenceClickListener { changeRootLayout(it) }

        val darkTheme = findPreference<Preference>(KEY_DARK_THEME)
        darkTheme?.onPreferenceChangeListener = this

        val extendedConversations = findPreference<Preference>(KEY_EXTENDED_CONVERSATIONS)
        extendedConversations?.onPreferenceChangeListener = this

        applyTintInPreferenceScreen(preferenceScreen)
    }

    private fun setNavigationIcon() {
        val drawable = if (currentLayout == R.xml.fragment_settings) null else requireContext().getDrawable(R.drawable.ic_arrow_back)
        drawable?.setTint(AppGlobal.colorAccent)

        (activity as MainActivity).setNavigationIcon(drawable)
        (activity as MainActivity).setNavigationClick(if (drawable == null) null else View.OnClickListener { onBackPressed() })
    }

    private fun setTitle() {
        var title = R.string.navigation_settings
        when (currentLayout) {
            R.xml.fragment_settings_general -> title = R.string.prefs_general
            R.xml.fragment_settings_appearance -> title = R.string.prefs_appearance
            R.xml.fragment_settings_about -> title = R.string.prefs_about
            R.xml.fragment_settings_account -> title = R.string.prefs_account
        }

        currentTitle = getString(title)

        requireActivity().title = currentTitle
    }

    private fun changeRootLayout(preference: Preference): Boolean {
        currentLayout = when (preference.key) {
            CATEGORY_GENERAL -> R.xml.fragment_settings_general
            CATEGORY_ABOUT -> R.xml.fragment_settings_about
            CATEGORY_ACCOUNT -> R.xml.fragment_settings_account
            CATEGORY_APPEARANCE -> R.xml.fragment_settings_appearance
            else -> R.xml.fragment_settings
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
            KEY_APP_VERSION -> {
                openUpdateScreen()
                return true
            }
        }
        return false
    }

    private fun openUpdateScreen() {
        startActivity(Intent(requireContext(), UpdateActivity::class.java))
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference.key) {
            KEY_EXTENDED_CONVERSATIONS -> {
//                sendEvent(EventInfo<Any>(EventInfo.CONVERSATIONS_REFRESH))
                return true
            }
            KEY_DARK_THEME -> {
                AppGlobal.updateTheme(newValue as String)
                return true
            }
        }

        return false
    }

    private fun sendEvent(event: EventInfo<*>) {
        for (listener in listeners) {
            listener.onNewEvent(event)
        }
    }

    fun onBackPressed(): Boolean {
        return if (currentLayout == R.xml.fragment_settings) {
            true
        } else {
            currentLayout = R.xml.fragment_settings
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
}