package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * Implements minecraft:emitter_rate_manual
 * Particles are only spawned via manual trigger (max_particles acts as
 * capacity).
 */
public class RateManualComponent implements IParticleComponent {

    private IMolangExpression maxParticles;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("max_particles")) {
            maxParticles = MolangParser.parseJson(comp.get("max_particles"));
        }
        if (maxParticles == null) {
            maxParticles = IMolangExpression.constant(50);
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        // Manual mode doesn't auto-spawn; just ensure max capacity is respected
        int max = (int) maxParticles.eval(emitter.getContext());
        emitter.setMaxParticles(max);
        // Spawning is controlled externally via emitter.spawnParticle()
    }
}
