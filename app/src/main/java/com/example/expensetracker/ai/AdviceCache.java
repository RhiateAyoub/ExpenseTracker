package com.example.expensetracker.ai;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Caches AI advice to avoid unnecessary API calls
 * Cache expires after 24 hours
 */
public class AdviceCache {
    private static final String PREF_NAME = "ai_advice_cache";
    private static final String KEY_PREFIX_ADVICE = "advice_";
    private static final String KEY_PREFIX_TIMESTAMP = "timestamp_";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours

    private final SharedPreferences prefs;

    public AdviceCache(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getCachedAdvice(int userId) {
        String timestampKey = KEY_PREFIX_TIMESTAMP + userId;
        String adviceKey = KEY_PREFIX_ADVICE + userId;

        long timestamp = prefs.getLong(timestampKey, 0);
        long now = System.currentTimeMillis();

        if (now - timestamp < CACHE_DURATION) {
            return prefs.getString(adviceKey, null);
        }
        return null; // Cache expired
    }

    public void cacheAdvice(int userId, String advice) {
        String timestampKey = KEY_PREFIX_TIMESTAMP + userId;
        String adviceKey = KEY_PREFIX_ADVICE + userId;

        prefs.edit()
                .putString(adviceKey, advice)
                .putLong(timestampKey, System.currentTimeMillis())
                .apply();
    }

    public void clearCache(int userId) {
        String timestampKey = KEY_PREFIX_TIMESTAMP + userId;
        String adviceKey = KEY_PREFIX_ADVICE + userId;

        prefs.edit()
                .remove(adviceKey)
                .remove(timestampKey)
                .apply();
    }

    public void clearAllCache() {
        prefs.edit().clear().apply();
    }
}