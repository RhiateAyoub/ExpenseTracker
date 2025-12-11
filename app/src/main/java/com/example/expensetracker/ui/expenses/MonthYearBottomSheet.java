package com.example.expensetracker.ui.expenses;

import android.os.Bundle;
import android.widget.NumberPicker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.expensetracker.R;

public class MonthYearBottomSheet extends BottomSheetDialogFragment {

    public interface OnMonthSelectedListener {
        void onMonthSelected(int year, int month);
    }

    private OnMonthSelectedListener listener;
    private int initialYear;
    private int initialMonth;

    public MonthYearBottomSheet(int year, int month, OnMonthSelectedListener listener) {
        this.initialYear = year;
        this.initialMonth = month;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_month_picker, container, false);

        NumberPicker monthPicker = view.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);

        // Mois
        final String[] months = new String[]{
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(initialMonth);

        // Année
        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(2100);
        yearPicker.setValue(initialYear);

        // Annuler
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());

        // OK
        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            listener.onMonthSelected(yearPicker.getValue(), monthPicker.getValue());
            dismiss();
        });

        return view;
    }
}
