package com.example.financialstory.utils;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CohereApiClient {

    private static final String API_URL = "https://api.cohere.ai/v1/generate";
    private static final String API_KEY = "uJvjzqyFc51Vg6pU814dXCPiilq2K3nybGe7Sl22"; // Replace with your actual API key

    public interface CohereCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    public static void generateStory(String transactionData, CohereCallback callback) {
        new GenerateStoryTask(transactionData, callback).execute();
    }

    public static void askChatbot(String question, String transactionContext, CohereCallback callback) {
        new AskChatbotTask(question, transactionContext, callback).execute();
    }

    private static class GenerateStoryTask extends AsyncTask<Void, Void, String> {
        private String transactionData;
        private CohereCallback callback;
        private String errorMessage;

        public GenerateStoryTask(String transactionData, CohereCallback callback) {
            this.transactionData = transactionData;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String prompt = "Analyze the following financial transactions and generate a summary of spending habits, " +
                        "trends, and recommendations for better financial management:\n\n" + transactionData;

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "command");
                requestBody.put("prompt", prompt);
                requestBody.put("max_tokens", 500);
                requestBody.put("temperature", 0.7);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getJSONArray("generations").getJSONObject(0).getString("text");
                } else {
                    errorMessage = "API request failed with response code: " + responseCode;
                    return null;
                }
            } catch (IOException | JSONException e) {
                errorMessage = "Error: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                callback.onSuccess(result);
            } else {
                callback.onFailure(errorMessage);
            }
        }
    }

    private static class AskChatbotTask extends AsyncTask<Void, Void, String> {
        private String question;
        private String transactionContext;
        private CohereCallback callback;
        private String errorMessage;

        public AskChatbotTask(String question, String transactionContext, CohereCallback callback) {
            this.question = question;
            this.transactionContext = transactionContext;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String prompt = "You are a financial assistant. Based on the following transaction data:\n\n" +
                        transactionContext + "\n\nAnswer this question: " + question;

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "command");
                requestBody.put("prompt", prompt);
                requestBody.put("max_tokens", 300);
                requestBody.put("temperature", 0.7);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getJSONArray("generations").getJSONObject(0).getString("text");
                } else {
                    errorMessage = "API request failed with response code: " + responseCode;
                    return null;
                }
            } catch (IOException | JSONException e) {
                errorMessage = "Error: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                callback.onSuccess(result);
            } else {
                callback.onFailure(errorMessage);
            }
        }
    }
}