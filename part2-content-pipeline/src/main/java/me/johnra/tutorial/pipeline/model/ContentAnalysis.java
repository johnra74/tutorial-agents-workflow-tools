package me.johnra.tutorial.pipeline.model;

import java.util.List;

public record ContentAnalysis(
        String contentType,
        String targetAudience,
        List<String> policyViolations,
        int readabilityScore,
        int structureScore,
        RouteDecision route,
        String editorialBrief   // null when route is APPROVE or REJECT
) {}
