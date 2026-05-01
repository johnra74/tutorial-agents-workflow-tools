package me.johnra.tutorial.agent;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BugInvestigationApplication implements CommandLineRunner {

    private final BugInvestigationAgent agent;

    public BugInvestigationApplication(BugInvestigationAgent agent) {
        this.agent = agent;
    }

    public static void main(String[] args) {
        SpringApplication.run(BugInvestigationApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String bugReport = """
                Bug: The calculator's add() operation is returning wrong results.
                For example, add(2, 3) returns -1 instead of 5, and add(4, -3) returns 7 instead of 1.
                Strangely, add(7, 0) returns the correct value of 7, so it is not broken for all inputs.
                The bug was introduced after yesterday's refactoring of CalculatorService.
                Subtract, multiply, and divide all appear to work correctly.
                The test suite has several failing tests — please run them for details.
                """;

        String result = agent.investigate(bugReport);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("INVESTIGATION REPORT");
        System.out.println("=".repeat(60));
        System.out.println(result);
    }
}
