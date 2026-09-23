package com.chitrakote.panjabiwholesale;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends Activity {
    private static final String BASE_URL = "https://chitrakote.com/shop";
    private static final String APP_URL = BASE_URL + "/portal/app.php";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 31;

    private WebView webView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initFirebase();
            PushNotifications.ensureChannel(this);
            buildUi();
            openFromIntentOrHome(getIntent());
        } catch (Throwable e) {
            showFatalMessage("Unable to start app. Please check Android System WebView and internet connection.");
        }
    }

    private void initFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:575784874081:android:e55e9bfc5e546a9888fa20")
                    .setApiKey("AIzaSyD2K8DzzhEuvrJM0f5cTgoc8eD_ciT_tNY")
                    .setProjectId("shop-6404e")
                    .setGcmSenderId("575784874081")
                    .setStorageBucket("shop-6404e.firebasestorage.app")
                    .build();
            FirebaseApp.initializeApp(this, options);
        }

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener((OnCompleteListener<String>) task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                PushTokenRegistrar.saveToken(this, task.getResult());
                PushTokenRegistrar.tryRegister(this);
            }
        });
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        webView = new WebView(getApplicationContext());
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

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
                    try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Throwable ignored) {}
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(ProgressBar.GONE);
                try {
                    android.webkit.CookieManager.getInstance().flush();
                    String cookie = android.webkit.CookieManager.getInstance().getCookie(BASE_URL);
                    if (cookie != null && cookie.contains("PHPSESSID")) {
                        PushTokenRegistrar.saveCookie(MainActivity.this, cookie);
                        PushTokenRegistrar.tryRegister(MainActivity.this);
                        requestNotificationPermissionIfNeeded();
                    }
                } catch (Throwable ignored) {}
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

    private void openFromIntentOrHome(Intent intent) {
        String url = null;
        if (intent != null) {
            url = intent.getStringExtra("open_url");
            if (url == null) url = intent.getStringExtra("link_url");
        }
        url = PushNotifications.safeUrl(url);
        progress.setVisibility(ProgressBar.VISIBLE);
        webView.loadUrl(url == null ? APP_URL : url);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try { openFromIntentOrHome(intent); } catch (Throwable ignored) {}
    }

    private void requestNotificationPermissionIfNeeded() {
        try {
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            }
        } catch (Throwable ignored) {}
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
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
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
