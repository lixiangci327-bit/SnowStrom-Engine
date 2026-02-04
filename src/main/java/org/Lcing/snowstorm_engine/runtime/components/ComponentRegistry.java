package org.Lcing.snowstorm_engine.runtime.components;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentRegistry {
    private static final Map<String, Supplier<IParticleComponent>> FACTORIES = new HashMap<>();

    public static void register(String identifier, Supplier<IParticleComponent> factory) {
        FACTORIES.put(identifier, factory);
    }

    public static IParticleComponent create(String identifier) {
        Supplier<IParticleComponent> factory = FACTORIES.get(identifier);
        return factory != null ? factory.get() : null;
    }
}
