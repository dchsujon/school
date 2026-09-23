package com.chitrakote.panjabiwholesale;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class PushNotifications {
    public static final String CHANNEL_ID = "dealer_updates";
    private static final String BASE_URL = "https://chitrakote.com/shop";

    private PushNotifications() {}

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "Orders, Delivery & Dealer Notices",
                        NotificationManager.IMPORTANCE_HIGH
                );
                ch.setDescription("Order status, delivery tracking, payment reminders and dealer notices.");
                ch.enableVibration(true);
                ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(ch);
            }
        }
    }

    public static void show(Context context, int id, String title, String body, String linkUrl) {
        ensureChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_url", safeUrl(linkUrl));
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
         .setContentTitle(title == null || title.isEmpty() ? "Panjabi Wholesale" : title)
         .setContentText(body == null ? "" : body)
         .setStyle(new Notification.BigTextStyle().bigText(body == null ? "" : body))
         .setAutoCancel(true)
         .setContentIntent(pi)
         .setPriority(Notification.PRIORITY_HIGH)
         .setDefaults(Notification.DEFAULT_ALL)
         .setVisibility(Notification.VISIBILITY_PUBLIC)
         .setCategory(Notification.CATEGORY_MESSAGE);

        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(id > 0 ? id : (int)(System.currentTimeMillis() % Integer.MAX_VALUE), b.build());
        } catch (SecurityException ignored) {}
    }

    public static String safeUrl(String link) {
        if (link == null || link.trim().isEmpty()) return BASE_URL + "/portal/notifications.php";
        String l = link.trim();
        if (l.startsWith(BASE_URL + "/")) return l;
        if (l.startsWith("http://") || l.startsWith("https://") || l.startsWith("//"))
            return BASE_URL + "/portal/notifications.php";
        while (l.startsWith("/")) l = l.substring(1);
        if (l.startsWith("portal/")) return BASE_URL + "/" + l;
        return BASE_URL + "/portal/" + l;
    }
}
