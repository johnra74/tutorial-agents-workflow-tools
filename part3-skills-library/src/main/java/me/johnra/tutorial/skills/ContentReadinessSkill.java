package me.johnra.tutorial.skills;

import java.util.ArrayList;
import java.util.List;

public class ContentReadinessSkill implements Skill<ContentReadinessResult> {

    private final Skill<ClassificationResult> classifier;
    private final Skill<PolicyCheckResult> policyChecker;
    private final Skill<String> summarizer;

    public ContentReadinessSkill(
            Skill<ClassificationResult> classifier,
            Skill<PolicyCheckResult> policyChecker,
            Skill<String> summarizer) {
        this.classifier = classifier;
        this.policyChecker = policyChecker;
        this.summarizer = summarizer;
    }

    @Override
    public String skillName() {
        return "content_readiness";
    }

    @Override
    public SkillResult<ContentReadinessResult> run(String content) {
        long start = System.currentTimeMillis();

        SkillResult<ClassificationResult> audience = classifier.run(content);
        SkillResult<PolicyCheckResult> policy = policyChecker.run(content);
        SkillResult<String> summary = summarizer.run(content);

        List<String> blockers = new ArrayList<>(policy.output().violations());
        if (List.of("major", "critical").contains(policy.output().severity())) {
            blockers.add("Policy severity: " + policy.output().severity());
        }

        ContentReadinessResult result = new ContentReadinessResult(
                policy.output().passed() && blockers.isEmpty(),
                summary.output(),
                audience.output().category(),
                blockers);

        return new SkillResult<>(result, "composite", 0, 0,
                System.currentTimeMillis() - start, skillName());
    }
}
