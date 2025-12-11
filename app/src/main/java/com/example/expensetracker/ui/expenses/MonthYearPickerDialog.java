package com.example.expensetracker.ui.expenses;

import android.app.Dialog;
import android.content.Context;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.example.expensetracker.R;

public class MonthYearPickerDialog extends Dialog {

    private NumberPicker monthPicker;
    private NumberPicker yearPicker;

    public interface OnDateSetListener {
        void onDateSet(int year, int month);
    }

    public MonthYearPickerDialog(@NonNull Context context, int initialYear, int initialMonth,
                                 OnDateSetListener listener) {
        super(context);
        setContentView(R.layout.dialog_month_year_picker);

        // APPLY YOUR ROUNDED BACKGROUND TO WHOLE DIALOG
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);
        }

        monthPicker = findViewById(R.id.monthPicker);
        yearPicker = findViewById(R.id.yearPicker);

        // Mois
        final String[] monthNames = new String[]{
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(monthNames);
        monthPicker.setValue(initialMonth);

        // Années
        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(2100);
        yearPicker.setValue(initialYear);

        // Bouton OK
        findViewById(R.id.btnOk).setOnClickListener(v -> {
            listener.onDateSet(yearPicker.getValue(), monthPicker.getValue());
            dismiss();
        });

        // Bouton Annuler
        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
    }
}
