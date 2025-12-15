// StatisticsFragment.java
package com.example.expensetracker.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.expensetracker.R;
import com.example.expensetracker.ui.expenses.MonthYearBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private DonutChartView donutChart;
    private TextView tvTotalAmount;
    private TextView tvSelectedMonth;
    private LinearLayout legendContainer;
    private LinearLayout categoryListContainer;
    private LinearLayout btnMonthSelector;
    private FloatingActionButton fabAddExpense;

    private Calendar currentMonth;
    private List<CategoryStatistic> allCategories = new ArrayList<>();

    // Theme colors for categories
    private int[] categoryColors = {
            0xFF22C55E, // Primary green
            0xFF3B82F6, // Blue
            0xFF10B981, // Teal
            0xFF8B5CF6, // Purple
            0xFF6B7280  // Gray for "Autre"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        setupMonthSelector();
        setupFAB();

        currentMonth = Calendar.getInstance();
        updateDisplay();
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        donutChart = view.findViewById(R.id.donutChart);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        legendContainer = view.findViewById(R.id.legendContainer);
        categoryListContainer = view.findViewById(R.id.categoryListContainer);
        btnMonthSelector = view.findViewById(R.id.btnMonthSelector);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);
    }

    private void setupMonthSelector() {
        btnMonthSelector.setOnClickListener(v -> {
            MonthYearBottomSheet sheet = new MonthYearBottomSheet(
                    currentMonth.get(Calendar.YEAR),
                    currentMonth.get(Calendar.MONTH),
                    (year, month) -> {
                        currentMonth.set(Calendar.YEAR, year);
                        currentMonth.set(Calendar.MONTH, month);
                        updateDisplay();
                        loadStatistics();
                    }
            );
            sheet.show(getParentFragmentManager(), "MonthYearPicker");
        });
    }

    private void setupFAB() {
        fabAddExpense.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_statistics_to_addExpense);
        });
    }

    private void updateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", new Locale("fr", "FR"));
        String monthYear = sdf.format(currentMonth.getTime());
        // Capitalize first letter and add period after month abbreviation
        monthYear = monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1);
        // Ensure there's a period after the abbreviated month (e.g., "Nov. 2025")
        if (!monthYear.contains(".")) {
            int spaceIndex = monthYear.indexOf(" ");
            if (spaceIndex > 0) {
                monthYear = monthYear.substring(0, spaceIndex) + "." + monthYear.substring(spaceIndex);
            }
        }
        tvSelectedMonth.setText(monthYear);
    }

    private void loadStatistics() {
        // TODO: Load actual data from database
        // For now, using dummy data
        allCategories = generateDummyData();

        if (allCategories.isEmpty()) {
            // Show empty state
            return;
        }

        // Calculate total
        double total = 0;
        for (CategoryStatistic cat : allCategories) {
            total += cat.getAmount();
        }

        // Calculate percentages
        for (CategoryStatistic cat : allCategories) {
            float percentage = (float) ((cat.getAmount() / total) * 100);
            cat.setPercentage(percentage);
        }

        // Sort by amount (descending)
        Collections.sort(allCategories, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        // Prepare chart data (top 4 + "Autre")
        List<DonutChartView.ChartSegment> chartSegments = prepareChartData(allCategories, total);

        // Update UI
        tvTotalAmount.setText(String.format(Locale.FRENCH, "%.0f", total));
        donutChart.setData(chartSegments);
        buildLegend(chartSegments);
        buildCategoryList(allCategories);
    }

    private List<DonutChartView.ChartSegment> prepareChartData(List<CategoryStatistic> categories, double total) {
        List<DonutChartView.ChartSegment> segments = new ArrayList<>();

        int topCount = Math.min(4, categories.size());
        double othersTotal = 0;

        // Add top 4 categories
        for (int i = 0; i < topCount; i++) {
            CategoryStatistic cat = categories.get(i);
            segments.add(new DonutChartView.ChartSegment(
                    cat.getCategoryName(),
                    cat.getAmount(),
                    cat.getPercentage(),
                    cat.getColor()
            ));
        }

        // Add "Autre" if there are more categories
        if (categories.size() > topCount) {
            for (int i = topCount; i < categories.size(); i++) {
                othersTotal += categories.get(i).getAmount();
            }
            float othersPercentage = (float) ((othersTotal / total) * 100);
            segments.add(new DonutChartView.ChartSegment(
                    "Autre",
                    othersTotal,
                    othersPercentage,
                    categoryColors[4] // Gray
            ));
        }

        return segments;
    }

    private void buildLegend(List<DonutChartView.ChartSegment> segments) {
        legendContainer.removeAllViews();

        for (DonutChartView.ChartSegment segment : segments) {
            View legendItem = createLegendItem(segment);
            legendContainer.addView(legendItem);
        }
    }

    private View createLegendItem(DonutChartView.ChartSegment segment) {
        LinearLayout item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(8));
        item.setLayoutParams(params);

        // Color indicator with rounded corners
        View colorBox = new View(requireContext());
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(dpToPx(12), dpToPx(12));
        boxParams.setMargins(0, 0, dpToPx(8), 0);
        colorBox.setLayoutParams(boxParams);
        colorBox.setBackgroundColor(segment.getColor());
        // Add rounded corners
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(segment.getColor());
        drawable.setCornerRadius(dpToPx(3));
        colorBox.setBackground(drawable);

        // Label
        TextView label = new TextView(requireContext());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        label.setLayoutParams(labelParams);
        label.setText(segment.getLabel());
        label.setTextSize(14);
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        label.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Percentage
        TextView percentage = new TextView(requireContext());
        percentage.setText(String.format(Locale.FRENCH, "%.0f%%", segment.getPercentage()));
        percentage.setTextSize(14);
        percentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        percentage.setTypeface(null, android.graphics.Typeface.BOLD);

        item.addView(colorBox);
        item.addView(label);
        item.addView(percentage);

        return item;
    }

    private void buildCategoryList(List<CategoryStatistic> categories) {
        categoryListContainer.removeAllViews();

        for (CategoryStatistic category : categories) {
            View categoryItem = createCategoryItem(category);
            categoryListContainer.addView(categoryItem);
        }
    }

    private View createCategoryItem(CategoryStatistic category) {
        LinearLayout item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(20));
        item.setLayoutParams(params);

        // Top row: Icon + Name + Amount
        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        topParams.setMargins(0, 0, 0, dpToPx(8));
        topRow.setLayoutParams(topParams);

        // Icon with background and rounded corners
        LinearLayout iconContainer = new LinearLayout(requireContext());
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        iconContainerParams.setMargins(0, 0, dpToPx(12), 0);
        iconContainer.setLayoutParams(iconContainerParams);
        iconContainer.setGravity(android.view.Gravity.CENTER);
        iconContainer.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        // Add rounded corners to icon background
        android.graphics.drawable.GradientDrawable iconDrawable = new android.graphics.drawable.GradientDrawable();
        iconDrawable.setColor(category.getColor());
        iconDrawable.setCornerRadius(dpToPx(8));
        iconContainer.setBackground(iconDrawable);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(category.getIconResId());
        icon.setColorFilter(Color.WHITE);
        iconContainer.addView(icon);

        // Category name
        TextView name = new TextView(requireContext());
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        name.setLayoutParams(nameParams);
        name.setText(category.getCategoryName());
        name.setTextSize(16);
        name.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        name.setTypeface(null, android.graphics.Typeface.BOLD);

        // Amount with currency
        TextView amount = new TextView(requireContext());
        amount.setText(String.format(Locale.FRENCH, "%.0f MAD", category.getAmount()));
        amount.setTextSize(16);
        amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        amount.setTypeface(null, android.graphics.Typeface.BOLD);

        topRow.addView(iconContainer);
        topRow.addView(name);
        topRow.addView(amount);

        // Progress bar with rounded corners
        ProgressBar progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(8)
        );
        progressBar.setLayoutParams(progressParams);
        progressBar.setMax(100);
        progressBar.setProgress((int) category.getPercentage());

        // Create rounded background
        android.graphics.drawable.GradientDrawable backgroundDrawable = new android.graphics.drawable.GradientDrawable();
        backgroundDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.line_gray));
        backgroundDrawable.setCornerRadius(dpToPx(4));

        // Create rounded progress (this is the colored part)
        android.graphics.drawable.GradientDrawable progressDrawable = new android.graphics.drawable.GradientDrawable();
        progressDrawable.setColor(category.getColor());
        progressDrawable.setCornerRadius(dpToPx(4)); // Rounded ends on the colored bar

        // Create scale drawable to make progress bar rounded
        android.graphics.drawable.ScaleDrawable scaleDrawable = new android.graphics.drawable.ScaleDrawable(
                progressDrawable,
                android.view.Gravity.START,
                1.0f,
                -1.0f
        );

        android.graphics.drawable.LayerDrawable layerDrawable = new android.graphics.drawable.LayerDrawable(
                new android.graphics.drawable.Drawable[]{backgroundDrawable, scaleDrawable}
        );
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);

        progressBar.setProgressDrawable(layerDrawable);

        item.addView(topRow);
        item.addView(progressBar);

        return item;
    }

    private List<CategoryStatistic> generateDummyData() {
        List<CategoryStatistic> categories = new ArrayList<>();

        categories.add(new CategoryStatistic("Transport", 617, 0, categoryColors[0], R.drawable.ic_transport));
        categories.add(new CategoryStatistic("Restauration", 333, 0, categoryColors[1], R.drawable.ic_restaurant));
        categories.add(new CategoryStatistic("Courses", 284, 0, categoryColors[2], R.drawable.ic_shopping));
        categories.add(new CategoryStatistic("Santé", 150, 0, categoryColors[3], R.drawable.ic_health));
        categories.add(new CategoryStatistic("Divertissement", 80, 0, categoryColors[4], R.drawable.ic_entertainment));

        return categories;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}