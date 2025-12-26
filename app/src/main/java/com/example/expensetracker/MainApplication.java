package com.example.expensetracker;

import android.app.Application;
import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.expensetracker.utils.NotificationHelper;
import com.example.expensetracker.workers.NotificationWorker;
import com.example.expensetracker.workers.SyncWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Crée le canal de notification au démarrage de l'application
        NotificationHelper.createNotificationChannel(this);

        // 2. Programme les tâches de notification récurrentes
        scheduleReminderNotifications();

        // NOUVEAU : Planifiez la synchronisation
        scheduleSyncWorker(this);
    }

    private void scheduleReminderNotifications() {
        // Plan pour la notification de midi
        scheduleNotificationWork("NoonNotification", 12);

        // Plan pour la notification du soir (ex: 20h)
        scheduleNotificationWork("EveningNotification", 20);
    }

    // NOUVELLE méthode pour la synchronisation
    public static void scheduleSyncWorker(Context context) {
        // Définir des contraintes : la tâche ne s'exécutera que si le réseau est connecté
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // On crée une tâche unique. WorkManager est intelligent et ne l'exécutera pas
        // plusieurs fois si elle est déjà en attente.
        OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .addTag("SyncWork")
                .build();

        WorkManager.getInstance(context).enqueue(syncWorkRequest);
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
