package com.example.expensetracker.ui.statistics;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.repository.StatisticsRepository;
import com.example.expensetracker.utils.DateUtils;

import java.util.Calendar;

public class StatisticsViewModel extends AndroidViewModel {

    private final StatisticsRepository repository;
    private final MutableLiveData<Calendar> selectedMonth = new MutableLiveData<>();

    // LiveData to hold the fetched statistics for the UI to observe
    private final MutableLiveData<MonthlyStats> monthlyStats = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new StatisticsRepository(application);
        // Default to the current month when the ViewModel is created
        selectedMonth.setValue(Calendar.getInstance());
    }

    // ==================== LIVE DATA GETTERS ====================

    public LiveData<Calendar> getSelectedMonth() {
        return selectedMonth;
    }

    public LiveData<MonthlyStats> getMonthlyStats() {
        return monthlyStats;
    }

    // ==================== ACTIONS ====================

    /**
     * Loads statistics for the currently selected month and user.
     */
    public void loadStatsForMonth(int userId) {
        Calendar calendar = selectedMonth.getValue();
        if (calendar == null) return;

        long startDate = DateUtils.getStartOfMonth(calendar);
        long endDate = DateUtils.getEndOfMonth(calendar);

        repository.getMonthlyStats(userId, startDate, endDate, stats -> {
            monthlyStats.postValue(stats);
        });
    }

    /**
     * Changes the selected month by a given amount (e.g., -1 for previous, 1 for next).
     */
    public void changeMonth(int amount) {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            Calendar newMonth = (Calendar) current.clone();
            newMonth.add(Calendar.MONTH, amount);
            selectedMonth.setValue(newMonth);
        }
    }
}
