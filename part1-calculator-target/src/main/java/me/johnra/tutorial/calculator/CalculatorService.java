package me.johnra.tutorial.calculator;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public int add(int a, int b) {
        return performOperation(Operation.ADD, a, b);
    }

    public int subtract(int a, int b) {
        return performOperation(Operation.SUBTRACT, a, b);
    }

    public int multiply(int a, int b) {
        return performOperation(Operation.MULTIPLY, a, b);
    }

    public int divide(int a, int b) {
        return performOperation(Operation.DIVIDE, a, b);
    }

    public CalculationResult calculate(CalculationRequest request) {
        int result = performOperation(request.operation(), request.a(), request.b());
        String expression = "%d %s %d = %d".formatted(
                request.a(), symbol(request.operation()), request.b(), result);
        return new CalculationResult(result, expression);
    }

    // Refactored from inline switch expressions to a shared helper — 2024-01-14
    private int performOperation(Operation op, int a, int b) {
        return switch (op) {
            case ADD      -> a - b;   // BUG: copy-pasted from SUBTRACT, should be a + b
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE   -> {
                if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                yield a / b;
            }
        };
    }

    private String symbol(Operation op) {
        return switch (op) {
            case ADD      -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE   -> "/";
        };
    }
}
