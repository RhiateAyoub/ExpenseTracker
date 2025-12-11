// AddExpenseFragment.java
package com.example.expensetracker.ui.expenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.expensetracker.R;
import com.example.expensetracker.data.model.Expense;
import com.example.expensetracker.utils.CategoryHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private TextInputEditText etPrice;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etDate;
    private TextInputEditText etNote;
    private MaterialButton btnAddExpense;

    private Calendar selectedDate;
    private CategoryHelper.Category selectedCategory;
    private List<CategoryHelper.Category> categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        initViews(view);
        setupCategoryDropdown();
        setupDatePicker();
        setupAddButton();

        // Initialize with current date
        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        return view;
    }

    private void initViews(View view) {
        etPrice = view.findViewById(R.id.etPrice);
        actvCategory = view.findViewById(R.id.actvCategory);
        etDate = view.findViewById(R.id.etDate);
        etNote = view.findViewById(R.id.etNote);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
    }

    private void setupCategoryDropdown() {
        categories = CategoryHelper.getCategories();
        CategoryHelper.CategoryAdapter adapter = new CategoryHelper.CategoryAdapter(
                requireContext(),
                categories
        );

        actvCategory.setAdapter(adapter);
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories.get(position);
        });

        // Set default category
        if (!categories.isEmpty()) {
            selectedCategory = categories.get(0);
            actvCategory.setText(selectedCategory.getName(), false);
        }
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> showDatePicker());

        // Also trigger on end icon click
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
                etDate.clearFocus();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate.getTime());
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupAddButton() {
        btnAddExpense.setOnClickListener(v -> {
            if (validateInputs()) {
                addExpense();
            }
        });
    }

    private boolean validateInputs() {
        String priceStr = etPrice.getText().toString().trim();

        if (priceStr.isEmpty()) {
            etPrice.setError("Veuillez entrer un montant");
            etPrice.requestFocus();
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Le montant doit être positif");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Montant invalide");
            etPrice.requestFocus();
            return false;
        }

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addExpense() {
        // Get values
        double amount = Double.parseDouble(etPrice.getText().toString().trim());
        String category = selectedCategory.getName();
        long date = selectedDate.getTimeInMillis();
        String note = etNote.getText().toString().trim();

        // Create expense object
        Expense expense = new Expense(0, amount, category, date, note);

        // TODO: Save to database using ViewModel
        // expenseViewModel.insert(expense);

        // Show success message
        Toast.makeText(requireContext(), "Dépense ajoutée avec succès", Toast.LENGTH_SHORT).show();

        // Navigate back to expenses list
        Navigation.findNavController(requireView()).navigateUp();
    }
}