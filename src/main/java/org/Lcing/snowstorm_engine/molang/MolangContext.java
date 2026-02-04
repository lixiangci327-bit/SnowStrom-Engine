package org.Lcing.snowstorm_engine.molang;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Execution context for Molang.
 * Stores variables, random seed, and providing environment queries.
 */
public class MolangContext {
    private final Map<String, Float> variables = new HashMap<>();
    private final Random random = new Random();

    public void setVariable(String name, float value) {
        variables.put(name, value);
    }

    public float getVariable(String name) {
        return variables.getOrDefault(name, 0.0f);
    }

    /**
     * Resolves a value. Handles both standard variables and special queries if
     * needed.
     */
    public float resolve(String name) {
        // Special handling for queries could go here
        if (name.equals("math.random")) {
            return random.nextFloat();
        }
        return getVariable(name);
    }

    public Random getRandom() {
        return random;
    }
}
