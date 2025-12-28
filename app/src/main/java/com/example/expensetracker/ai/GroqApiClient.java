package com.example.expensetracker.ai;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.expensetracker.data.model.ExpenseSummary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GroqApiClient {
    private static final String TAG = "GroqApiClient";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final String apiKey;

    public GroqApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFinancialAdvice(ExpenseSummary summary) throws Exception {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(summary);

        // ============ ADD THIS LOGGING ============
        Log.d(TAG, "=== Groq API Request Debug ===");
        Log.d(TAG, "API URL: " + GROQ_API_URL);
        Log.d(TAG, "Model: " + MODEL);
        Log.d(TAG, "API Key Length: " + (apiKey != null ? apiKey.length() : "null"));
        Log.d(TAG, "System Prompt Length: " + systemPrompt.length());
        Log.d(TAG, "User Prompt Length: " + userPrompt.length());
        Log.d(TAG, "User Prompt Content:\n" + userPrompt);
        // =========================================

        // Build JSON request
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 200);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userPrompt));

        requestBody.put("messages", messages);

        // ============ ADD THIS LOGGING ============
        Log.d(TAG, "Request Body:\n" + requestBody.toString(2));
        // =========================================

        HttpURLConnection conn = null;
        try {
            URL url = new URL(GROQ_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                br.close();

                return parseGroqResponse(response.toString());
            } else {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line.trim());
                }
                br.close();

                // ============ ENHANCED ERROR LOGGING ============
                Log.e(TAG, "========================================");
                Log.e(TAG, "Groq API Error Response Code: " + responseCode);
                Log.e(TAG, "Error Body: " + error.toString());
                Log.e(TAG, "========================================");
                // ================================================

                throw new Exception("API Error: " + responseCode + " - " + error.toString());
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String buildSystemPrompt() {
        return "You are a friendly financial advisor helping people manage their personal expenses. " +
                "Analyze the user's spending data and provide exactly 3 specific, actionable tips. " +
                "Use simple language. Keep your response under 120 words. " +
                "Focus on the most impactful changes. " +
                "Output ONLY plain text advice, no JSON, no markdown formatting.";
    }

    private String buildUserPrompt(ExpenseSummary summary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Here is my financial situation for this month:\n\n");
        prompt.append("Currency: ").append(summary.getCurrency()).append("\n");
        prompt.append("Total Monthly Expenses: ").append(String.format("%.2f", summary.getMonthlyExpenses())).append("\n");
        prompt.append("Monthly Budget Limit: ").append(String.format("%.2f", summary.getBudgetLimit())).append("\n\n");
        prompt.append("Spending by Category:\n");

        for (Map.Entry<String, Double> entry : summary.getCategoryExpenses().entrySet()) {
            prompt.append("- ").append(entry.getKey()).append(": ")
                    .append(String.format("%.2f", entry.getValue())).append("\n");
        }

        // Calculate budget status
        double budgetLeft = summary.getBudgetLimit() - summary.getMonthlyExpenses();
        if (budgetLeft < 0) {
            prompt.append("\n⚠️ I'm over budget by ").append(String.format("%.2f", Math.abs(budgetLeft))).append("\n");
        } else {
            prompt.append("\n✓ Budget remaining: ").append(String.format("%.2f", budgetLeft)).append("\n");
        }

        prompt.append("\nPlease analyze my spending and give me 3 specific tips to improve my finances.");

        return prompt.toString();
    }

    private String parseGroqResponse(String jsonResponse) throws Exception {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray choices = json.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content").trim();
            } else {
                throw new Exception("No response from AI");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse response: " + e.getMessage());
            throw new Exception("Invalid API response");
        }
    }
}