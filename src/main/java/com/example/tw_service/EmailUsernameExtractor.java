package com.example.tw_service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts X/Twitter handles from user-provided email exports/text.
 * It deliberately does not query X to discover an account from an arbitrary email address.
 */
@Service
public class EmailUsernameExtractor {
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern X_URL = Pattern.compile("(?i)https?://(?:www\\.)?(?:x\\.com|twitter\\.com)/([A-Za-z0-9_]{1,15})(?:\\b|/|\\?)");
    private static final Pattern AT_HANDLE = Pattern.compile("(?<![A-Za-z0-9_])@([A-Za-z0-9_]{1,15})\\b");

    public List<EmailUsernameRow> extract(String text) {
        Map<String, String> rows = new LinkedHashMap<>();
        if (text == null) return List.of();

        Matcher emails = EMAIL.matcher(text);
        List<String> foundEmails = new ArrayList<>();
        while (emails.find()) foundEmails.add(emails.group());

        Matcher urls = X_URL.matcher(text);
        while (urls.find()) {
            String username = urls.group(1);
            String email = nearestEmail(text, urls.start(), foundEmails);
            String key = (email == null ? "" : email.toLowerCase()) + "|" + username.toLowerCase();
            rows.putIfAbsent(key, email + "\t" + username);
        }

        // If no X URL is present, accept @handles from the supplied email/export text.
        // Common words such as @gmail are filtered by the 15-char username rule plus email overlap.
        Matcher handles = AT_HANDLE.matcher(text);
        while (handles.find()) {
            String username = handles.group(1);
            if (EMAIL.matcher(username + "@x").find()) continue;
            String email = nearestEmail(text, handles.start(), foundEmails);
            String key = (email == null ? "" : email.toLowerCase()) + "|" + username.toLowerCase();
            rows.putIfAbsent(key, (email == null ? "" : email) + "\t" + username);
        }

        List<EmailUsernameRow> result = new ArrayList<>();
        for (String value : rows.values()) {
            String[] parts = value.split("\\t", -1);
            result.add(new EmailUsernameRow(parts[0], parts.length > 1 ? parts[1] : ""));
        }
        return result;
    }

    private String nearestEmail(String text, int position, List<String> emails) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String email : emails) {
            int idx = text.toLowerCase().indexOf(email.toLowerCase());
            if (idx >= 0) {
                int d = Math.abs(idx - position);
                if (d < bestDistance && d <= 500) {
                    bestDistance = d;
                    best = email;
                }
            }
        }
        return best;
    }

    public record EmailUsernameRow(String email, String username) {}
}
