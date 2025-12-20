package com.example.expensetracker.ui.expenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.expensetracker.R;
import com.example.expensetracker.data.entity.Expense;
import com.example.expensetracker.utils.CategoryHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditExpenseFragment extends Fragment {

    private TextInputEditText etPrice;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etDate;
    private TextInputEditText etNote;
    private MaterialButton btnUpdateExpense;
    private ImageButton btnBack;

    private Calendar selectedDate = Calendar.getInstance();
    private CategoryHelper.Category selectedCategory;
    private List<CategoryHelper.Category> categories;

    private ExpenseViewModel viewModel;
    private Expense expenseToEdit;
    private int expenseId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_expense, container, false);

        initViews(view);
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);

        // Get expense ID from arguments
        if (getArguments() != null) {
            expenseId = getArguments().getInt("expenseId", -1);
        }

        setupBackButton();
        setupCategoryDropdown();
        setupDatePicker();
        setupUpdateButton();

        // Observe the expense data
        viewModel.getExpenseById(expenseId).observe(getViewLifecycleOwner(), expense -> {
            if (expense != null) {
                expenseToEdit = expense;
                populateFields();
            }
        });

        return view;
    }

    private void initViews(View view) {
        etPrice = view.findViewById(R.id.etPrice);
        actvCategory = view.findViewById(R.id.actvCategory);
        etDate = view.findViewById(R.id.etDate);
        etNote = view.findViewById(R.id.etNote);
        btnUpdateExpense = view.findViewById(R.id.btnUpdateExpense);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void populateFields() {
        etPrice.setText(String.valueOf(expenseToEdit.getAmount()));
        etNote.setText(expenseToEdit.getNote());

        // Set date
        selectedDate.setTimeInMillis(expenseToEdit.getDate());
        updateDateDisplay();

        // Set category
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equals(expenseToEdit.getCategory())) {
                selectedCategory = categories.get(i);
                actvCategory.setText(selectedCategory.getName(), false);
                break;
            }
        }
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupCategoryDropdown() {
        categories = CategoryHelper.getCategories();
        CategoryHelper.CategoryAdapter adapter = new CategoryHelper.CategoryAdapter(requireContext(), categories);
        actvCategory.setAdapter(adapter);
        actvCategory.setOnItemClickListener((parent, view, position, id) -> selectedCategory = categories.get(position));
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupUpdateButton() {
        btnUpdateExpense.setOnClickListener(v -> {
            if (validateInputs()) {
                updateExpense();
            }
        });
    }

    private boolean validateInputs() {
        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Veuillez entrer un montant");
            return false;
        }
        if (selectedCategory == null) {
            Toast.makeText(getContext(), "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateExpense() {
        double amount = Double.parseDouble(etPrice.getText().toString().trim());
        String category = selectedCategory.getName();
        long date = selectedDate.getTimeInMillis();
        String note = etNote.getText().toString().trim();

        // Update the existing expense object
        expenseToEdit.setAmount(amount);
        expenseToEdit.setCategory(category);
        expenseToEdit.setDate(date);
        expenseToEdit.setNote(note);

        viewModel.updateExpense(expenseToEdit);

        Toast.makeText(getContext(), "Dépense mise à jour !", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }
}
