package com.example.expensetracker.ui.expenses;

import android.app.AlertDialog;
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
import com.example.expensetracker.utils.SessionManager;
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
    private ImageButton btnBack;

    private Calendar selectedDate;
    private CategoryHelper.Category selectedCategory;
    private List<CategoryHelper.Category> categories;

    private ExpenseViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        initViews(view);
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        sessionManager = new SessionManager(requireContext());

        setupBackButton();
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
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            boolean hasData = !etPrice.getText().toString().isEmpty() || !etNote.getText().toString().isEmpty();

            if (hasData) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Abandonner les modifications ?")
                        .setMessage("Les données non enregistrées seront perdues.")
                        .setPositiveButton("Abandonner", (dialog, which) -> Navigation.findNavController(v).navigateUp())
                        .setNegativeButton("Annuler", null)
                        .show();
            } else {
                Navigation.findNavController(v).navigateUp();
            }
        });
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
        DatePickerDialog dialog = new DatePickerDialog(
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
            return false;
        }
        if (selectedCategory == null) {
            Toast.makeText(getContext(), "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addExpense() {
        double amount = Double.parseDouble(etPrice.getText().toString().trim());
        String category = selectedCategory.getName();
        long date = selectedDate.getTimeInMillis();
        String note = etNote.getText().toString().trim();
        int userId = sessionManager.getUserId();

        Expense expense = new Expense(userId, amount, category, date, note);
        viewModel.addExpense(expense);

        Toast.makeText(getContext(), "Dépense ajoutée avec succès!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }
}
