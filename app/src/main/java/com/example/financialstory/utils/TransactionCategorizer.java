package com.example.financialstory.utils;

public class TransactionCategorizer {
    public static String categorizeTransaction(String description) {
        description = description.toLowerCase();

        if (description.contains("grocery") || description.contains("supermarket") ||
                description.contains("food") || description.contains("market")) {
            return "Groceries";
        } else if (description.contains("restaurant") || description.contains("cafe") ||
                description.contains("coffee") || description.contains("dining")) {
            return "Dining";
        } else if (description.contains("gas") || description.contains("fuel") ||
                description.contains("uber") || description.contains("lyft") ||
                description.contains("transport")) {
            return "Transportation";
        } else if (description.contains("rent") || description.contains("mortgage") ||
                description.contains("housing")) {
            return "Housing";
        } else if (description.contains("utility") || description.contains("electric") ||
                description.contains("water") || description.contains("gas bill") ||
                description.contains("internet") || description.contains("phone")) {
            return "Utilities";
        } else if (description.contains("entertainment") || description.contains("movie") ||
                description.contains("subscription") || description.contains("netflix") ||
                description.contains("spotify")) {
            return "Entertainment";
        } else if (description.contains("health") || description.contains("medical") ||
                description.contains("doctor") || description.contains("pharmacy")) {
            return "Healthcare";
        } else if (description.contains("salary") || description.contains("payroll") ||
                description.contains("deposit") || description.contains("income")) {
            return "Income";
        } else {
            return "Other";
        }
    }
}
