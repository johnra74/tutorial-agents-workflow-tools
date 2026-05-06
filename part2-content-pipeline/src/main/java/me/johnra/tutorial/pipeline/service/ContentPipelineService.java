package me.johnra.tutorial.pipeline.service;

import me.johnra.tutorial.pipeline.model.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ContentPipelineService {

    private final ContentClassifier classifier;
    private final PolicyComplianceChecker complianceChecker;
    private final ReadabilityScorer readabilityScorer;
    private final EditorialBriefWriter briefWriter;
    private final ContentRouter router;
    private final Executor pipelineExecutor;

    public ContentPipelineService(ContentClassifier classifier,
                                  PolicyComplianceChecker complianceChecker,
                                  ReadabilityScorer readabilityScorer,
                                  EditorialBriefWriter briefWriter,
                                  ContentRouter router,
                                  @Qualifier("pipelineExecutor") Executor pipelineExecutor) {
        this.classifier = classifier;
        this.complianceChecker = complianceChecker;
        this.readabilityScorer = readabilityScorer;
        this.briefWriter = briefWriter;
        this.router = router;
        this.pipelineExecutor = pipelineExecutor;
    }

    public ContentAnalysis analyze(String draft) {
        // Step 1: Classify
        System.out.println("Step 1: Classifying content...");
        ClassificationResult classification = classifier.classify(draft);
        System.out.printf("  → %s for %s audience (~%d min read)%n",
                classification.contentType(),
                classification.targetAudience(),
                classification.estimatedReadTimeMinutes());

        // Steps 2A + 2B: run in parallel — saves wall-clock time vs. sequential
        System.out.println("Steps 2A + 2B: Compliance check and quality scoring running in parallel...");
        CompletableFuture<ComplianceResult> complianceFuture =
                CompletableFuture.supplyAsync(() -> complianceChecker.check(draft), pipelineExecutor);
        CompletableFuture<ReadabilityResult> qualityFuture =
                CompletableFuture.supplyAsync(() -> readabilityScorer.score(draft), pipelineExecutor);

        ComplianceResult compliance = complianceFuture.join();
        ReadabilityResult quality = qualityFuture.join();

        System.out.printf("  → Compliance severity: %s | violations: %d%n",
                compliance.severity(), compliance.violations().size());
        System.out.printf("  → Quality: readability=%d, structure=%d, engagement=%d%n",
                quality.readabilityScore(), quality.structureScore(), quality.engagementScore());

        // Step 3: Route — pure Java logic, no AI, fully testable with JUnit
        RouteDecision route = router.determine(compliance, quality);
        System.out.printf("Step 3: Routing decision → %s%n", route);

        // Step 4: Conditional — only fires when a human needs context for review
        String editorialBrief = null;
        if (route == RouteDecision.FLAG_FOR_REVIEW) {
            System.out.println("Step 4: Generating editorial brief for reviewer...");
            String context = """
                    Content type: %s | Audience: %s
                    Violations: %s | Severity: %s
                    Readability: %d/10 | Structure: %d/10 | Engagement: %d/10
                    Top improvement: %s

                    Content (first 1500 chars):
                    %s
                    """.formatted(
                    classification.contentType(), classification.targetAudience(),
                    compliance.violations(), compliance.severity(),
                    quality.readabilityScore(), quality.structureScore(), quality.engagementScore(),
                    quality.topImprovement(),
                    draft.substring(0, Math.min(draft.length(), 1500)));
            editorialBrief = briefWriter.writeBrief(context);
        }

        return new ContentAnalysis(
                classification.contentType(),
                classification.targetAudience(),
                compliance.violations(),
                quality.readabilityScore(),
                quality.structureScore(),
                route,
                editorialBrief);
    }
}
