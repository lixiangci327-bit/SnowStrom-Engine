package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

public class RateSteadyComponent implements IParticleComponent {

    private IMolangExpression spawnRate = IMolangExpression.ZERO;
    private IMolangExpression maxParticles = IMolangExpression.constant(50);

    // Accumulator for fractional spawning
    private float spawnAccumulator = 0;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("spawn_rate")) {
            this.spawnRate = MolangParser.parseJson(obj.get("spawn_rate"));
        }
        if (obj.has("max_particles")) {
            this.maxParticles = MolangParser.parseJson(obj.get("max_particles"));
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        if (!emitter.isSpawning)
            return;

        float rate = spawnRate.eval(emitter.getContext());
        float max = maxParticles.eval(emitter.getContext());

        // Spawn Logic
        if (rate > 0 && emitter.getParticleCount() < max) {
            float particlesToSpawn = rate * dt + spawnAccumulator;
            int count = (int) particlesToSpawn;
            spawnAccumulator = particlesToSpawn - count;

            for (int i = 0; i < count; i++) {
                if (emitter.getParticleCount() >= max)
                    break;
                emitter.spawnParticle();
            }
        }
    }
}
