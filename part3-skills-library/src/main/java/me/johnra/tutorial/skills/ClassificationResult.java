package me.johnra.tutorial.skills;

public record ClassificationResult(
        String category,
        double confidence,   // 0.0 to 1.0
        String reasoning
) {}
