package ru.melod1n.vk.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import ru.melod1n.vk.R;
import ru.melod1n.vk.current.BaseFragment;
import ru.melod1n.vk.fragment.FragmentMessages;

public class FragmentSwitcher {

    private static Fragment currentFragment = null;
    private static ArrayList<Fragment> fragments = new ArrayList<>();

    public static FragmentMessages fragmentMessages = new FragmentMessages();

    public static void switchFragment(AppCompatActivity parentActivity, @Nullable Fragment parentFragment, @NonNull Fragment fragment, @Nullable Bundle extraData, boolean withBackStack) {
        if (currentFragment != null && fragment.getClass().getSimpleName().equals(currentFragment.getClass().getSimpleName())) {
            return;
        }

        if (extraData != null) fragment.setArguments(extraData);

        boolean[] containsList = new boolean[]{false, false};

        for (Fragment f : fragments) {
            if (f.getClass().getSimpleName().equals(fragment.getClass().getSimpleName())) {
                containsList[0] = true;
                break;
            }
        }

        boolean fromFragment = parentFragment != null;

        FragmentManager manager = fromFragment ? parentFragment.requireFragmentManager() : parentActivity.getSupportFragmentManager();

        for (Fragment f : manager.getFragments()) {
            if (f.getClass().getSimpleName().equals(fragment.getClass().getSimpleName())) {
                containsList[1] = true;
                break;
            }
        }

        boolean contains = containsList[0] && containsList[1];

        if (contains) {
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).onReopen(extraData);
            }
        } else {
            if (!containsList[0])
                fragments.add(fragment);
        }

        FragmentTransaction transaction = manager.beginTransaction();

        if (currentFragment != null) {
            Fragment managerCurrentFragment = null;

            for (Fragment f : manager.getFragments()) {
                if (f.isVisible()) {
                    managerCurrentFragment = f;
                    break;
                }
            }

            if (managerCurrentFragment != currentFragment) {
                currentFragment = managerCurrentFragment;
            }

            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            if (contains) {
                transaction.show(fragment);
            } else {
                transaction.add(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
            }
        } else {
            transaction.add(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        }

        if (withBackStack) transaction.addToBackStack(fragment.getClass().getSimpleName());
        manager.addOnBackStackChangedListener(() -> {
            for (Fragment f : manager.getFragments()) {
                if (f.isVisible()) {
                    currentFragment = f;
                    break;
                }
            }
        });

        transaction.commit();

        currentFragment = fragment;
    }

    public static void switchFragment(AppCompatActivity parentActivity, @NonNull Fragment fragment) {
        switchFragment(parentActivity, null, fragment, null, false);
    }

}
