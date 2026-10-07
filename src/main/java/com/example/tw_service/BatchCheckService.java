package com.example.tw_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BatchCheckService {
    private final TwitterCheckService twitterCheckService;
    private final int maxItems;
    private final int workers;
    private final long delayMs;

    public BatchCheckService(
            TwitterCheckService twitterCheckService,
            @Value("${bot.batch.max-items:2000}") int maxItems,
            @Value("${bot.batch.workers:4}") int workers,
            @Value("${bot.batch.delay-ms:350}") long delayMs) {
        this.twitterCheckService = twitterCheckService;
        this.maxItems = Math.max(1, maxItems);
        this.workers = Math.max(1, Math.min(8, workers));
        this.delayMs = Math.max(0, delayMs);
    }

    public List<String> normalizeUsernames(List<String> raw) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String line : raw) {
            if (line == null) continue;
            for (String token : line.split("[,;\\t\\s]+")) {
                String u = token.trim().replaceFirst("^@+", "");
                if (u.matches("[A-Za-z0-9_]{1,15}")) unique.add(u);
            }
        }
        return new ArrayList<>(unique).subList(0, Math.min(unique.size(), maxItems));
    }

    public List<ResultRow> checkAll(List<String> usernames) {
        List<String> input = usernames.subList(0, Math.min(usernames.size(), maxItems));
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        try {
            List<CompletableFuture<ResultRow>> futures = new ArrayList<>();
            AtomicInteger index = new AtomicInteger();
            for (String username : input) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    int i = index.getAndIncrement();
                    if (delayMs > 0 && i > 0) {
                        try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }
                    try {
                        TwitterCheckService.CheckResult r = twitterCheckService.checkStructured(username);
                        return new ResultRow(username, r.status().name(), r.reason());
                    } catch (Exception e) {
                        return new ResultRow(username, "ERROR", e.getClass().getSimpleName());
                    }
                }, pool));
            }
            List<ResultRow> result = new ArrayList<>();
            for (CompletableFuture<ResultRow> f : futures) result.add(f.join());
            return result;
        } finally {
            pool.shutdownNow();
        }
    }

    public record ResultRow(String username, String status, String reason) {}
}
