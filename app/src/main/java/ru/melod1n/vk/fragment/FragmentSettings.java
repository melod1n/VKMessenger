package ru.melod1n.vk.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import ru.melod1n.vk.R;
import ru.melod1n.vk.activity.MainActivity;
import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.database.DatabaseHelper;

public class FragmentSettings extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    private static final String KEY_ACCOUNT_LOGOUT = "account_logout";

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.navigation_settings);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
        init(savedInstanceState, rootKey);
    }

    private void init(Bundle savedInstanceState, String rootKey) {
        Preference logout = findPreference(KEY_ACCOUNT_LOGOUT);
        if (logout != null) {
            logout.setOnPreferenceClickListener(this);
        }

        applyTintInPreferenceScreen(getPreferenceScreen());
    }

    private void applyTintInPreferenceScreen(@NonNull PreferenceScreen rootScreen) {
        if (rootScreen.getPreferenceCount() > 0) {
            for (int i = 0; i < rootScreen.getPreferenceCount(); i++) {
                Preference preference = rootScreen.getPreference(i);
                tintPreference(preference);
            }
        }
    }

    private void tintPreference(@NonNull Preference preference) {
        if (preference.getIcon() != null && getContext() != null) {
            preference.getIcon().setTint(AppGlobal.colorAccent);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_ACCOUNT_LOGOUT:
                logout();
                return true;
        }

        return false;
    }

    private void logout() {
        UserConfig.clear();
        DatabaseHelper.getInstance(requireContext()).clear(AppGlobal.database);
        startActivity(new Intent(requireContext(), MainActivity.class));
        requireActivity().finish();
    }
}
