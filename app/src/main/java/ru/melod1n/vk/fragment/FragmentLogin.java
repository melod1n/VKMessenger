package ru.melod1n.vk.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.melod1n.vk.R;
import ru.melod1n.vk.activity.MainActivity;
import ru.melod1n.vk.api.UserConfig;
import ru.melod1n.vk.api.VKAuth;
import ru.melod1n.vk.common.AppGlobal;
import ru.melod1n.vk.current.BaseFragment;

public class FragmentLogin extends BaseFragment {

    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    public FragmentLogin(int titleRes) {
        super(titleRes);
    }

    public FragmentLogin() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);

        webView.setWebViewClient(new VKWebClient());

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        refreshLayout.setColorSchemeColors(AppGlobal.colorAccent);
        refreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            refreshLayout.setRefreshing(false);
        });

        String url = VKAuth.getUrl(UserConfig.API_ID, VKAuth.getSettings());
        webView.loadUrl(url);
    }

    private class VKWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            parseUrl(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        }
    }

    private void parseUrl(String url) {
        try {
            if (TextUtils.isEmpty(url))
                return;
            if (url.startsWith(VKAuth.redirect_url)) {
                if (!url.contains("error=")) {
                    String[] auth = VKAuth.parseRedirectUrl(url);

                    String token = auth[0];
                    int id = Integer.parseInt(auth[1]);

                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("user_id", id);

                    startActivity(intent);

                    requireActivity().finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
