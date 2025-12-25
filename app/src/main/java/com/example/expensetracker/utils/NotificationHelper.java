package com.example.expensetracker.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.expensetracker.MainActivity;
import com.example.expensetracker.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "EXPENSE_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "Rappels de dépenses";
    private static final String CHANNEL_DESCRIPTION = "Notifications pour rappeler d'enregistrer les dépenses";
    private static final int NOTIFICATION_ID = 1; // ID unique pour cette notification

    /**
     * Crée le canal de notification. Doit être appelé une seule fois, par exemple au lancement de l'application.
     * @param context Contexte de l'application.
     */
    public static void createNotificationChannel(Context context) {
        // Le canal de notification n'est nécessaire que pour Android 8.0 (API 26) et plus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            // Enregistre le canal avec le système
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Construit et affiche une notification.
     * @param context Contexte.
     * @param title   Titre de la notification.
     * @param message Contenu de la notification.
     */
    public static void createNotification(Context context, String title, String message) {
        // Crée une intention pour ouvrir MainActivity lorsque l'utilisateur clique sur la notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Construit la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_wallet) // Assurez-vous d'avoir une icône pour la notification
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Action de clic
                .setAutoCancel(true); // La notification disparaît après le clic

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Vérifie si l'application a la permission d'afficher des notifications (obligatoire sur Android 13+)
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si la permission n'est pas accordée, ne rien faire. La demande de permission doit être gérée dans l'interface utilisateur.
            return;
        }

        // Affiche la notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
