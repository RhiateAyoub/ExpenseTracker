package com.example.expensetracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.expensetracker.R;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // Header
    private TextView tvWelcome, tvDate;

    // Sections
    private LinearLayout noBudgetSection, positiveSection, negativeSection;

    // Card 1: Expenses of the month
    private TextView tvExpensesAmount, tvExpensesPercentage;

    // Card 2: Most expensive category
    private TextView tvExpensiveCategory, tvCategoryPercentage;

    // Card 3: Predicted balance
    private View balanceCard; // Use View to target the card background
    private TextView tvBalanceAmount, tvBalancePercentageText;

    private HomeViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupDynamicHeader();
        setupObservers();

        // Load all data for the home screen
        viewModel.loadHomeData(sessionManager.getUserId());

        FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_addExpense)
        );

        return view;
    }

    private void initViews(View view) {
        // Header
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvDate = view.findViewById(R.id.tvDate);

        // Sections
        noBudgetSection = view.findViewById(R.id.noBudgetSection);
        positiveSection = view.findViewById(R.id.positiveSection);
        negativeSection = view.findViewById(R.id.negativeSection);

        // Card 1 Views (home_card_expenses_month.xml)
        View expensesCard = view.findViewById(R.id.card_expenses_month_wrapper);
        tvExpensesAmount = expensesCard.findViewById(R.id.tvExpensesAmount);
        tvExpensesPercentage = expensesCard.findViewById(R.id.tvExpensesPercentage);

        // Card 2 Views (home_card_expensive_category.xml)
        View categoryCard = view.findViewById(R.id.card_expensive_category_wrapper);
        tvExpensiveCategory = categoryCard.findViewById(R.id.tvExpensiveCategory);
        tvCategoryPercentage = categoryCard.findViewById(R.id.tvCategoryPercentage);

        // Card 3 Views (home_card_balance.xml)
        View balanceCardWrapper = view.findViewById(R.id.card_balance_wrapper);
        balanceCard = balanceCardWrapper.findViewById(R.id.balanceCard); // The inner LinearLayout with the background
        tvBalanceAmount = balanceCardWrapper.findViewById(R.id.tvBalanceAmount);
        tvBalancePercentageText = balanceCardWrapper.findViewById(R.id.tvBalancePercentage);
    }

    private void setupDynamicHeader() {
        String firstName = sessionManager.getFirstName();
        tvWelcome.setText(String.format("Bonjour %s 👋", firstName));

        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.FRANCE);
        tvDate.setText(sdf.format(Calendar.getInstance().getTime()));
    }

    private void setupObservers() {
        viewModel.getHomeData().observe(getViewLifecycleOwner(), this::updateUI);
    }

    /**
     * Main method to update the entire UI based on the HomeData object.
     * @param data The consolidated data for the home screen.
     */
    private void updateUI(HomeViewModel.HomeData data) {
        if (data == null) return;

        // Update UI based on whether a budget is set
        if (!data.hasBudget) {
            noBudgetSection.setVisibility(View.VISIBLE);
            positiveSection.setVisibility(View.GONE);
            negativeSection.setVisibility(View.GONE);
        } else {
            noBudgetSection.setVisibility(View.GONE);
            // Show positive or negative message based on prediction
            positiveSection.setVisibility(data.isOnTrack ? View.VISIBLE : View.GONE);
            negativeSection.setVisibility(data.isOnTrack ? View.GONE : View.VISIBLE);
        }

        // Update Card 1: Expenses of the month
        tvExpensesAmount.setText(String.format(Locale.FRENCH, "%.0f MAD", data.currentMonthExpenses));
        tvExpensesPercentage.setText(String.format(Locale.FRENCH, "%.0f%% du budget mensuel", data.expensePercentageOfBudget));

        // Update Card 2: Most expensive category
        if (data.mostExpensiveCategory != null) {
            tvExpensiveCategory.setText(data.mostExpensiveCategory.category);
            tvCategoryPercentage.setText(String.format(Locale.FRENCH, "%.0f%% des dépenses", data.expensiveCategoryPercentage));
        } else {
            tvExpensiveCategory.setText("N/A");
            tvCategoryPercentage.setText("Aucune dépense ce mois-ci");
        }

        // Update Card 3: Predicted balance
        updateBalanceCard(data.predictedBalance, data.predictedBalancePercentage, data.isOnTrack);
    }

    /**
     * Updates the predicted balance card with the correct colors, text, and sign.
     */
    private void updateBalanceCard(double predictedBalance, double percentage, boolean isOnTrack) {
        if (isOnTrack) {
            balanceCard.setBackgroundResource(R.drawable.card_positive);
            tvBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
            tvBalancePercentageText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
            tvBalanceAmount.setText(String.format(Locale.FRENCH, "+%.0f MAD", predictedBalance));
            tvBalancePercentageText.setText(String.format(Locale.FRENCH, "+%.0f%% du budget mensuel", percentage));
        } else {
            balanceCard.setBackgroundResource(R.drawable.card_negative);
            tvBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_strong));
            tvBalancePercentageText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_strong));
            tvBalanceAmount.setText(String.format(Locale.FRENCH, "%.0f MAD", predictedBalance)); // No plus sign for negative
            tvBalancePercentageText.setText(String.format(Locale.FRENCH, "-%.0f%% du budget mensuel", percentage));
        }
    }
}
