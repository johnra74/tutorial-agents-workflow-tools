package me.johnra.tutorial.skills;

import java.util.List;

public record ContentReadinessResult(
        boolean readyForPublication,
        String summary,
        String audience,
        List<String> blockers
) {}
