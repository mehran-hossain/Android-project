package com.example.demo3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        com.example.demo3.NotificationHelper notificationHelper = new com.example.demo3.NotificationHelper(context);
        String message = intent.getStringExtra("message");
        int id = intent.getIntExtra("id", 0);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(message);
        notificationHelper.getManager().notify(id, nb.build());
    }

}

