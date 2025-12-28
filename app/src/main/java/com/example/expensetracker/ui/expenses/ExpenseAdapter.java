// ExpenseAdapter.java
package com.example.expensetracker.ui.expenses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.data.entity.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private final OnExpenseActionsListener listener;
    private int expandedPosition = -1; // To track which item is showing actions

    // Updated listener interface
    public interface OnExpenseActionsListener {
        void onExpenseClick(Expense expense);
        void onDeleteClick(Expense expense);
        void onModifyClick(Expense expense);
    }

    public ExpenseAdapter(OnExpenseActionsListener listener) {
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

        // Determine if the current item is the one with actions revealed
        final boolean isExpanded = position == expandedPosition;
        holder.contentLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.actionsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

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
        holder.tvExpenseAmount.setText(String.format(Locale.FRENCH, "-%.2f MAD", expense.getAmount()));

        // Click listener to hide actions when tapping outside
        holder.itemView.setOnClickListener(v -> {
            if (isExpanded) {
                // If actions are visible, a normal click should hide them
                expandedPosition = -1;
                notifyItemChanged(position);
            } else {
                // Otherwise, perform the normal click action
                if (listener != null) {
                    listener.onExpenseClick(expense);
                }
            }
        });

        // Long press listener to reveal the actions
        holder.itemView.setOnLongClickListener(v -> {
            // Hide previously expanded item
            if (expandedPosition != -1) {
                notifyItemChanged(expandedPosition);
            }
            // Expand the new item
            expandedPosition = holder.getAdapterPosition();
            notifyItemChanged(expandedPosition);
            return true; // Consume the long click event
        });

        // Listeners for the new action buttons
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(expense);
                expandedPosition = -1; // Hide actions after click
            }
        });

        holder.btnModify.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModifyClick(expense);
                expandedPosition = -1; // Hide actions after click
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
            case "éducation":
                return R.drawable.ic_education;
            case "vêtements":
                return R.drawable.ic_clothing;
            case "snacks":
                return R.drawable.ic_snack;
            case "abonnements":
                return R.drawable.ic_subscription;
            case "logement":
                return R.drawable.ic_home;
            case "autre":
            default:
                return R.drawable.ic_other;
        }
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contentLayout, actionsLayout, dateHeader;
        ImageButton btnDelete, btnModify;
        TextView tvExpenseDate;
        TextView tvExpenseDayName;
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvExpenseNote;
        TextView tvExpenseAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            contentLayout = itemView.findViewById(R.id.contentLayout);
            actionsLayout = itemView.findViewById(R.id.actionsLayout);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnModify = itemView.findViewById(R.id.btnModify);
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