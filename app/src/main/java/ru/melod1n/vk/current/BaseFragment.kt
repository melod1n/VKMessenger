package ru.melod1n.vk.current

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment {
    private var title: String? = null
    private var titleRes = -1
    private var requestReopen = false
    private var reopenBundle: Bundle? = null

    constructor(titleRes: Int) {
        this.titleRes = titleRes
    }

    constructor(title: String?) {
        this.title = title
    }

    constructor() {
        title = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (TextUtils.isEmpty(title) && titleRes > 0) {
            title = getString(titleRes)
        }
    }

    override fun onResume() {
        super.onResume()

        if (titleRes > 0) {
            requireActivity().setTitle(titleRes)
        } else {
            requireActivity().title = title
        }
        if (requestReopen) {
            requestReopen = false
            onReopen(reopenBundle)
        }
    }

    fun getTitle(): CharSequence? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
        requireActivity().title = title
    }

    fun setTitleRes(titleRes: Int) {
        this.titleRes = titleRes
        requireActivity().setTitle(titleRes)
    }

    fun getTitleRes(): Int {
        return titleRes
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            onResume()
        }
    }

    fun onReopen(bundle: Bundle?) {}

    fun requestReopen(bundle: Bundle?) {
        reopenBundle = bundle
        requestReopen = true
    }
}