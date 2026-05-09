package me.johnra.tutorial.skills.config;

import me.johnra.tutorial.skills.ClassifySkill;
import me.johnra.tutorial.skills.ContentReadinessSkill;
import me.johnra.tutorial.skills.PolicyCheckSkill;
import me.johnra.tutorial.skills.SkillRegistry;
import me.johnra.tutorial.skills.SummarizeSkill;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class SkillRegistryConfig {

    @Bean
    @Order(1)
    public ApplicationRunner registerSkills(
            SkillRegistry registry,
            ClassifySkill bugClassifySkill,
            PolicyCheckSkill contentPolicySkill,
            SummarizeSkill editorialSummarizer,
            ContentReadinessSkill contentReadinessSkill) {
        return args -> {
            registry
                    .register(bugClassifySkill)
                    .register(contentPolicySkill)
                    .register(editorialSummarizer)
                    .register(contentReadinessSkill);
            System.out.println("Skills registered: " + registry.list());
        };
    }
}
