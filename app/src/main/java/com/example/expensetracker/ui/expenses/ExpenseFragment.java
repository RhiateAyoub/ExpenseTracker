// ExpensesFragment.java
package com.example.expensetracker.ui.expenses;

import android.app.DatePickerDialog;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.data.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private TextView tvSelectedMonth;
    private TextView tvMonthTotal;
    private RecyclerView rvExpenses;
    private LinearLayout emptyState;
    private ImageButton btnPreviousMonth;
    private ImageButton btnNextMonth;
    private LinearLayout btnMonthPicker;
    private FloatingActionButton fabAddExpense;

    private ExpenseAdapter adapter;
    private Calendar currentMonth;
    private List<Expense> allExpenses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        initViews(view);
        setupRecyclerView();
        setupMonthNavigation();

        // Initialize with current month
        currentMonth = Calendar.getInstance();
        updateMonthDisplay();
        loadExpenses();

        return view;
    }

    private void initViews(View view) {
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        tvMonthTotal = view.findViewById(R.id.tvMonthTotal);
        rvExpenses = view.findViewById(R.id.rvExpenses);
        emptyState = view.findViewById(R.id.emptyState);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnMonthPicker = view.findViewById(R.id.btnMonthPicker);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);

        fabAddExpense.setOnClickListener(v -> {
            // Navigate to Add Expense fragment
            Navigation.findNavController(v).navigate(R.id.action_expenses_to_addExpense);
        });
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(this);
        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenses.setAdapter(adapter);
    }

    private void setupMonthNavigation() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadExpenses();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadExpenses();
        });

        btnMonthPicker.setOnClickListener(v -> showMonthYearPicker());
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("fr", "FR"));
        String monthYear = sdf.format(currentMonth.getTime());
        // Capitalize first letter
        monthYear = monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1);
        tvSelectedMonth.setText(monthYear);
    }

    private void showMonthYearPicker() {
        MonthYearBottomSheet sheet = new MonthYearBottomSheet(
                currentMonth.get(Calendar.YEAR),
                currentMonth.get(Calendar.MONTH),
                (year, month) -> {
                    currentMonth.set(Calendar.YEAR, year);
                    currentMonth.set(Calendar.MONTH, month);
                    updateMonthDisplay();
                    loadExpenses();
                }
        );
        sheet.show(getParentFragmentManager(), "MonthYearPicker");
    }

    private void loadExpenses() {
        // TODO: Load expenses from database for the selected month
        // For now, using dummy data
        List<Expense> monthExpenses = getExpensesForMonth();

        if (monthExpenses.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvExpenses.setVisibility(View.GONE);
            tvMonthTotal.setText("Total: 0 MAD");
        } else {
            emptyState.setVisibility(View.GONE);
            rvExpenses.setVisibility(View.VISIBLE);
            adapter.setExpenses(monthExpenses);

            // Calculate total
            double total = 0;
            for (Expense expense : monthExpenses) {
                total += expense.getAmount();
            }
            tvMonthTotal.setText(String.format(Locale.FRENCH, "Total: %.0f MAD", total));
        }
    }

    private List<Expense> getExpensesForMonth() {
        // TODO: Filter expenses from database by selected month
        // This is dummy data for demonstration
        List<Expense> expenses = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), 28);

        expenses.add(new Expense(1, 16, "Transport", cal.getTimeInMillis(), "2x Taxi"));
        expenses.add(new Expense(2, 115, "Courses", cal.getTimeInMillis(), "Dinde, Pain, Tomates"));
        expenses.add(new Expense(3, 40, "Restauration", cal.getTimeInMillis(), "Pizza"));

        cal.set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), 27);
        expenses.add(new Expense(4, 50, "Santé", cal.getTimeInMillis(), "Doliprane, Rhumix"));
        expenses.add(new Expense(5, 30, "Divertissement", cal.getTimeInMillis(), "Match de foot"));

        return expenses;
    }

    @Override
    public void onExpenseClick(Expense expense) {
        // TODO: Handle expense item click (edit/delete)
    }
}