# Part 3 — Skills Library

Companion code for **[Part 3: Skills — Reusable AI Capabilities](https://www.johnra.me/2026/05/09/part-3-skills-building-the-reusable-capabilities-that-power-everything-else/)**.

## Purpose

Demonstrates how to design AI capabilities as discrete, reusable skills rather than embedding model calls directly into workflows or agents. Skills are self-contained units that can be discovered, composed, and invoked across multiple callers without duplication.

## Objective

Build a `SkillRegistry` containing named skills, each implementing the `Skill<T>` interface. A caller can retrieve any skill by name and invoke it with arbitrary input, receiving a typed `SkillResult<T>`. The same skills can be used from an agent loop, a workflow step, or a one-off script without modification.

Skills included:

| Skill | Purpose |
|---|---|
| `ClassifySkill` | Classifies text into one of N caller-supplied categories |
| `SummarizeSkill` | Produces a concise summary of input text |
| `ExtractStructuredDataSkill` | Extracts fields into a caller-supplied record type using reflection |
| `PolicyCheckSkill` | Checks content against a configurable set of policy rules |
| `ContentReadinessSkill` | Composite skill: classify + policy check + summarize |

## Key Concepts

- **`Skill<T>` interface** — the public contract (`skillName()`, `run(input)`) that every skill implements, whether backed by a model or composed from other skills. Agents, workflows, and the registry all depend on this interface, not on concrete classes.
- **`BaseSkill<T>`** — abstract base that implements the retry loop, token tracking, and structured logging. Concrete model-backed skills extend this; composite skills implement `Skill<T>` directly.
- **`JsonExtractor`** — package-private utility that normalises model output before JSON parsing: strips `<think>` reasoning blocks, extracts content from code fences, and falls back to the first `{...}` span in the response.
- **`SkillRegistry`** — a central `Map<String, Skill<?>>` registered at startup via `SkillRegistryConfig`; any `Skill<T>` implementation can be registered regardless of whether it extends `BaseSkill`.
- **Interface-based composition** — `ContentReadinessSkill` accepts `Skill<ClassificationResult>`, `Skill<PolicyCheckResult>`, and `Skill<String>` in its constructor. It composes atomic skills without knowing their implementations, making it trivially testable with stubs.

## SOLID Principles Applied

| Principle | Where |
|---|---|
| **Single Responsibility** | `JsonExtractor` handles model output normalisation; `BaseSkill` handles the execution loop; skills handle prompting and parsing |
| **Open/Closed** | `SkillRegistry` accepts any `Skill<?>` — new skill types require no registry changes |
| **Interface Segregation** | `Skill<T>` exposes only `skillName()` and `run()` — callers never need to see `BaseSkill` internals |
| **Dependency Inversion** | `ContentReadinessSkill`, `SkillRegistry`, and `SkillConfig` all depend on `Skill<T>`, not on concrete classes |

## Project Structure

```
src/main/java/me/johnra/tutorial/skills/
├── Skill.java                         # Public interface: skillName() + run()
├── SkillResult.java                   # Result record: output, model, tokens, latency, skillName
├── BaseSkill.java                     # Abstract base: retry loop, token tracking, logging
├── JsonExtractor.java                 # Package-private: normalises model output before JSON parsing
├── SkillRegistry.java                 # Name-based Skill<?> lookup
├── SkillsApplication.java             # Entry point; exercises all skills and prints results
├── ClassifySkill.java                 # Classifies text into one of N categories
├── SummarizeSkill.java                # Summarises text to a target word count
├── ExtractStructuredDataSkill.java    # Extracts typed data via record reflection
├── PolicyCheckSkill.java              # Checks content against configurable rules
├── ContentReadinessSkill.java         # Composite: classify + policy + summarize
├── ClassificationResult.java
├── PolicyCheckResult.java
├── ContentReadinessResult.java
└── config/
    ├── ModelConfig.java               # Configures OllamaChatModel bean
    ├── SkillConfig.java               # Instantiates skill beans; wires ContentReadinessSkill
    └── SkillRegistryConfig.java       # Registers all skills into the SkillRegistry at startup
```

## Configuration

`src/main/resources/application.properties`:

```properties
ollama.base-url=http://localhost:11434
ollama.model=llama3.1
```

Pull the model with `ollama pull llama3.1`.

## Running

```bash
mvn spring-boot:run
```

The application runs five demo scenarios against sample input and prints structured results to stdout, showing atomic and composite skill invocation through the same `Skill<T>` interface.

## Testing

```bash
mvn test
```

`PolicyCheckSkillTest` makes real Ollama calls. Run with `ollama serve` active. In CI, schedule these as a nightly job rather than running on every commit.
