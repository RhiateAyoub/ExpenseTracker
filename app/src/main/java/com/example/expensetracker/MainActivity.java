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
                    // L'utilisateur a refusé.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ==================== STEP 1: SPLASH SCREEN ====================
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Demande la permission pour les notifications
        askNotificationPermission();

        // ==================== INITIALIZATION =================
        sessionManager = new SessionManager(this);

        // ATTENTION : L'ID dans activity_main.xml est "bottomNavigation"
        bottomNavigation = findViewById(R.id.bottomNavigation);

        content = findViewById(android.R.id.content);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // ==================== STEP 2: DYNAMIC START DESTINATION ======
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        if (sessionManager.isLoggedIn()) {
            navGraph.setStartDestination(R.id.homeFragment);
        } else {
            navGraph.setStartDestination(R.id.loginFragment);
        }
        navController.setGraph(navGraph, savedInstanceState);

        // ==================== STEP 3: SETUP BOTTOM NAVIGATION & VISIBILITY =======
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // C'est ICI que se trouve la correction : UN SEUL LISTENER
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Liste des fragments où on veut CACHER la barre (Auth screens)
            if (destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.registerFragment ||
                    destination.getId() == R.id.forgotPasswordFragment) { // Ajout du fragment oublié

                bottomNavigation.setVisibility(View.GONE); // Cacher
            } else {
                bottomNavigation.setVisibility(View.VISIBLE); // Afficher (Home, Stats, etc.)
            }
        });

        // ==================== SPLASH SCREEN EXIT ANIMATION CONTROL ============
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
