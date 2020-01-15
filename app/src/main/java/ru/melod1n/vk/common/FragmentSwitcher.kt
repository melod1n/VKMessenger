package ru.melod1n.vk.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.melod1n.vk.R
import ru.melod1n.vk.current.BaseFragment
import ru.melod1n.vk.util.ArrayUtil.isEmpty
import java.util.*

class FragmentSwitcher {
    fun switchFragment(parentActivity: AppCompatActivity, fragment: Fragment) {
        switchFragment(parentActivity, null, fragment, null, false)
    }

    companion object {
        private var currentFragmentTag: String? = null
        private val tags = ArrayList<String>()
        fun switchFragment(parentActivity: AppCompatActivity, parentFragment: Fragment?, fragment: Fragment, extraData: Bundle?, withBackStack: Boolean) {
            val fromFragment = parentFragment != null
            val manager = if (fromFragment) parentFragment!!.requireFragmentManager() else parentActivity.supportFragmentManager
            var currentFragment = getCurrentFragment(parentActivity.supportFragmentManager)
            if (currentFragment != null) {
                currentFragmentTag = currentFragment.javaClass.simpleName
                if (fragment.javaClass.simpleName == currentFragmentTag) {
                    if (!currentFragment.isAdded) {
                        manager.beginTransaction().add(R.id.fragment_container, currentFragment, currentFragment.javaClass.simpleName).commit()
                    }
                    return
                }
            }
            if (extraData != null) fragment.arguments = extraData
            val containsList = booleanArrayOf(false, false)
            for (tag in tags) {
                if (tag == fragment.javaClass.simpleName) {
                    containsList[0] = true
                    break
                }
            }
            for (f in manager.fragments) {
                if (f.javaClass.simpleName == fragment.javaClass.simpleName) {
                    containsList[1] = true
                    break
                }
            }
            val contains = containsList[0] && containsList[1]
            if (contains) {
                if (fragment is BaseFragment) {
                    fragment.requestReopen(extraData)
                }
            } else {
                if (!containsList[0]) tags.add(fragment.javaClass.simpleName)
            }
            val transaction = manager.beginTransaction()
            if (currentFragment != null) {
                var managerCurrentFragment: Fragment? = null
                for (f in manager.fragments) {
                    if (f.isVisible) {
                        managerCurrentFragment = f
                        break
                    }
                }
                if (managerCurrentFragment !== currentFragment) {
                    currentFragment = managerCurrentFragment
                }
                if (currentFragment != null) {
                    transaction.hide(currentFragment)
                }
                if (contains) {
                    transaction.show(fragment)
                } else {
                    transaction.add(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
                }
            } else {
                transaction.add(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
            }
            if (withBackStack) transaction.addToBackStack(fragment.javaClass.simpleName)
            manager.addOnBackStackChangedListener {
                for (f in manager.fragments) {
                    if (f.isVisible) {
                        currentFragmentTag = f.javaClass.simpleName
                        break
                    }
                }
            }
            transaction.commit()
            currentFragmentTag = fragment.javaClass.simpleName
        }

        fun getCurrentFragment(fragmentManager: FragmentManager): Fragment? {
            val fragments = fragmentManager.fragments
            if (isEmpty(fragments)) return null
            for (fragment in fragments) {
                if (fragment!!.isVisible) return fragment
            }
            return null
        }

        val instance: FragmentSwitcher
            get() = FragmentSwitcher()
    }
}