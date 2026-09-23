package com.chitrakote.panjabiwholesale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://chitrakote.com/shop/portal/app.php";
    private WebView webView;
    private ProgressBar progress;
    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            buildUi();
            openApp();
        } catch (Throwable e) {
            showFatalMessage("Unable to start app. Please check Android System WebView and internet connection.");
        }
    }

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        webView = new WebView(getApplicationContext());
        FrameLayout.LayoutParams webLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(webView, webLp);

        progress = new ProgressBar(this);
        FrameLayout.LayoutParams progressLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressLp.gravity = Gravity.CENTER;
        root.addView(progress, progressLp);

        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String scheme = u.getScheme();
                if ("tel".equalsIgnoreCase(scheme) ||
                    "mailto".equalsIgnoreCase(scheme) ||
                    "sms".equalsIgnoreCase(scheme) ||
                    "intent".equalsIgnoreCase(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, u));
                    } catch (Throwable ignored) {}
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(ProgressBar.GONE);
                android.webkit.CookieManager.getInstance().flush();
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                try {
                    if (view != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                        view.destroy();
                    }
                    recreate();
                } catch (Throwable ignored) {}
                return true;
            }
        });
    }

    private void openApp() {
        progress.setVisibility(ProgressBar.VISIBLE);
        webView.loadUrl(APP_URL);
    }

    private void showFatalMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(18);
        tv.setTextColor(Color.DKGRAY);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(48, 48, 48, 48);
        setContentView(tv);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (webView != null) {
                webView.stopLoading();
                webView.loadUrl("about:blank");
                webView.clearHistory();
                webView.removeAllViews();
                webView.destroy();
                webView = null;
            }
        } catch (Throwable ignored) {}
        super.onDestroy();
    }
}
