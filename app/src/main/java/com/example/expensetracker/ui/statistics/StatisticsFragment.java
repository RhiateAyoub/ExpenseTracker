package com.example.expensetracker.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.data.dao.ExpenseDao;
import com.example.expensetracker.ui.expenses.MonthYearBottomSheet; // Import the bottom sheet
import com.example.expensetracker.utils.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Implement the listener from the bottom sheet
public class StatisticsFragment extends Fragment implements MonthYearBottomSheet.OnMonthSelectedListener {

    private StatisticsViewModel viewModel;
    private SessionManager sessionManager;

    private PieChart pieChart;
    private RecyclerView rvCategoryStats;
    private TextView tvSelectedMonth;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private LinearLayout emptyState;
    private LinearLayout dataState;
    private FloatingActionButton fabAddExpense;

    private CategoryStatsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        setupObservers();

        return view;
    }

    private void initViews(View view) {
        pieChart = view.findViewById(R.id.pieChart);
        rvCategoryStats = view.findViewById(R.id.rvCategoryStats);
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        emptyState = view.findViewById(R.id.emptyState);
        dataState = view.findViewById(R.id.dataState);
        fabAddExpense = view.findViewById(R.id.fabAddExpense); // Initialize FAB
    }

    private void setupRecyclerView() {
        adapter = new CategoryStatsAdapter();
        rvCategoryStats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategoryStats.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnPreviousMonth.setOnClickListener(v -> viewModel.changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> viewModel.changeMonth(1));

        // Set FAB click listener
        fabAddExpense.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_statistics_to_addExpense)
        );

        // Add click listener to show the bottom sheet
        tvSelectedMonth.setOnClickListener(v -> showMonthPicker());
    }

    private void setupObservers() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), calendar -> {
            updateMonthDisplay(calendar);
            // Trigger data loading for the new month
            viewModel.loadStatsForMonth(sessionManager.getUserId());
        });

        viewModel.getMonthlyStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null || stats.categoryTotals.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                dataState.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                dataState.setVisibility(View.VISIBLE);
                setupPieChart(stats.categoryTotals, stats.totalExpenses); // Pass total expenses
                // Pass colors from the dataset to the adapter
                if (pieChart.getData() != null && pieChart.getData().getDataSet() != null) {
                    adapter.setCategoryTotals(stats.categoryTotals, stats.totalExpenses, pieChart.getData().getDataSet().getColors());
                }
            }
        });
    }

    private void updateMonthDisplay(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("fr", "FR"));
        String monthName = sdf.format(calendar.getTime());
        tvSelectedMonth.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1));
    }

    private void setupPieChart(List<ExpenseDao.CategoryTotal> categoryTotals, double totalExpenses) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        // This line is now corrected
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT); // Make hole transparent
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getLegend().setEnabled(false);

        // Set total expenses text in the center
        pieChart.setCenterText(String.format(Locale.FRENCH, "%.2f\nMAD", totalExpenses));
        pieChart.setCenterTextSize(20f);
        pieChart.setCenterTextTypeface(getResources().getFont(R.font.inter_semibold));
        pieChart.setCenterTextColor(Color.BLACK);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (ExpenseDao.CategoryTotal total : categoryTotals) {
            entries.add(new PieEntry((float) total.total, total.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Dépenses par catégorie");
        dataSet.setSliceSpace(3f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Use predefined colors

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(0f); // Hide percentages on the chart slices

        pieChart.setData(data);
        pieChart.invalidate();
    }

    // Method to show the bottom sheet
    private void showMonthPicker() {
        Calendar currentSelection = viewModel.getSelectedMonth().getValue();
        if (currentSelection != null) {
            int year = currentSelection.get(Calendar.YEAR);
            int month = currentSelection.get(Calendar.MONTH);
            // Use the correct ViewModel's listener implementation
            MonthYearBottomSheet bottomSheet = new MonthYearBottomSheet(year, month, this);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        }
    }

    // This method is called when a month is selected in the bottom sheet
    @Override
    public void onMonthSelected(int year, int month) {
        // Now we can call the proper method in the ViewModel
        viewModel.setMonth(year, month);
    }

}
