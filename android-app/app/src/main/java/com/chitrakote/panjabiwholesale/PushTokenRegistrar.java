package com.chitrakote.panjabiwholesale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class PushTokenRegistrar {
    private static final String PREFS = "panjabi_fcm";
    private static final String REGISTER_URL = "https://chitrakote.com/shop/portal/register_push_token.php";

    private PushTokenRegistrar() {}

    public static void saveToken(Context c, String token) {
        if (token == null || token.trim().isEmpty()) return;
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("token", token).apply();
    }

    public static void saveCookie(Context c, String cookie) {
        if (cookie == null || cookie.trim().isEmpty()) return;
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("cookie", cookie).apply();
    }

    public static void tryRegister(Context context) {
        new Thread(() -> {
            try {
                SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String token = p.getString("token", "");
                String cookie = p.getString("cookie", "");
                if (token.isEmpty() || cookie.isEmpty()) return;

                String body = "device_token=" + URLEncoder.encode(token, "UTF-8")
                        + "&platform=android"
                        + "&device_name=" + URLEncoder.encode(Build.MANUFACTURER + " " + Build.MODEL, "UTF-8")
                        + "&app_version=1.0.4";

                HttpURLConnection c = (HttpURLConnection) new URL(REGISTER_URL).openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Cookie", cookie);
                c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                c.setRequestProperty("Accept", "application/json");
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.setDoOutput(true);

                try (OutputStream os = c.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
                if (c.getResponseCode() == 200) {
                    p.edit().putString("registered_token", token).apply();
                }
                c.disconnect();
            } catch (Throwable ignored) {}
        }).start();
    }
}
