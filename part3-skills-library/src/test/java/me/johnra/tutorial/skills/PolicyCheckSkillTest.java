package me.johnra.tutorial.skills;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// These tests make real Ollama calls. Run them with `ollama serve` active.
// In CI, schedule these as a nightly job — not on every commit push.
class PolicyCheckSkillTest {

    private PolicyCheckSkill skill;

    @BeforeEach
    void setUp() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(60))
                .build();

        skill = new PolicyCheckSkill(model, List.of(
                "No unverified statistics",
                "No competitor disparagement"
        ));
    }

    @Test
    void cleanContentPasses() {
        SkillResult<PolicyCheckResult> result = skill.run(
                "Java is a great programming language for enterprise applications.");

        assertThat(result.output().passed()).isTrue();
        assertThat(result.output().severity()).isEqualTo("none");
        assertThat(result.output().violations()).isEmpty();
    }

    @Test
    void unverifiedStatisticIsDetected() {
        SkillResult<PolicyCheckResult> result = skill.run(
                "Studies show 99% of users prefer our product over all competitors.");

        assertThat(result.output().passed()).isFalse();
        assertThat(result.output().violations()).isNotEmpty();
    }

    @Test
    void competitorDenunciationIsDetected() {
        SkillResult<PolicyCheckResult> result = skill.run(
                "Unlike CompetitorX, which is terrible and unreliable, our tool always works.");

        assertThat(result.output().passed()).isFalse();
        assertThat(result.output().violations()).isNotEmpty();
    }

    @Test
    void resultCarriesObservabilityMetadata() {
        SkillResult<PolicyCheckResult> result = skill.run("Some perfectly reasonable content.");

        assertThat(result.latencyMs()).isPositive();
        assertThat(result.skillName()).isEqualTo("policy_check");
        assertThat(result.model()).isEqualTo("ollama");
    }
}
