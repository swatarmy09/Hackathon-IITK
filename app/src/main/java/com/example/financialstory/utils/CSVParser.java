package com.example.financialstory.utils;

import com.example.financialstory.models.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVParser {

    public static List<Transaction> parseTransactions(InputStream inputStream) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        List<String> dateFormats = List.of("MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd"); // Support different date formats

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
             CSVReader reader = new CSVReader(bufferedReader)) {

            String[] nextLine;
            boolean isHeader = true;

            while ((nextLine = reader.readNext()) != null) {
                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Ensure correct CSV format: Date, Description, Amount
                if (nextLine.length >= 3) {
                    try {
                        String dateStr = nextLine[0].trim();
                        String description = nextLine[1].trim();
                        String amountStr = nextLine[2].trim().replace("$", "").replace(",", "");

                        Date date = parseDate(dateStr, dateFormats);
                        double amount = Double.parseDouble(amountStr);

                        // Categorize transaction
                        String category = TransactionCategorizer.categorizeTransaction(description);

                        transactions.add(new Transaction(date, description, amount, category));

                    } catch (ParseException | NumberFormatException e) {
                        System.err.println("Skipping invalid row: " + String.join(", ", nextLine));
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Skipping incomplete row: " + String.join(", ", nextLine));
                }
            }
        } catch (CsvValidationException e) {
            System.err.println("Error validating CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    private static Date parseDate(String dateStr, List<String> formats) throws ParseException {
        for (String format : formats) {
            try {
                return new SimpleDateFormat(format, Locale.US).parse(dateStr);
            } catch (ParseException ignored) {
            }
        }
        throw new ParseException("Unrecognized date format: " + dateStr, 0);
    }
}
