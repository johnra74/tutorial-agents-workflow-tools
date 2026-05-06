package me.johnra.tutorial.pipeline;

import me.johnra.tutorial.pipeline.model.ComplianceResult;
import me.johnra.tutorial.pipeline.model.ReadabilityResult;
import me.johnra.tutorial.pipeline.model.RouteDecision;
import me.johnra.tutorial.pipeline.service.ContentRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContentRouterTest {

    private ContentRouter router;

    @BeforeEach
    void setUp() {
        router = new ContentRouter();
    }

    @Test
    void criticalViolationAlwaysRejects() {
        ComplianceResult compliance = new ComplianceResult(List.of("harmful content"), "critical");
        ReadabilityResult quality = new ReadabilityResult(9, 9, 9, "none");

        assertThat(router.determine(compliance, quality)).isEqualTo(RouteDecision.REJECT);
    }

    @Test
    void majorViolationFlagsForReview() {
        ComplianceResult compliance = new ComplianceResult(List.of("unverified stat"), "major");
        ReadabilityResult quality = new ReadabilityResult(8, 8, 8, "none");

        assertThat(router.determine(compliance, quality)).isEqualTo(RouteDecision.FLAG_FOR_REVIEW);
    }

    @Test
    void lowQualityFlagsForReview() {
        ComplianceResult compliance = new ComplianceResult(List.of(), "none");
        ReadabilityResult quality = new ReadabilityResult(3, 4, 3, "improve structure");

        assertThat(router.determine(compliance, quality)).isEqualTo(RouteDecision.FLAG_FOR_REVIEW);
    }

    @Test
    void minorViolationFlagsForReview() {
        ComplianceResult compliance = new ComplianceResult(List.of("minor style issue"), "minor");
        ReadabilityResult quality = new ReadabilityResult(8, 8, 8, "none");

        assertThat(router.determine(compliance, quality)).isEqualTo(RouteDecision.FLAG_FOR_REVIEW);
    }

    @Test
    void cleanHighQualityContentApproves() {
        ComplianceResult compliance = new ComplianceResult(List.of(), "none");
        ReadabilityResult quality = new ReadabilityResult(8, 8, 8, "none");

        assertThat(router.determine(compliance, quality)).isEqualTo(RouteDecision.APPROVE);
    }
}
