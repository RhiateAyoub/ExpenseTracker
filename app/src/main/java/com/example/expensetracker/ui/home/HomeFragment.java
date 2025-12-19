package com.example.expensetracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.core.content.ContextCompat;

import com.example.expensetracker.R;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private LinearLayout balanceCard;
    private LinearLayout positiveSection;
    private LinearLayout negativeSection;
    private TextView tvBalanceAmount;
    private TextView tvBalancePercentage;

    private HomeViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ==================== INIT ====================
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Initialize views
        balanceCard = view.findViewById(R.id.balanceCard);
        positiveSection = view.findViewById(R.id.positiveSection);
        negativeSection = view.findViewById(R.id.negativeSection);
        tvBalanceAmount = view.findViewById(R.id.tvBalanceAmount);
        tvBalancePercentage = view.findViewById(R.id.tvBalancePercentage);

        // ==================== OBSERVERS ====================
        viewModel.getPredictedBalance().observe(getViewLifecycleOwner(), balance ->
                updateBalanceState(balance)
        );

        viewModel.getBalancePercentage().observe(getViewLifecycleOwner(), percentage ->
                updateBalanceWithPercentage(
                        viewModel.getPredictedBalance().getValue(),
                        percentage
                )
        );

        // Load real data from DB
        int userId = sessionManager.getUserId();
        viewModel.loadHomeData(userId);

        // ==================== FAB ====================
        FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_home_to_addExpense)
        );

        return view;
    }

    /**
     * Updates the UI based on whether the predicted balance is positive or negative
     */
    private void updateBalanceState(double balance) {
        if (balance >= 0) {
            balanceCard.setBackgroundResource(R.drawable.card_positive);
            tvBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
            tvBalancePercentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));

            positiveSection.setVisibility(View.VISIBLE);
            negativeSection.setVisibility(View.GONE);

            tvBalanceAmount.setText(String.format("+%.0f MAD", balance));
        } else {
            balanceCard.setBackgroundResource(R.drawable.card_negative);
            tvBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_strong));
            tvBalancePercentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_strong));

            positiveSection.setVisibility(View.GONE);
            negativeSection.setVisibility(View.VISIBLE);

            tvBalanceAmount.setText(String.format("%.0f MAD", balance));
        }
    }

    /**
     * Updates percentage text only (keeps existing UI logic)
     */
    private void updateBalanceWithPercentage(double balance, double percentage) {
        if (balance >= 0) {
            tvBalancePercentage.setText(String.format("+%.0f%% du budget mensuel", percentage));
        } else {
            tvBalancePercentage.setText(String.format("%.0f%% du budget mensuel", percentage));
        }
    }
}
