package me.johnra.tutorial.pipeline.service;

import me.johnra.tutorial.pipeline.model.ComplianceResult;
import me.johnra.tutorial.pipeline.model.ReadabilityResult;
import me.johnra.tutorial.pipeline.model.RouteDecision;
import org.springframework.stereotype.Component;

@Component
public class ContentRouter {

    public RouteDecision determine(ComplianceResult compliance, ReadabilityResult quality) {
        // Critical violations always reject — no exceptions, no AI judgment involved
        if ("critical".equals(compliance.severity())) {
            return RouteDecision.REJECT;
        }

        // Major violations: a human should make the final call
        if ("major".equals(compliance.severity())) {
            return RouteDecision.FLAG_FOR_REVIEW;
        }

        double avgQuality = (quality.readabilityScore()
                + quality.structureScore()
                + quality.engagementScore()) / 3.0;

        // Below 5 average: too rough to publish
        if (avgQuality < 5.0) {
            return RouteDecision.FLAG_FOR_REVIEW;
        }

        // Minor violations or borderline quality: human eyes can make the call
        if (!compliance.violations().isEmpty() || avgQuality < 7.0) {
            return RouteDecision.FLAG_FOR_REVIEW;
        }

        return RouteDecision.APPROVE;
    }
}
