package com.example.expensetracker;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.expensetracker.utils.NotificationHelper;
import com.example.expensetracker.workers.NotificationWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Crée le canal de notification au démarrage de l'application
        NotificationHelper.createNotificationChannel(this);

        // 2. Programme les tâches de notification récurrentes
        scheduleDailyNotifications();
    }

    private void scheduleDailyNotifications() {
        // Plan pour la notification de midi
        scheduleNotificationWork("NoonNotification", 12);

        // Plan pour la notification du soir (ex: 20h)
        scheduleNotificationWork("EveningNotification", 20);
    }

    private void scheduleNotificationWork(String workTag, int targetHour) {
        // Calcule le délai initial pour que la première notification se déclenche à la bonne heure
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        // Si l'heure cible est déjà passée aujourd'hui, programmer pour demain
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        long initialDelay = target.getTimeInMillis() - now.getTimeInMillis();

        // Création de la requête de travail périodique (toutes les 24 heures)
        PeriodicWorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .addTag(workTag)
                        .build();

        // Soumet la tâche à WorkManager, en remplaçant toute tâche existante avec le même tag.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                workTag,
                ExistingPeriodicWorkPolicy.KEEP, // Garde l'ancienne si elle existe déjà
                notificationWorkRequest
        );
    }
}
