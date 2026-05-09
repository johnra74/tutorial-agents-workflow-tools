package me.johnra.tutorial.skills;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;

@SpringBootApplication
@Order(2)
public class SkillsApplication implements CommandLineRunner {

    private final ClassifySkill bugClassifySkill;
    private final PolicyCheckSkill contentPolicySkill;
    private final SummarizeSkill editorialSummarizer;
    private final ContentReadinessSkill contentReadinessSkill;

    public SkillsApplication(
            ClassifySkill bugClassifySkill,
            PolicyCheckSkill contentPolicySkill,
            SummarizeSkill editorialSummarizer,
            ContentReadinessSkill contentReadinessSkill) {
        this.bugClassifySkill = bugClassifySkill;
        this.contentPolicySkill = contentPolicySkill;
        this.editorialSummarizer = editorialSummarizer;
        this.contentReadinessSkill = contentReadinessSkill;
    }

    public static void main(String[] args) {
        SpringApplication.run(SkillsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SKILLS DEMO");
        System.out.println("=".repeat(60));

        // Demo 1: Bug classification
        System.out.println("\n[1] ClassifySkill — bug category");
        SkillResult<ClassificationResult> bugResult = bugClassifySkill.run(
                "NullPointerException in UserService.getUser() when user.getProfile() returns null");
        System.out.printf("  Category:   %s%n", bugResult.output().category());
        System.out.printf("  Confidence: %.0f%%%n", bugResult.output().confidence() * 100);
        System.out.printf("  Reasoning:  %s%n", bugResult.output().reasoning());
        System.out.printf("  Latency:    %dms | tokens: %d/%d%n",
                bugResult.latencyMs(), bugResult.inputTokens(), bugResult.outputTokens());

        // Demo 2: Policy check — clean content
        System.out.println("\n[2] PolicyCheckSkill — clean content");
        SkillResult<PolicyCheckResult> cleanResult = contentPolicySkill.run(
                "Java 21 introduces virtual threads, making concurrent programming simpler and more efficient.");
        System.out.printf("  Passed:   %b | Severity: %s%n",
                cleanResult.output().passed(), cleanResult.output().severity());

        // Demo 3: Policy check — problematic content
        System.out.println("\n[3] PolicyCheckSkill — problematic content");
        SkillResult<PolicyCheckResult> badResult = contentPolicySkill.run(
                "Studies show 99% of developers prefer our tool over CompetitorX, which is terrible and broken.");
        System.out.printf("  Passed:     %b | Severity: %s%n",
                badResult.output().passed(), badResult.output().severity());
        System.out.printf("  Violations: %s%n", badResult.output().violations());

        // Demo 4: Summarization
        System.out.println("\n[4] SummarizeSkill");
        SkillResult<String> summaryResult = editorialSummarizer.run("""
                Spring Boot 3.3.4 brings numerous improvements to the developer experience.
                Virtual thread support via Java 21 makes writing high-throughput applications
                significantly easier. The auto-configuration system has been refined to reduce
                startup time, and observability support with Micrometer is now first-class.
                Teams migrating from older Spring Boot versions will find the upgrade path
                well-documented and the breaking changes minimal.
                """);
        System.out.println("  Summary: " + summaryResult.output());

        // Demo 5: Compound skill
        System.out.println("\n[5] ContentReadinessSkill (compound: classify + policy + summarize)");
        SkillResult<ContentReadinessResult> readinessResult = contentReadinessSkill.run("""
                This intermediate-level tutorial walks through building a REST API with Spring Boot.
                We cover controller setup, service layer design, and repository patterns.
                By the end, you will have a working CRUD application ready for deployment.
                """);
        ContentReadinessResult readiness = readinessResult.output();
        System.out.printf("  Ready for publication: %b%n", readiness.readyForPublication());
        System.out.printf("  Audience:              %s%n", readiness.audience());
        System.out.printf("  Blockers:              %s%n", readiness.blockers());
        System.out.println("  Summary: " + readiness.summary());

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Demo complete.");
    }
}
