package com.example.financialstory;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.financialstory.models.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private TextView storyText;
    private PieChart categoryPieChart;
    private BarChart monthlyBarChart;
    private ProgressBar loadingProgressBar;
    private TextView noDataText;

    private FirebaseFirestore db;
    private String userId = "default_user"; // Static user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Financial Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        storyText = findViewById(R.id.story_text);
        categoryPieChart = findViewById(R.id.category_pie_chart);
        monthlyBarChart = findViewById(R.id.monthly_bar_chart);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        noDataText = findViewById(R.id.no_data_text);

        // Load data
        loadData();
    }

    private void loadData() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        noDataText.setVisibility(View.GONE);

        // Load the latest financial story
        db.collection("users").document(userId)
                .collection("stories")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String story = document.getString("content");
                            runOnUiThread(() -> storyText.setText(story));
                        }
                    } else {
                        runOnUiThread(() -> storyText.setText("No financial story available yet. Upload a statement to generate insights."));
                    }
                });

        // Load the latest transactions
        db.collection("users").document(userId)
                .collection("statements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("transactions")) {
                                List<Map<String, Object>> transactionMaps = (List<Map<String, Object>>) document.get("transactions");

                                if (transactionMaps != null && !transactionMaps.isEmpty()) {
                                    List<Transaction> transactions = new ArrayList<>();

                                    for (Map<String, Object> transactionMap : transactionMaps) {
                                        long timestamp = transactionMap.get("date") instanceof Long ? (Long) transactionMap.get("date") : 0;
                                        Date date = new Date(timestamp);
                                        String description = (String) transactionMap.getOrDefault("description", "Unknown");
                                        double amount = transactionMap.get("amount") instanceof Number ? ((Number) transactionMap.get("amount")).doubleValue() : 0.0;
                                        String category = (String) transactionMap.getOrDefault("category", "Other");

                                        transactions.add(new Transaction(date, description, amount, category));
                                    }

                                    // Generate charts
                                    generateCategoryPieChart(transactions);
                                    generateMonthlyBarChart(transactions);
                                    return;
                                }
                            }
                        }
                    }
                    showNoDataMessage();
                });
    }

    private void showNoDataMessage() {
        categoryPieChart.setVisibility(View.GONE);
        monthlyBarChart.setVisibility(View.GONE);
        noDataText.setVisibility(View.VISIBLE);
    }

    private void generateCategoryPieChart(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                String category = transaction.getCategory();
                double amount = Math.abs(transaction.getAmount());
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }

        if (categoryTotals.isEmpty()) {
            categoryPieChart.setVisibility(View.GONE);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
        dataSet.setColors(getCustomColors());
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);

        categoryPieChart.setData(data);
        categoryPieChart.setDescription(null);
        categoryPieChart.setCenterText("Spending\nby Category");
        categoryPieChart.setHoleColor(Color.TRANSPARENT);
        categoryPieChart.invalidate();
    }

    private void generateMonthlyBarChart(List<Transaction> transactions) {
        Map<String, Double> monthlyTotals = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                String month = monthFormat.format(transaction.getDate());
                double amount = Math.abs(transaction.getAmount());
                monthlyTotals.put(month, monthlyTotals.getOrDefault(month, 0.0) + amount);
            }
        }

        if (monthlyTotals.isEmpty()) {
            monthlyBarChart.setVisibility(View.GONE);
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Spending");
        dataSet.setColors(getCustomColors());
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        monthlyBarChart.setData(new BarData(dataSet));
        monthlyBarChart.invalidate();
    }

    private int[] getCustomColors() {
        return new int[]{
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent)
        };
    }
}
