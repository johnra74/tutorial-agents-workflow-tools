package me.johnra.tutorial.skills;

public record SkillResult<T>(
        T output,
        String model,
        int inputTokens,
        int outputTokens,
        long latencyMs,
        String skillName
) {}
