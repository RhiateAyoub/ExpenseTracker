package com.example.expensetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private NavController navController;
    private BottomNavigationView bottomNavigation;
    private View content;

    private boolean isReady = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // La permission est accordée.
                } else {
                    // L'utilisateur a refusé. Vous pouvez afficher un message expliquant pourquoi la notif est utile.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Demande la permission pour les notifications
        askNotificationPermission();

        sessionManager = new SessionManager(this);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        content = findViewById(android.R.id.content);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        if (sessionManager.isLoggedIn()) {
            navGraph.setStartDestination(R.id.homeFragment);
        } else {
            navGraph.setStartDestination(R.id.loginFragment);
        }
        navController.setGraph(navGraph, savedInstanceState);

        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (isReady) {
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        }
                        return false;
                    }
                });

        isReady = true;

        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // ============================================================
        // CHANGE #1: Update this listener to include advisorFragment
        // ============================================================
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isAuthScreen = destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.registerFragment;

            bottomNavigation.setVisibility(isAuthScreen ? View.GONE : View.VISIBLE);
        });
        // ============================================================
        // That's it! The advisor tab navigation works automatically
        // because of NavigationUI.setupWithNavController() above
        // ============================================================
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}