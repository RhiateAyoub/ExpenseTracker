package com.example.expensetracker.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.example.expensetracker.MainActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This must be called before super.onCreate()
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Immediately start MainActivity and finish this one.
        // MainActivity is now responsible for handling the splash screen exit animation.
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish SplashActivity so the user can't go back to it
    }
}
