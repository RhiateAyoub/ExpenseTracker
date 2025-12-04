package com.example.expensetracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R; // <-- IMPORTANT : import de R

// HomeFragment.java - Add this method to your HomeFragment class

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class HomeFragment extends Fragment {

    private LinearLayout balanceCard;
    private LinearLayout positiveSection;
    private LinearLayout negativeSection;
    private TextView tvBalanceAmount;
    private TextView tvBalancePercentage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        balanceCard = view.findViewById(R.id.balanceCard);
        positiveSection = view.findViewById(R.id.positiveSection);
        negativeSection = view.findViewById(R.id.negativeSection);
        tvBalanceAmount = view.findViewById(R.id.tvBalanceAmount);
        tvBalancePercentage = view.findViewById(R.id.tvBalancePercentage);

        // Example: Update balance state based on your data
        double predictedBalance = -100; // This would come from your ViewModel
        updateBalanceState(predictedBalance);

        return view;
    }

    /**
     * Updates the UI based on whether the predicted balance is positive or negative
     * @param balance The predicted balance amount
     */
    private void updateBalanceState(double balance) {
        if (balance >= 0) {
            // POSITIVE balance - green theme
            balanceCard.setBackgroundResource(R.drawable.card_positive);
            tvBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));
            tvBalancePercentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green));

            // Show positive message, hide negative
            positiveSection.setVisibility(View.VISIBLE);
            negativeSection.setVisibility(View.GONE);

            // Update text with positive format
            tvBalanceAmount.setText(String.format("+%.0f MAD", balance));
        } else {
            // NEGATIVE balance - red theme
            balanceCard.setBackgroundResource(R.drawable.card_negative);
            tvBalanceAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_strong));
            tvBalancePercentage.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_strong));

            // Show negative message, hide positive
            positiveSection.setVisibility(View.GONE);
            negativeSection.setVisibility(View.VISIBLE);

            // Update text with negative format
            tvBalanceAmount.setText(String.format("%.0f MAD", balance));
        }
    }

    /**
     * Alternative method if you want to update with percentage
     */
    private void updateBalanceWithPercentage(double balance, double percentage) {
        updateBalanceState(balance);

        // Update percentage text
        if (balance >= 0) {
            tvBalancePercentage.setText(String.format("+%.0f%% du budget mensuel", percentage));
        } else {
            tvBalancePercentage.setText(String.format("%.0f%% du budget mensuel", percentage));
        }
    }
}
