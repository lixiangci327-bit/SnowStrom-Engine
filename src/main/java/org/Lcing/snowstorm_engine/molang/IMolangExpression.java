package org.Lcing.snowstorm_engine.molang;

/**
 * Represents any evaluatable Molang expression.
 * Examples: "5", "variable.age + 1", "math.random(0,1)"
 */
public interface IMolangExpression {

    /**
     * Evaluates the expression in the given context.
     * 
     * @param context The current execution context (contains variables, time, etc.)
     * @return The result as a float
     */
    float eval(MolangContext context);

    /**
     * Helper to create a static constant expression.
     */
    static IMolangExpression constant(float value) {
        return context -> value;
    }

    /**
     * Helper to create a zero expression.
     */
    static IMolangExpression ZERO = constant(0.0f);
}
