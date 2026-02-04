package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class InitialSpeedComponent implements IParticleComponent {
    private IMolangExpression speed = IMolangExpression.constant(1.0f);

    @Override
    public void fromJson(JsonElement json) {
        this.speed = MolangParser.parseJson(json);
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        float s = speed.eval(particle.getContext());

        // Normalize current velocity (direction) then scale by speed
        // Assuming current vx,vy,vz is direction set by ShapePoint
        double len = Math.sqrt(particle.vx * particle.vx + particle.vy * particle.vy + particle.vz * particle.vz);
        if (len > 0.0001) {
            particle.vx = (particle.vx / len) * s;
            particle.vy = (particle.vy / len) * s;
            particle.vz = (particle.vz / len) * s;
        } else {
            // If no direction, speed does nothing (or random direction? usually 0)
        }
    }
}
