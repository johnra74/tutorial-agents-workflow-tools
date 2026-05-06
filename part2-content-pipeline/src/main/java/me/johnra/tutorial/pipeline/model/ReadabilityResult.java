package me.johnra.tutorial.pipeline.model;

public record ReadabilityResult(
        int readabilityScore,   // 1-10
        int structureScore,     // 1-10
        int engagementScore,    // 1-10
        String topImprovement
) {}
