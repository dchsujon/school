package com.chitrakote.panjabiwholesale;

import android.app.JobInfo;
import android.app.JobScheduler;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class NotificationSync {
    public static final String PREFS = "panjabi_notification_sync";
    public static final String CHANNEL_ID = "dealer_updates";
    private static final String BASE_URL = "https://chitrakote.com/shop";
    private static final String API_URL = BASE_URL + "/portal/api_notifications.php";
    private static final int JOB_ID = 4103;

    private NotificationSync() {}

    public static void saveSessionCookie(Context context, String cookie) {
        if (cookie == null || cookie.trim().isEmpty()) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString("session_cookie", cookie).apply();
    }

    public static void syncSession(Context context) {
        new Thread(() -> {
            try {
                SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String cookie = p.getString("session_cookie", "");
                if (cookie.isEmpty()) return;

                JSONObject root = request(cookie, 0, true);
                if (!root.optBoolean("ok", false)) return;

                int clientId = root.optInt("client_id", 0);
                int latest = root.optInt("latest_id", 0);
                int savedClient = p.getInt("client_id", 0);

                SharedPreferences.Editor e = p.edit();
                if (clientId > 0 && clientId != savedClient) {
                    e.putInt("client_id", clientId);
                    e.putInt("last_notification_id", latest);
                } else if (!p.getBoolean("bootstrapped", false)) {
                    e.putInt("last_notification_id", latest);
                }
                e.putBoolean("bootstrapped", true).apply();
            } catch (Throwable ignored) {}
        }).start();
    }

    public static void poll(Context context, boolean forceBootstrap) {
        new Thread(() -> {
            try {
                SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String cookie = p.getString("session_cookie", "");
                if (cookie.isEmpty()) return;

                if (forceBootstrap || !p.getBoolean("bootstrapped", false)) {
                    JSONObject boot = request(cookie, 0, true);
                    if (!boot.optBoolean("ok", false)) return;
                    p.edit()
                     .putInt("client_id", boot.optInt("client_id", 0))
                     .putInt("last_notification_id", boot.optInt("latest_id", 0))
                     .putBoolean("bootstrapped", true)
                     .apply();
                    return;
                }

                int after = p.getInt("last_notification_id", 0);
                JSONObject root = request(cookie, after, false);
                if (!root.optBoolean("ok", false)) return;

                int clientId = root.optInt("client_id", 0);
                int savedClient = p.getInt("client_id", 0);
                if (savedClient != 0 && clientId != 0 && clientId != savedClient) {
                    p.edit()
                     .putInt("client_id", clientId)
                     .putInt("last_notification_id", root.optInt("latest_id", 0))
                     .apply();
                    return;
                }

                JSONArray items = root.optJSONArray("items");
                int max = after;
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject n = items.getJSONObject(i);
                        int id = n.optInt("id", 0);
                        if (id > max) max = id;
                        showNotification(
                                context,
                                id,
                                n.optString("title", "Dealer update"),
                                n.optString("message", ""),
                                n.optString("link_url", "notifications.php")
                        );
                    }
                }
                int latest = root.optInt("latest_id", max);
                p.edit().putInt("last_notification_id", Math.max(max, latest)).apply();
            } catch (Throwable ignored) {}
        }).start();
    }

    private static JSONObject request(String cookie, int after, boolean bootstrap) throws Exception {
        String u = API_URL + "?after=" + after + (bootstrap ? "&bootstrap=1" : "");
        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("Cookie", cookie);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Cache-Control", "no-cache");
        c.setConnectTimeout(10_000);
        c.setReadTimeout(10_000);
        int code = c.getResponseCode();
        if (code != 200) return new JSONObject().put("ok", false);

        BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder b = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) b.append(line);
        r.close();
        return new JSONObject(b.toString());
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "Orders, Delivery & Dealer Notices",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                ch.setDescription("Order status, delivery tracking, payment reminders and dealer notices.");
                nm.createNotificationChannel(ch);
            }
        }
    }

    private static void showNotification(Context context, int id, String title, String message, String link) {
        ensureChannel(context);

        String openUrl = trustedUrl(link);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_url", openUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);

        b.setSmallIcon(android.R.drawable.ic_dialog_info)
         .setContentTitle(title)
         .setContentText(message)
         .setStyle(new Notification.BigTextStyle().bigText(message))
         .setAutoCancel(true)
         .setContentIntent(pi);

        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(id > 0 ? id : (int) (System.currentTimeMillis() % Integer.MAX_VALUE), b.build());
        } catch (SecurityException ignored) {}
    }

    private static String trustedUrl(String link) {
        if (link == null || link.trim().isEmpty()) return BASE_URL + "/portal/notifications.php";
        String l = link.trim();
        if (l.startsWith(BASE_URL + "/")) return l;
        if (l.startsWith("http://") || l.startsWith("https://") || l.startsWith("//"))
            return BASE_URL + "/portal/notifications.php";
        while (l.startsWith("/")) l = l.substring(1);
        if (l.startsWith("portal/")) return BASE_URL + "/" + l;
        return BASE_URL + "/portal/" + l;
    }

    public static void scheduleBackground(Context context) {
        try {
            JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            ComponentName cn = new ComponentName(context, NotificationJob.class);
            JobInfo info = new JobInfo.Builder(JOB_ID, cn)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(15 * 60 * 1000L)
                    .build();
            js.schedule(info);
        } catch (Throwable ignored) {}
    }
}
