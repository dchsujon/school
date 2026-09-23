package com.chitrakote.panjabiwholesale;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebasePushService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        PushTokenRegistrar.saveToken(this, token);
        PushTokenRegistrar.tryRegister(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String,String> data = message.getData();
        String title = data.get("title");
        String body = data.get("message");
        String link = data.get("link_url");
        int id = 0;
        try { id = Integer.parseInt(data.get("notification_id")); } catch (Throwable ignored) {}

        if (message.getNotification() != null) {
            if (title == null || title.isEmpty()) title = message.getNotification().getTitle();
            if (body == null || body.isEmpty()) body = message.getNotification().getBody();
        }

        PushNotifications.show(this, id, title, body, link);
    }
}
