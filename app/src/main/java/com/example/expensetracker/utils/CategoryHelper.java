// CategoryHelper.java
package com.example.expensetracker.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.expensetracker.R;
import java.util.ArrayList;
import java.util.List;

public class CategoryHelper {

    public static class Category {
        private String name;
        private int iconResId;

        public Category(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }

        public String getName() {
            return name;
        }

        public int getIconResId() {
            return iconResId;
        }

        @Override
        public String toString() {
            return name; // This is what gets displayed when selected
        }
    }

    // Get all available categories
    public static List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Transport", R.drawable.ic_transport));
        categories.add(new Category("Courses", R.drawable.ic_shopping));
        categories.add(new Category("Restauration", R.drawable.ic_restaurant));
        categories.add(new Category("Santé", R.drawable.ic_health));
        categories.add(new Category("Divertissement", R.drawable.ic_entertainment));
        categories.add(new Category("Logement", R.drawable.ic_home));
        categories.add(new Category("Autre", R.drawable.ic_other));
        categories.add(new Category("Éducation", R.drawable.ic_education));
        categories.add(new Category("Vêtements", R.drawable.ic_clothing));
        categories.add(new Category("Snacks", R.drawable.ic_snack));
        categories.add(new Category("Abonnements", R.drawable.ic_subscription));
        // Add more categories as needed
        return categories;
    }

    // Custom adapter for category dropdown with icons
    public static class CategoryAdapter extends ArrayAdapter<Category> {

        public CategoryAdapter(@NonNull Context context, @NonNull List<Category> categories) {
            super(context, 0, categories);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        private View createView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_category_dropdown, parent, false);
            }

            Category category = getItem(position);

            ImageView icon = convertView.findViewById(R.id.ivCategoryIcon);
            TextView name = convertView.findViewById(R.id.tvCategoryName);

            if (category != null) {
                icon.setImageResource(category.getIconResId());
                name.setText(category.getName());
            }

            return convertView;
        }
    }
}