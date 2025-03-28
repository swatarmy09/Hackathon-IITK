package com.example.financialstory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.financialstory.models.Transaction;
import com.example.financialstory.utils.CSVParser;
import com.example.financialstory.utils.PDFExtractor;
import com.example.financialstory.utils.CohereApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_PDF = 1;
    private static final int REQUEST_CSV = 2;

    private Button uploadPdfButton, uploadCsvButton;
    private TextView statusText;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseRef;
    private String userId = "default_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Upload Statement");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        uploadPdfButton = findViewById(R.id.upload_pdf_button);
        uploadCsvButton = findViewById(R.id.upload_csv_button);
        statusText = findViewById(R.id.status_text);

        uploadPdfButton.setOnClickListener(v -> selectFile(REQUEST_PDF, "application/pdf"));
        uploadCsvButton.setOnClickListener(v -> selectFile(REQUEST_CSV, "*/*"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void selectFile(int requestCode, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri == null) {
                Toast.makeText(this, "File selection failed!", Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(new Intent(this, LoadingActivity.class));

            if (requestCode == REQUEST_PDF) {
                processPdfFile(fileUri);
            } else if (requestCode == REQUEST_CSV) {
                processCsvFile(fileUri);
            }
        }
    }

    private void processPdfFile(Uri fileUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null) throw new IOException("Failed to open file");

            List<Transaction> transactions = PDFExtractor.extractTransactions(inputStream);
            saveTransactionsToDatabase(transactions);

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error processing PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            statusText.setText("Failed to process PDF.");
        }
    }

    private void processCsvFile(Uri fileUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null) throw new IOException("Failed to open file");

            List<Transaction> transactions = CSVParser.parseTransactions(inputStream);
            saveTransactionsToDatabase(transactions);

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error processing CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
            statusText.setText("Failed to process CSV.");
        }
    }

    private void saveTransactionsToDatabase(final List<Transaction> transactions) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet!", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            return;
        }

        String transactionId = String.valueOf(System.currentTimeMillis());
        DatabaseReference transactionRef = databaseRef.child("statements").child(transactionId);

        List<Map<String, Object>> transactionMaps = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("date", transaction.getDate());
            transactionMap.put("description", transaction.getDescription());
            transactionMap.put("amount", transaction.getAmount());
            transactionMap.put("category", transaction.getCategory());
            transactionMaps.add(transactionMap);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("transactions", transactionMaps);
        data.put("timestamp", System.currentTimeMillis());

        transactionRef.setValue(data)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    statusText.setText("Successfully processed " + transactions.size() + " transactions.");
                    generateFinancialStory(transactionId, transactions);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this, "Error saving to Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    statusText.setText("Failed to save transactions.");
                });
    }

    private void generateFinancialStory(String transactionId, List<Transaction> transactions) {
        progressDialog.setTitle("AI Analysis");
        progressDialog.setMessage("Generating financial story...");
        progressDialog.show();

        StringBuilder transactionData = new StringBuilder();
        for (Transaction transaction : transactions) {
            transactionData.append(transaction.getDate())
                    .append(", ")
                    .append(transaction.getDescription())
                    .append(", ")
                    .append(transaction.getAmount())
                    .append("\n");
        }

        CohereApiClient.generateStory(transactionData.toString(), new CohereApiClient.CohereCallback() {
            @Override
            public void onSuccess(String story) {
                progressDialog.dismiss();
                saveFinancialStoryToDatabase(transactionId, story);
                Toast.makeText(UploadActivity.this, "Financial story generated!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(UploadActivity.this, DashboardActivity.class));
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(UploadActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveFinancialStoryToDatabase(String transactionId, String story) {
        DatabaseReference storyRef = databaseRef.child("statements").child(transactionId).child("financialStory");
        storyRef.setValue(story)
                .addOnSuccessListener(aVoid -> statusText.setText("Financial story saved successfully!"))
                .addOnFailureListener(e -> Toast.makeText(UploadActivity.this, "Error saving story: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
