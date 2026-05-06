package me.johnra.tutorial.pipeline.model;

public record ClassificationResult(
        String contentType,             // tutorial|opinion|news|product|other
        String targetAudience,          // beginner|intermediate|advanced|general
        int estimatedReadTimeMinutes
) {}
