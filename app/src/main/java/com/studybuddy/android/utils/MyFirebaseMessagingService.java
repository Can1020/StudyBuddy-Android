package com.studybuddy.android.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.studybuddy.android.R;
import com.studybuddy.android.ui.activities.ChatRoomActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String senderId = remoteMessage.getData().get("senderId");
        String message = remoteMessage.getData().get("message");

        sendNotification(senderId, message);
    }

    private void sendNotification(String senderId, String message) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("userId", senderId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("chat_notifications", "Chat Messages", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_notifications")
                .setSmallIcon(R.drawable.bg_message)
                .setContentTitle("New Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(0, builder.build());
    }
}
