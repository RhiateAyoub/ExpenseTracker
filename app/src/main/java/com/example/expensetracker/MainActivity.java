package com.example.expensetracker;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen; // <-- IMPORT THIS
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ==================== STEP 1: INSTALL SPLASH SCREEN ====================
        // This MUST be called before super.onCreate()
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ==================== INITIALIZATION (no changes here) =================
        sessionManager = new SessionManager(this);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        content = findViewById(android.R.id.content);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // ==================== DYNAMIC START DESTINATION (no changes here) ======
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        if (sessionManager.isLoggedIn()) {
            navGraph.setStartDestination(R.id.homeFragment);
        } else {
            navGraph.setStartDestination(R.id.loginFragment);
        }
        navController.setGraph(navGraph, savedInstanceState);

        // ==================== SPLASH SCREEN EXIT ANIMATION CONTROL ============
        // The pre-draw listener is now more important than ever.
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // The logic remains the same, but now we set the isReady flag
                        // once the destination is known.
                        if (isReady) {
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        }
                        return false;
                    }
                });

        // Set the app to "ready" AFTER the navigation graph is set.
        // This signals the pre-draw listener that it's okay to draw the UI.
        isReady = true;

        // ==================== BOTTOM NAVIGATION SETUP (no changes here) =======
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Updated this to remove SplashActivity and just check auth screens
            boolean isAuthScreen = destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.registerFragment;

            bottomNavigation.setVisibility(isAuthScreen ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
