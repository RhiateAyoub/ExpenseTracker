package com.example.expensetracker.ui.expenses;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Implement the listener from the bottom sheet
public class ExpenseFragment extends Fragment implements ExpenseAdapter.OnExpenseActionsListener, MonthYearBottomSheet.OnMonthSelectedListener {

    // Header Views
    private TextView tvWelcome, tvDate;
    private ImageButton btnLogout;

    // Other Views
    private TextView tvSelectedMonth;
    private TextView tvMonthTotal;
    private RecyclerView rvExpenses;
    private LinearLayout emptyState;
    private ImageButton btnPreviousMonth;
    private ImageButton btnNextMonth;
    private FloatingActionButton fabAddExpense;

    private ExpenseAdapter adapter;
    private ExpenseViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        initViews(view);
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // ==================== DYNAMIC HEADER ====================
        setupDynamicHeader();

        setupRecyclerView();
        setupMonthNavigation();
        setupObservers();

        return view;
    }

    private void initViews(View view) {
        // Header
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvDate = view.findViewById(R.id.tvDate);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Other Views
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        tvMonthTotal = view.findViewById(R.id.tvMonthTotal);
        rvExpenses = view.findViewById(R.id.rvExpenses);
        emptyState = view.findViewById(R.id.emptyState);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);

        btnLogout.setOnClickListener(v -> logoutUser());
        fabAddExpense.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_expenses_to_addExpense)
        );

        // Add click listener to the month text to show the bottom sheet
        tvSelectedMonth.setOnClickListener(v -> showMonthPicker());
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

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(this);
        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenses.setAdapter(adapter);
    }

    private void setupMonthNavigation() {
        btnPreviousMonth.setOnClickListener(v -> viewModel.changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> viewModel.changeMonth(1));
    }

    private void setupObservers() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), calendar -> {
            updateMonthDisplay(calendar);
            loadExpensesForMonth(calendar);
        });
    }

    private void updateMonthDisplay(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("fr", "FR"));
        String monthYear = sdf.format(calendar.getTime());
        monthYear = monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1);
        tvSelectedMonth.setText(monthYear);
    }

    private void loadExpensesForMonth(Calendar calendar) {
        int userId = sessionManager.getUserId();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Remove any previous observer to avoid multiple updates
        viewModel.getExpensesForMonth(userId, year, month).removeObservers(getViewLifecycleOwner());

        viewModel.getExpensesForMonth(userId, year, month).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvExpenses.setVisibility(View.GONE);
                tvMonthTotal.setText("Total: 0 MAD");
            } else {
                emptyState.setVisibility(View.GONE);
                rvExpenses.setVisibility(View.VISIBLE);
                adapter.setExpenses(expenses);
                calculateAndDisplayTotal(expenses);
            }
        });
    }

    private void calculateAndDisplayTotal(List<Expense> expenses) {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        tvMonthTotal.setText(String.format(Locale.FRENCH, "Total: %.0f MAD", total));
    }

    // Method to show the bottom sheet
    private void showMonthPicker() {
        Calendar currentSelection = viewModel.getSelectedMonth().getValue();
        if (currentSelection != null) {
            int year = currentSelection.get(Calendar.YEAR);
            int month = currentSelection.get(Calendar.MONTH);
            MonthYearBottomSheet bottomSheet = new MonthYearBottomSheet(year, month, this);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        }
    }

    @Override
    public void onExpenseClick(Expense expense) {
        // Here you can handle editing or deleting an expense
        Toast.makeText(getContext(), "Dépense: " + expense.getCategory() + " de " + expense.getAmount() + " MAD", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Expense expense) {
        // Show a confirmation dialog before deleting
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la dépense")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette dépense ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    viewModel.deleteExpense(expense);
                    Toast.makeText(getContext(), "Dépense supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    @Override
    public void onModifyClick(Expense expense) {
        // Navigate to EditExpenseFragment, passing the expense ID
        ExpenseFragmentDirections.ActionExpensesToEditExpense action =
                ExpenseFragmentDirections.actionExpensesToEditExpense(expense.getId());
        Navigation.findNavController(requireView()).navigate(action);
    }

    // This method is called when a month is selected in the bottom sheet
    @Override
    public void onMonthSelected(int year, int month) {
        viewModel.setMonth(year, month);
    }
}
