package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:particle_appearance_lighting
 * Enables block light/sky light influence on particle color.
 */
public class LightingComponent implements IParticleComponent {

    // Currently just a flag - actual implementation would use light level from
    // world

    @Override
    public void fromJson(JsonElement json) {
        // This component has no fields - its presence enables lighting
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        particle.useLighting = true;
    }
}
