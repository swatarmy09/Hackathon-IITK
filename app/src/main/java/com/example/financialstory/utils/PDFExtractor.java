package com.example.financialstory.utils;

import com.example.financialstory.models.Transaction;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFExtractor {

    public static List<Transaction> extractTransactions(InputStream pdfInputStream) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        // Load PDF document using OpenPDF
        PdfReader reader = new PdfReader(pdfInputStream);
        StringBuilder textBuilder = new StringBuilder();

        // Extract text from all pages using OpenPDF
        int numPages = reader.getNumberOfPages();
        for (int i = 1; i <= numPages; i++) {
            textBuilder.append(extractTextFromPage(reader, i)).append("\n");
        }
        reader.close();
        String text = textBuilder.toString();

        // Define pattern to extract transactions (Modify as per your bank statement format)
        Pattern pattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+\\$(\\d+\\.\\d{2})");
        Matcher matcher = pattern.matcher(text);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        while (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                String description = matcher.group(2).trim();
                String amountStr = matcher.group(3);

                Date date = dateFormat.parse(dateStr);
                double amount = Double.parseDouble(amountStr);

                // Categorize transaction
                String category = TransactionCategorizer.categorizeTransaction(description);

                transactions.add(new Transaction(date, description, amount, category));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return transactions;
    }

    // Custom method to extract text from a page using OpenPDF
    private static String extractTextFromPage(PdfReader reader, int pageNumber) {
        try {
            byte[] pageContent = reader.getPageContent(pageNumber);
            return new String(pageContent);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
