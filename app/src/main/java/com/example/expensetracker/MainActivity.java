package com.example.expensetracker;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ==================== INITIALIZATION ====================
        sessionManager = new SessionManager(this);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // ==================== DYNAMIC START DESTINATION ====================
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

        if (sessionManager.isLoggedIn()) {
            // User is logged in, start at Home
            navGraph.setStartDestination(R.id.homeFragment);
        } else {
            // User is not logged in, start at Login
            navGraph.setStartDestination(R.id.loginFragment);
        }
        navController.setGraph(navGraph);

        // ==================== BOTTOM NAVIGATION SETUP ====================
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // Show/hide bottom navigation based on the current screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registerFragment) {
                bottomNavigation.setVisibility(View.GONE); // Hide on auth screens
            } else {
                bottomNavigation.setVisibility(View.VISIBLE); // Show on all other screens
            }
        });
    }

    // Optional but recommended: Handle Up button correctly
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
