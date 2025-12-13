// BudgetFragment.java
package com.example.expensetracker.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.expensetracker.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private TextView tvMonthName;
    private TextView tvExpensesValue;
    private TextView tvBudgetValue;
    private TextView tvBalanceValue;
    private TextView tvBudgetAmount;
    private TextView btnModifier;
    private TextView btnEnregistrer;
    private TextView btnAnnuler;
    private TextInputEditText etBudgetInput;
    private LinearLayout displayMode;
    private LinearLayout editMode;
    private ImageButton btnMonthDetails;
    private FloatingActionButton fabAddExpense;

    private double currentBudget = 1000; // Default budget
    private double currentExpenses = 925; // This should come from database

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        initViews(view);
        setupClickListeners();
        updateDisplay();

        return view;
    }

    private void initViews(View view) {
        tvMonthName = view.findViewById(R.id.tvMonthName);
        tvExpensesValue = view.findViewById(R.id.tvExpensesValue);
        tvBudgetValue = view.findViewById(R.id.tvBudgetValue);
        tvBalanceValue = view.findViewById(R.id.tvBalanceValue);
        tvBudgetAmount = view.findViewById(R.id.tvBudgetAmount);
        btnModifier = view.findViewById(R.id.btnModifier);
        btnEnregistrer = view.findViewById(R.id.btnEnregistrer);
        btnAnnuler = view.findViewById(R.id.btnAnnuler);
        etBudgetInput = view.findViewById(R.id.etBudgetInput);
        displayMode = view.findViewById(R.id.displayMode);
        editMode = view.findViewById(R.id.editMode);
        btnMonthDetails = view.findViewById(R.id.btnMonthDetails);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);
    }

    private void setupClickListeners() {
        // Modify button - switch to edit mode
        btnModifier.setOnClickListener(v -> {
            displayMode.setVisibility(View.GONE);
            editMode.setVisibility(View.VISIBLE);
            btnModifier.setVisibility(View.GONE);
            etBudgetInput.setText(String.valueOf((int) currentBudget));
            etBudgetInput.requestFocus();
        });

        // Save button - save and switch to display mode
        btnEnregistrer.setOnClickListener(v -> {
            String input = etBudgetInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez entrer un montant", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                currentBudget = Double.parseDouble(input);
                if (currentBudget <= 0) {
                    Toast.makeText(requireContext(), "Le budget doit être positif", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Save to database
                // budgetViewModel.updateBudget(currentBudget);

                updateDisplay();
                displayMode.setVisibility(View.VISIBLE);
                editMode.setVisibility(View.GONE);
                btnModifier.setVisibility(View.VISIBLE);

                Toast.makeText(requireContext(), "Budget mis à jour", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Montant invalide", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button - switch back to display mode
        btnAnnuler.setOnClickListener(v -> {
            displayMode.setVisibility(View.VISIBLE);
            editMode.setVisibility(View.GONE);
            btnModifier.setVisibility(View.VISIBLE);
        });

        // Month details button (optional - navigate somewhere)
        btnMonthDetails.setOnClickListener(v -> {
            // TODO: Navigate to detailed monthly view or do nothing
            Toast.makeText(requireContext(), "Détails du mois", Toast.LENGTH_SHORT).show();
        });

        // FAB - navigate to add expense
        fabAddExpense.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addExpenseFragment);
        });
    }

    private void updateDisplay() {
        // Update month name
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", new Locale("fr", "FR"));
        String monthName = sdf.format(Calendar.getInstance().getTime());
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        tvMonthName.setText(monthName);

        // TODO: Load actual expenses from database
        // For now using dummy data
        currentExpenses = 925;

        // Calculate balance
        double balance = currentBudget - currentExpenses;

        // Update values
        tvExpensesValue.setText(String.format(Locale.FRENCH, "%.0f", currentExpenses));
        tvBudgetValue.setText(String.format(Locale.FRENCH, "%.0f", currentBudget));
        tvBalanceValue.setText(String.format(Locale.FRENCH, "%.0f", balance));
        tvBudgetAmount.setText(String.format(Locale.FRENCH, "%.0f MAD", currentBudget));

        // Color balance based on positive/negative
        if (balance >= 0) {
            tvBalanceValue.setTextColor(getResources().getColor(R.color.primary_green, null));
        } else {
            tvBalanceValue.setTextColor(getResources().getColor(R.color.red_strong, null));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        updateDisplay();
    }
}