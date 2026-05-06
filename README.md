# Agents, Workflows, and Skills - Source Code

Companion code for the 3-part blog series. Each project is a standalone Spring Boot application.

## Projects

### `part1-bug-agent`
A bug investigation agent that autonomously searches source files, reads code, and runs tests to diagnose a reported defect.

**Companion post:** [Part 1: Agents - When the Problem Doesn't Have a Roadmap](https://www.johnra.me/2026/05/01/part-1-agents-when-the-problem-doesnt-have-a-roadmap/).

### `part1-calculator-target`
The intentionally buggy calculator project that `part1-bug-agent` investigates. Contains a copy-paste bug in `CalculatorService.add()` - `a - b` instead of `a + b` - along with a test suite that exposes it.

### `part2-content-pipeline`
A multi-step content analysis workflow: classification → compliance check → readability scoring → routing → editorial brief. Each step runs in parallel where possible using virtual threads.

** Companion post:** [Part 2: Workflows — When You Know the Steps Before You Start](https://www.johnra.me/2026/05/06/part-2-workflow-when-you-know-the-steps-before-you-start/).

> Note: Will update once Part 3 is completed

---

## Prerequisites

- Java 21 JDK
- Maven 3.9+
- [Ollama](https://ollama.ai) running locally (or reachable over the network)

Pull the models used by the projects:

```bash
ollama pull qwen3   # part1-bug-agent (tool calling)
```

---

## Running the Projects

Each project is a self-contained Spring Boot app. From the project directory:

```bash
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn package -q
java -jar target/*.jar
```

### Configuration

Each project reads from `src/main/resources/application.properties`. At minimum, set the Ollama base URL and confirm the model name matches what you have pulled:

**`part1-bug-agent`**
```properties
langchain4j.ollama.chat-model.base-url=http://localhost:11434
langchain4j.ollama.chat-model.model-name=qwen3
project.root=/path/to/part1-calculator-target
```

For `part1-bug-agent`, `project.root` must point to the `part1-calculator-target` directory (absolute path or relative to where you run the agent). The default relative path `../part1-calculator-target` works if you run from inside the `part1-bug-agent` directory.

**`part2-content-pipeline`**
```properties
ollama.base-url=http://localhost:11434
ollama.model=llama3.1
```

---

## Structure

```
agents/
├── part1-bug-agent/            # Agent that investigates bugs
├── part1-calculator-target/    # Buggy project the agent investigates
└── part2-content-pipeline/     # Deterministic content analysis workflow
```
