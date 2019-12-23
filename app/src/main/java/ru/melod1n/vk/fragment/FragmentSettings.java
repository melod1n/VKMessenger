package ru.melod1n.vk.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

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

    private static final String CATEGORY_ABOUT = "about";
    private static final String CATEGORY_ACCOUNT = "account";

    private static final String KEY_ACCOUNT_LOGOUT = "account_logout";

    private int currentPreferenceLayout;

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.navigation_settings);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        currentPreferenceLayout = R.xml.fragment_settings;

        init();
    }

    private void init() {
        setTitle();
        setNavigationIcon();
        setPreferencesFromResource(currentPreferenceLayout, null);

        Preference account = findPreference(CATEGORY_ACCOUNT);
        if (account != null) {
            account.setOnPreferenceClickListener(this::changeRootLayout);
        }

        Preference logout = findPreference(KEY_ACCOUNT_LOGOUT);
        if (logout != null) {
            logout.setOnPreferenceClickListener(this);
        }


        Preference about = findPreference(CATEGORY_ABOUT);
        if (about != null) {
            about.setOnPreferenceClickListener(this::changeRootLayout);
        }

        applyTintInPreferenceScreen(getPreferenceScreen());
    }

    private void setNavigationIcon() {
        Drawable drawable = currentPreferenceLayout == R.xml.fragment_settings ? null : requireContext().getDrawable(R.drawable.ic_arrow_back);
        if (drawable != null) {
            drawable.setTint(AppGlobal.colorAccent);
        }

        ((MainActivity) requireActivity()).setNavigationIcon(drawable);
        ((MainActivity) requireActivity()).setNavigationClick(drawable == null ? null : (View.OnClickListener) v -> {
            onBackPressed();
        });
    }

    private void setTitle() {
        int title = R.string.navigation_settings;

        switch (currentPreferenceLayout) {
            case R.xml.fragment_settings_about:
                title = R.string.prefs_about;
                break;
            case R.xml.fragment_settings_account:
                title = R.string.prefs_account;
                break;
        }

        requireActivity().setTitle(title);
    }

    private boolean changeRootLayout(@NonNull Preference preference) {
        switch (preference.getKey()) {
            case CATEGORY_ABOUT:
                currentPreferenceLayout = R.xml.fragment_settings_about;
                break;
            case CATEGORY_ACCOUNT:
                currentPreferenceLayout = R.xml.fragment_settings_account;
                break;
        }

        init();
        return true;
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

    public boolean onBackPressed() {
        if (currentPreferenceLayout == R.xml.fragment_settings) {
            return true;
        } else {
            currentPreferenceLayout = R.xml.fragment_settings;
            init();

            return false;
        }
    }

    private void logout() {
        UserConfig.clear();
        DatabaseHelper.getInstance(requireContext()).clear(AppGlobal.database);
        startActivity(new Intent(requireContext(), MainActivity.class));
        requireActivity().finish();
    }
}
