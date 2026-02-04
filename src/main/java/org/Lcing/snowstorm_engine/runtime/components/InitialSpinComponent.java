package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:particle_initial_spin
 * Sets initial rotation and rotation rate for particles.
 */
public class InitialSpinComponent implements IParticleComponent {

    private IMolangExpression rotation = IMolangExpression.ZERO;
    private IMolangExpression rotationRate = IMolangExpression.ZERO;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value, not wrapped
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("rotation")) {
            rotation = MolangParser.parseJson(comp.get("rotation"));
            if (rotation == null) {
                rotation = IMolangExpression.ZERO;
            }
        }

        if (comp.has("rotation_rate")) {
            rotationRate = MolangParser.parseJson(comp.get("rotation_rate"));
            if (rotationRate == null) {
                rotationRate = IMolangExpression.ZERO;
            }
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        var ctx = particle.getContext();

        // Set initial rotation (degrees)
        particle.rotation = rotation.eval(ctx);

        // Set rotation rate (degrees/second)
        particle.rotationRate = rotationRate.eval(ctx);
    }
}
