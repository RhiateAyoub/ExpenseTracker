// ============================================================================
// FILE: FinancialAdvisorFragment.java
// Location: app/src/main/java/com/example/expensetracker/ui/advisor/FinancialAdvisorFragment.java
// ============================================================================

package com.example.expensetracker.ui.advisor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.BuildConfig;
import com.example.expensetracker.R;
import com.example.expensetracker.ai.AIAdvisorService;
import com.example.expensetracker.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for AI Financial Advisor feature
 * Displays personalized financial advice based on user's spending
 */
public class FinancialAdvisorFragment extends Fragment {

    private TextView adviceTextView;
    private Button generateButton;
    private Button refreshButton;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private View adviceCard;

    private AIAdvisorService aiService;
    private ExecutorService executorService;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_financial_advisor, container, false);

        // Initialize views
        adviceTextView = view.findViewById(R.id.adviceTextView);
        generateButton = view.findViewById(R.id.generateButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        adviceCard = view.findViewById(R.id.adviceCard);

        // Initialize services
        sessionManager = new SessionManager(requireContext());
        String groqApiKey = BuildConfig.GROQ_API_KEY;
        aiService = new AIAdvisorService(requireContext(), groqApiKey);
        executorService = Executors.newSingleThreadExecutor();

        // Set up buttons
        generateButton.setOnClickListener(v -> generateAdvice(false));
        refreshButton.setOnClickListener(v -> generateAdvice(true));

        return view;
    }

    private void generateAdvice(boolean forceRefresh) {
        int userId = sessionManager.getUserId();

        // Clear cache if force refresh
        if (forceRefresh) {
            aiService.clearCache(userId);
        }

        // Show loading state
        showLoadingState();

        // Run on background thread
        executorService.execute(() -> {
            String advice = aiService.generateAdvice(userId);

            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                showAdviceState(advice);
            });
        });
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        refreshButton.setEnabled(false);
        adviceTextView.setText("🤔 Analyzing your financial data...");
        emptyStateLayout.setVisibility(View.GONE);
        adviceCard.setVisibility(View.VISIBLE);
    }

    private void showAdviceState(String advice) {
        progressBar.setVisibility(View.GONE);
        generateButton.setEnabled(true);
        refreshButton.setEnabled(true);
        adviceTextView.setText(advice);

        // Show refresh button after first generation
        generateButton.setVisibility(View.GONE);
        refreshButton.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        adviceCard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}