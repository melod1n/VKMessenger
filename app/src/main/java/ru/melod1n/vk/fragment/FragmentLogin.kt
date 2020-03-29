package ru.melod1n.vk.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.fragment_login.*
import ru.melod1n.vk.R
import ru.melod1n.vk.activity.MainActivity
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKAuth
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.current.BaseFragment

class FragmentLogin : BaseFragment {

    constructor(titleRes: Int) : super(titleRes)
    constructor() : super()

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.fragment_login)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareSettings()
        prepareRefreshLayout()
        val url = VKAuth.getUrl(UserConfig.API_ID, VKAuth.settings)
        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun prepareSettings() {
        webView.settings.javaScriptEnabled = true
        webView.clearCache(true)
        webView.webViewClient = VKWebClient()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    private fun prepareRefreshLayout() {
        swipeRefreshLayout.apply {
            setColorSchemeColors(AppGlobal.colorAccent)
            setOnRefreshListener {
                webView!!.reload()
                isRefreshing = false
            }
        }
    }

    private inner class VKWebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            requireView().findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
            view.visibility = View.GONE
            parseUrl(url)
        }

        override fun onPageFinished(wview: WebView, url: String) {
            super.onPageFinished(wview, url)
            if (view == null) return

            requireView().findViewById<View>(R.id.progressBar).visibility = View.GONE
            wview.visibility = View.VISIBLE
        }
    }

    private fun parseUrl(url: String) {
        try {
            if (TextUtils.isEmpty(url)) return
            if (url.startsWith(VKAuth.redirect_url)) {
                if (!url.contains("error=")) {
                    val auth = VKAuth.parseRedirectUrl(url)
                    val token = auth[0]
                    val id = auth[1].toInt()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("user_id", id)

                    requireActivity().finishAffinity()
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}