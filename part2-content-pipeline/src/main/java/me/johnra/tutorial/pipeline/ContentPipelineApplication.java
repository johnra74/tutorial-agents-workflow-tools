package me.johnra.tutorial.pipeline;

import me.johnra.tutorial.pipeline.model.ContentAnalysis;
import me.johnra.tutorial.pipeline.service.ContentPipelineService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContentPipelineApplication implements CommandLineRunner {

    private final ContentPipelineService pipelineService;

    public ContentPipelineApplication(ContentPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ContentPipelineApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String draft = """
                # 5 Ways AI Will Replace Every Developer by 2025

                Studies show that 90% of coding jobs will be automated within 18 months.
                As someone who has personally spoken with 50 Fortune 500 CEOs, I can confirm
                that mass layoffs are coming. Our product, DevKiller Pro, is the only tool
                that will help you survive this transition. Unlike our competitors who are
                hiding this information, we're being honest with you...
                """;

        ContentAnalysis result = pipelineService.analyze(draft);

        System.out.println("\n" + "=".repeat(60));
        System.out.printf("PIPELINE RESULT: %s%n", result.route());
        System.out.println("=".repeat(60));
        System.out.printf("Content type:  %s%n", result.contentType());
        System.out.printf("Audience:      %s%n", result.targetAudience());
        System.out.printf("Violations:    %s%n", result.policyViolations());
        System.out.printf("Readability:   %d/10%n", result.readabilityScore());
        System.out.printf("Structure:     %d/10%n", result.structureScore());
        if (result.editorialBrief() != null) {
            System.out.println("\nEditorial Brief:");
            System.out.println(result.editorialBrief());
        }
    }
}
