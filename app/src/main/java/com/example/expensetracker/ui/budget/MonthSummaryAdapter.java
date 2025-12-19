package com.example.expensetracker.ui.budget;

import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthSummaryAdapter extends RecyclerView.Adapter<MonthSummaryAdapter.ViewHolder> {

    private List<MonthSummary> summaries = new ArrayList<>();
    private final OnMonthClickListener listener;

    public interface OnMonthClickListener {
        void onMonthClick(MonthSummary monthSummary);
    }

    public MonthSummaryAdapter(OnMonthClickListener listener) {
        this.listener = listener;
    }

    public void setMonthSummaries(List<MonthSummary> summaries) {
        this.summaries = summaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthSummary summary = summaries.get(position);
        holder.bind(summary, listener);
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMonthYear;
        private final TextView tvBalance;
        private final TextView tvExpenses;
        private final TextView tvBudget;

        ViewHolder(View view) {
            super(view);
            tvMonthYear = view.findViewById(R.id.tvMonthYear);
            tvBalance = view.findViewById(R.id.tvBalance);
            tvExpenses = view.findViewById(R.id.tvExpenses);
            tvBudget = view.findViewById(R.id.tvBudget);
        }

        void bind(MonthSummary summary, OnMonthClickListener listener) {
            tvMonthYear.setText(summary.getMonthYear());
            tvExpenses.setText(String.format(Locale.FRENCH, "%.0f MAD", summary.getExpenses()));
            tvBudget.setText(String.format(Locale.FRENCH, "%.0f MAD", summary.getBudget()));

            double balance = summary.getBalance();
            tvBalance.setText(String.format(Locale.FRENCH, "%.0f MAD", balance));

            int colorRes = (balance >= 0) ? R.color.primary_green : R.color.red_strong;
            tvBalance.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));

            itemView.setOnClickListener(v -> listener.onMonthClick(summary));
        }
    }
}
