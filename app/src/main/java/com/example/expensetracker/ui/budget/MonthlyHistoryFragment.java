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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.utils.SessionManager;

public class MonthlyHistoryFragment extends Fragment implements MonthSummaryAdapter.OnMonthClickListener {

    private RecyclerView rvMonthlyHistory;
    private LinearLayout emptyState;
    private ImageButton btnBack;

    private MonthSummaryAdapter adapter;
    private MonthlyHistoryViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_history, container, false);

        initViews(view);
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(MonthlyHistoryViewModel.class);

        setupRecyclerView();
        setupBackButton();
        setupObservers();

        // Load the data
        viewModel.loadHistory(sessionManager.getUserId());

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
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupObservers() {
        viewModel.monthlySummaries.observe(getViewLifecycleOwner(), summaries -> {
            if (summaries == null || summaries.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvMonthlyHistory.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                rvMonthlyHistory.setVisibility(View.VISIBLE);
                adapter.setMonthSummaries(summaries);
            }
        });
    }

    @Override
    public void onMonthClick(MonthSummary monthSummary) {
        // When a user clicks a month, navigate to the expenses screen for that month.
        // We'll pass the year and month to the ExpenseFragment.
        // Note: This requires the ExpenseFragment to handle these arguments, which we'll do next.

        Toast.makeText(requireContext(), "Affichage des dépenses pour " + monthSummary.getMonthYear(), Toast.LENGTH_SHORT).show();

        // TODO: Implement navigation to ExpenseFragment with specific month args
        // For now, we'll just show the toast.
    }
}

