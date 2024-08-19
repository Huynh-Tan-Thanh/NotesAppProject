package com.droiddevhub.notesapp.Model;
import android.util.Log;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.droiddevhub.notesapp.R;

public class NotificationHelper {

    // Channel ID for notifications
    private static final String CHANNEL_ID = "your_channel_id";
    private static final String CHANNEL_NAME = "Reminder Notifications";
    private static final int NOTIFICATION_ID = 1001; // Unique ID for the notification

    // Method to create the notification channel
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for reminder notifications");

            // Set the vibration pattern
            long[] vibrationPattern = {0, 1000, 500, 1000}; // Vibration pattern: wait, vibrate, wait, vibrate
            channel.setVibrationPattern(vibrationPattern);
            channel.enableVibration(true);

            // Set the default sound
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            channel.setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Method to trigger a notification
    @SuppressLint("MissingPermission")
    public static void triggerNotification(Context context, String noteTitle, String noteContent) {
        // Create notification channel if necessary
        createNotificationChannel(context);

        // Create an intent to open AlarmReceiver Activity when the notification is clicked
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("note_title", noteTitle);
        intent.putExtra("note_content", noteContent);

        // Create a PendingIntent to be triggered when the notification is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ensure this is a valid drawable resource (preferably a PNG image)
                .setContentTitle(noteTitle) // Set title from the note
                .setContentText(noteContent) // Set content from the note
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Set the PendingIntent to be triggered when the notification is clicked
                .setAutoCancel(true) // Automatically remove the notification when clicked
                .setVibrate(new long[]{0, 1000, 500, 1000}) // Vibration pattern: wait, vibrate, wait, vibrate
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)); // Set sound for the notification

        // Get the NotificationManager and notify
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d("NotificationHelper", "Notification triggered.");
    }
}
