// MonthSummaryAdapter.java
package com.example.expensetracker.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthSummaryAdapter extends RecyclerView.Adapter<MonthSummaryAdapter.MonthViewHolder> {

    private List<MonthSummary> monthSummaries = new ArrayList<>();
    private OnMonthClickListener listener;

    public interface OnMonthClickListener {
        void onMonthClick(MonthSummary monthSummary);
    }

    public MonthSummaryAdapter(OnMonthClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month_summary, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        MonthSummary summary = monthSummaries.get(position);

        // Set month and year
        holder.tvMonthYear.setText(summary.getMonthYear());

        // Set expenses
        holder.tvExpenses.setText(String.format(Locale.FRENCH, "%.0f", summary.getExpenses()));

        // Set budget
        holder.tvBudget.setText(String.format(Locale.FRENCH, "%.0f", summary.getBudget()));

        // Set balance with color
        double balance = summary.getBalance();
        String balanceText;
        if (balance >= 0) {
            balanceText = String.format(Locale.FRENCH, "+%.0f", balance);
            holder.tvBalance.setTextColor(holder.itemView.getContext().getColor(R.color.primary_green));
        } else {
            balanceText = String.format(Locale.FRENCH, "%.0f", balance);
            holder.tvBalance.setTextColor(holder.itemView.getContext().getColor(R.color.red_strong));
        }
        holder.tvBalance.setText(balanceText);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMonthClick(summary);
            }
        });
    }

    @Override
    public int getItemCount() {
        return monthSummaries.size();
    }

    public void setMonthSummaries(List<MonthSummary> summaries) {
        this.monthSummaries = summaries;
        notifyDataSetChanged();
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthYear;
        TextView tvExpenses;
        TextView tvBudget;
        TextView tvBalance;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonthYear = itemView.findViewById(R.id.tvMonthYear);
            tvExpenses = itemView.findViewById(R.id.tvExpenses);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}