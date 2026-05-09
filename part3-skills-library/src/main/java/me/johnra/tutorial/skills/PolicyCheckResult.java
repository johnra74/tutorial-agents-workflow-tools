package me.johnra.tutorial.skills;

import java.util.List;

public record PolicyCheckResult(
        boolean passed,
        List<String> violations,
        String severity   // none|minor|major|critical
) {}
