package com.example.expensetracker.ui.budget;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.expensetracker.R;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import androidx.navigation.fragment.NavHostFragment;

public class BudgetFragment extends Fragment {

    // Header Views
    private TextView tvWelcome, tvDate;
    private ImageButton btnLogout;

    // Other Views
    private TextView tvMonthName, tvExpensesValue, tvBudgetValue, tvBalanceValue;
    private TextView tvBudgetAmount, btnModifier, btnEnregistrer, btnAnnuler;
    private TextInputEditText etBudgetInput;
    private LinearLayout displayMode, editMode;
    private ImageButton btnMonthDetails;
    private FloatingActionButton fabAddExpense;

    // ViewModel and Session
    private BudgetViewModel viewModel;
    private SessionManager sessionManager;

    private double currentBudgetAmount = 0.0;
    private double currentExpensesAmount = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        initViews(view);
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // ==================== DYNAMIC HEADER ====================
        setupDynamicHeader();

        setupClickListeners();
        setupObservers();

        // Load data for the logged-in user
        viewModel.loadData(sessionManager.getUserId());

        return view;
    }

    private void initViews(View view) {
        // Header
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvDate = view.findViewById(R.id.tvDate);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Other views
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

    /**
     * Sets the welcome message and current date in the header.
     */
    private void setupDynamicHeader() {
        // Set Welcome Message
        String firstName = sessionManager.getFirstName();
        tvWelcome.setText(String.format("Bonjour %s 👋", firstName));

        // Set Current Date
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("fr", "FR"));
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);
    }

    private void logoutUser() {
        sessionManager.logout();
        // Use the global action to navigate
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_to_loginFragment);
    }

    // ... (keep the rest of the existing methods: setupObservers, updateUI, setupClickListeners, etc.)
    private void setupObservers() {
        // Observe budget changes
        viewModel.budget.observe(getViewLifecycleOwner(), budget -> {
            currentBudgetAmount = (budget != null) ? budget.getAmount() : 0.0;
            updateUI();
        });

        // Observe expense changes
        viewModel.totalExpenses.observe(getViewLifecycleOwner(), total -> {
            currentExpensesAmount = (total != null) ? total : 0.0;
            updateUI();
        });
    }

    private void updateUI() {
        // Update month name
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", new Locale("fr", "FR"));
        String monthName = sdf.format(Calendar.getInstance().getTime());
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        tvMonthName.setText(monthName);

        // Calculate balance
        double balance = currentBudgetAmount - currentExpensesAmount;

        // Update values in the UI
        tvExpensesValue.setText(String.format(Locale.FRENCH, "%.2f", currentExpensesAmount));
        tvBudgetValue.setText(String.format(Locale.FRENCH, "%.0f", currentBudgetAmount));
        tvBalanceValue.setText(String.format(Locale.FRENCH, "%.2f", balance));
        tvBudgetAmount.setText(String.format(Locale.FRENCH, "%.0f MAD", currentBudgetAmount));

        // Color balance based on positive/negative
        int balanceColor = (balance >= 0) ? R.color.primary_green : R.color.red_strong;
        tvBalanceValue.setTextColor(getResources().getColor(balanceColor, null));
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logoutUser());
        btnModifier.setOnClickListener(v -> switchToEditMode());
        btnEnregistrer.setOnClickListener(v -> saveBudget());
        btnAnnuler.setOnClickListener(v -> switchToDisplayMode());

        btnMonthDetails.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_budget_to_monthlyHistory)
        );

        fabAddExpense.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_budget_to_addExpense)
        );
    }

    private void switchToEditMode() {
        displayMode.setVisibility(View.GONE);
        editMode.setVisibility(View.VISIBLE);
        btnModifier.setVisibility(View.GONE);
        etBudgetInput.setText(String.valueOf((int) currentBudgetAmount));
        etBudgetInput.requestFocus();
        showKeyboard(etBudgetInput);
    }

    private void switchToDisplayMode() {
        hideKeyboard();
        displayMode.setVisibility(View.VISIBLE);
        editMode.setVisibility(View.GONE);
        btnModifier.setVisibility(View.VISIBLE);
    }

    private void saveBudget() {
        String input = etBudgetInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un montant", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double newBudgetAmount = Double.parseDouble(input);
            if (newBudgetAmount < 0) {
                Toast.makeText(requireContext(), "Le budget ne peut pas être négatif", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.saveBudget(newBudgetAmount);
            hideKeyboard();
            switchToDisplayMode();
            Toast.makeText(requireContext(), "Budget mis à jour", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Montant invalide", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Keyboard Utility Methods ---
    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
