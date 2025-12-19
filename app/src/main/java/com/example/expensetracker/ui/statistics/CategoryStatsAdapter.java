package com.example.expensetracker.ui.statistics;

import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.data.dao.ExpenseDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryStatsAdapter extends RecyclerView.Adapter<CategoryStatsAdapter.ViewHolder> {

    private List<ExpenseDao.CategoryTotal> categoryTotals = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private double totalExpenses = 0.0;

    public void setCategoryTotals(List<ExpenseDao.CategoryTotal> totals, double totalExpenses, List<Integer> colors) {
        this.categoryTotals = totals;
        this.totalExpenses = totalExpenses;
        this.colors = colors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseDao.CategoryTotal item = categoryTotals.get(position);
        // Get the color for this position, cycling through if necessary
        int color = colors.get(position % colors.size());
        holder.bind(item, totalExpenses, color);
    }

    @Override
    public int getItemCount() {
        return categoryTotals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryName;
        private final TextView tvCategoryTotal;
        private final TextView tvCategoryPercentage;
        private final ProgressBar progressBar;

        ViewHolder(View view) {
            super(view);
            tvCategoryName = view.findViewById(R.id.tvCategoryName);
            tvCategoryTotal = view.findViewById(R.id.tvCategoryTotal);
            tvCategoryPercentage = view.findViewById(R.id.tvCategoryPercentage);
            progressBar = view.findViewById(R.id.progressBar);
        }

        void bind(ExpenseDao.CategoryTotal item, double totalExpenses, int color) {
            tvCategoryName.setText(item.category);
            tvCategoryTotal.setText(String.format(Locale.FRENCH, "%.0f MAD", item.total));

            // Set the color of the progress bar
            LayerDrawable progressDrawable = (LayerDrawable) progressBar.getProgressDrawable();
            progressDrawable.findDrawableByLayerId(android.R.id.progress).setColorFilter(color, PorterDuff.Mode.SRC_IN);

            if (totalExpenses > 0) {
                int percentage = (int) ((item.total / totalExpenses) * 100);
                tvCategoryPercentage.setText(String.format(Locale.FRENCH, "%d%%", percentage));
                progressBar.setProgress(percentage);
            } else {
                tvCategoryPercentage.setText("0%");
                progressBar.setProgress(0);
            }
        }
    }
}
