// ExpenseAdapter.java
package com.example.expensetracker.ui.expenses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.data.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        // Check if we need to show date header
        boolean showDateHeader = false;
        if (position == 0) {
            showDateHeader = true;
        } else {
            Expense previousExpense = expenses.get(position - 1);
            // Show header if date is different from previous item
            if (!isSameDay(expense.getDate(), previousExpense.getDate())) {
                showDateHeader = true;
            }
        }

        if (showDateHeader) {
            holder.dateHeader.setVisibility(View.VISIBLE);

            // Format date: "28 Novembre"
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", new Locale("fr", "FR"));
            holder.tvExpenseDate.setText(dateFormat.format(expense.getDate()));

            // Format day name: "Samedi"
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("fr", "FR"));
            String dayName = dayFormat.format(expense.getDate());
            // Capitalize first letter
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            holder.tvExpenseDayName.setText(dayName);
        } else {
            holder.dateHeader.setVisibility(View.GONE);
        }

        // Set category icon
        holder.ivCategoryIcon.setImageResource(getCategoryIcon(expense.getCategory()));

        // Set category name
        holder.tvCategoryName.setText(expense.getCategory());

        // Set note (if available)
        if (expense.getNote() != null && !expense.getNote().isEmpty()) {
            holder.tvExpenseNote.setVisibility(View.VISIBLE);
            holder.tvExpenseNote.setText(expense.getNote());
        } else {
            holder.tvExpenseNote.setVisibility(View.GONE);
        }

        // Set amount
        holder.tvExpenseAmount.setText(String.format(Locale.FRENCH, "-%.0f MAD", expense.getAmount()));

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    private boolean isSameDay(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        cal2.setTimeInMillis(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private int getCategoryIcon(String category) {
        // Map category names to drawable resources
        switch (category.toLowerCase()) {
            case "transport":
                return R.drawable.ic_transport;
            case "courses":
                return R.drawable.ic_shopping;
            case "restauration":
                return R.drawable.ic_restaurant;
            case "santé":
                return R.drawable.ic_health;
            case "divertissement":
                return R.drawable.ic_entertainment;
            default:
                return R.drawable.ic_other;
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        LinearLayout dateHeader;
        TextView tvExpenseDate;
        TextView tvExpenseDayName;
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvExpenseNote;
        TextView tvExpenseAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            dateHeader = itemView.findViewById(R.id.dateHeader);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            tvExpenseDayName = itemView.findViewById(R.id.tvExpenseDayName);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvExpenseNote = itemView.findViewById(R.id.tvExpenseNote);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
        }
    }
}