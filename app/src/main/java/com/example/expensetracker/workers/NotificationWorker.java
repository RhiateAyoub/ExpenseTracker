package com.example.expensetracker.workers;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.expensetracker.R;
import com.example.expensetracker.utils.NotificationHelper;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Le message peut être personnalisé en fonction de l'heure
        String title = "N'oubliez pas vos dépenses !";
        String message = "Avez-vous pensé à noter toutes vos dépenses d'aujourd'hui ?";

        // Utilise le helper pour créer et afficher la notification
        NotificationHelper.createNotification(getApplicationContext(), title, message);

        // Indique que la tâche a réussi
        return Result.success();
    }
}
