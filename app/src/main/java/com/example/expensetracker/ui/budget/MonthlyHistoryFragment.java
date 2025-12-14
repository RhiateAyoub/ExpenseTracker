// MonthlyHistoryFragment.java
package com.example.expensetracker.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthlyHistoryFragment extends Fragment implements MonthSummaryAdapter.OnMonthClickListener {

    private RecyclerView rvMonthlyHistory;
    private LinearLayout emptyState;
    private ImageButton btnBack;
    private MonthSummaryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_history, container, false);

        initViews(view);
        setupRecyclerView();
        setupBackButton();
        loadMonthlyData();

        return view;
    }

    private void initViews(View view) {
        rvMonthlyHistory = view.findViewById(R.id.rvMonthlyHistory);
        emptyState = view.findViewById(R.id.emptyState);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new MonthSummaryAdapter(this);
        rvMonthlyHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMonthlyHistory.setAdapter(adapter);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void loadMonthlyData() {
        // TODO: Load actual data from database
        // For now, using dummy data for demonstration

        List<MonthSummary> summaries = generateDummyData();

        if (summaries.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvMonthlyHistory.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvMonthlyHistory.setVisibility(View.VISIBLE);
            adapter.setMonthSummaries(summaries);
        }
    }

    private List<MonthSummary> generateDummyData() {
        // Generate dummy data for past 6 months
        List<MonthSummary> summaries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Current month
        summaries.add(new MonthSummary(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                925,
                1000
        ));

        // Previous months
        for (int i = 1; i <= 5; i++) {
            calendar.add(Calendar.MONTH, -1);

            // Generate random-ish data for demonstration
            double budget = 1000;
            double expenses = 800 + (Math.random() * 400); // 800-1200

            summaries.add(new MonthSummary(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    expenses,
                    budget
            ));
        }

        return summaries;
    }

    @Override
    public void onMonthClick(MonthSummary monthSummary) {
        // TODO: Navigate to detailed month view showing all expenses for that month
        // For now, just show a toast
        String message = String.format("Détails pour %s", monthSummary.getMonthYear());
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Future implementation:
        // Bundle bundle = new Bundle();
        // bundle.putInt("year", monthSummary.getYear());
        // bundle.putInt("month", monthSummary.getMonth());
        // Navigation.findNavController(requireView())
        //     .navigate(R.id.action_monthlyHistory_to_monthDetails, bundle);
    }
}