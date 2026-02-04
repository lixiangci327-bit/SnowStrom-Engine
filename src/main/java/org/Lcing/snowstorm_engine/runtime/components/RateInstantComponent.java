package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:emitter_rate_instant
 * All particles are emitted at once when the emitter starts/loops.
 */
public class RateInstantComponent implements IParticleComponent {

    private IMolangExpression numParticles;
    private boolean spawned = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("num_particles")) {
            numParticles = MolangParser.parseJson(comp.get("num_particles"));
        }
        if (numParticles == null) {
            numParticles = IMolangExpression.constant(10);
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        if (!emitter.isSpawning)
            return;

        // Only spawn once per loop
        if (!spawned) {
            int count = (int) numParticles.eval(emitter.getContext());
            for (int i = 0; i < count; i++) {
                emitter.spawnParticle();
            }
            spawned = true;
        }
    }

    @Override
    public void onEmitterLoopReset(SnowstormEmitter emitter) {
        // Reset for next loop
        spawned = false;
    }
}
