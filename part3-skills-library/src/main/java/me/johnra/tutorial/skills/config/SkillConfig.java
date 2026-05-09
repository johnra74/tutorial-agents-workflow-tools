package me.johnra.tutorial.skills.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import me.johnra.tutorial.skills.ClassifySkill;
import me.johnra.tutorial.skills.ContentReadinessSkill;
import me.johnra.tutorial.skills.PolicyCheckSkill;
import me.johnra.tutorial.skills.SummarizeSkill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SkillConfig {

    private static final List<String> CONTENT_POLICY_RULES = List.of(
            "No unverified statistics presented as fact",
            "No competitor disparagement",
            "Medical/legal/financial claims require disclaimers",
            "No misleading headlines"
    );

    private static final List<String> BUG_CATEGORIES = List.of(
            "null_reference", "off_by_one", "race_condition",
            "data_corruption", "configuration", "other"
    );

    @Bean
    public ClassifySkill bugClassifySkill(ChatLanguageModel chatModel) {
        return new ClassifySkill(chatModel, BUG_CATEGORIES);
    }

    @Bean
    public PolicyCheckSkill contentPolicySkill(ChatLanguageModel chatModel) {
        return new PolicyCheckSkill(chatModel, CONTENT_POLICY_RULES);
    }

    @Bean
    public SummarizeSkill editorialSummarizer(ChatLanguageModel chatModel) {
        return new SummarizeSkill(chatModel, 100, "editorial");
    }

    @Bean
    public ContentReadinessSkill contentReadinessSkill(
            ClassifySkill bugClassifySkill,
            PolicyCheckSkill contentPolicySkill,
            SummarizeSkill editorialSummarizer) {
        return new ContentReadinessSkill(bugClassifySkill, contentPolicySkill, editorialSummarizer);
    }
}
