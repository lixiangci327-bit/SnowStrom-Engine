package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * Implements minecraft:emitter_lifetime_once
 * Emitter executes once and expires after active_time.
 */
public class LifetimeOnceComponent implements IParticleComponent {

    private IMolangExpression activeTime = IMolangExpression.constant(10);
    private boolean started = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("active_time")) {
            activeTime = MolangParser.parseJson(comp.get("active_time"));
            if (activeTime == null) {
                activeTime = IMolangExpression.constant(10);
            }
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        if (!started) {
            // Evaluate active_time once at start
            float lifetime = activeTime.eval(emitter.getContext());
            emitter.setMaxLifetime(lifetime);
            emitter.isSpawning = true;
            started = true;
        }

        // Check if emitter has exceeded its lifetime
        float age = emitter.getAge();
        float maxLife = emitter.getMaxLifetime();

        if (age >= maxLife) {
            // Stop spawning new particles, but DON'T remove emitter yet
            emitter.isSpawning = false;

            // Only mark for removal when all particles have died
            if (emitter.getParticleCount() == 0) {
                emitter.markForRemoval();
            }
        }
    }
}
