package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * Implements minecraft:emitter_local_space
 * When enabled, particles simulate in emitter's local space.
 */
public class LocalSpaceComponent implements IParticleComponent {

    private boolean localPosition = false;
    private boolean localRotation = false;
    private boolean localVelocity = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("position")) {
            localPosition = comp.get("position").getAsBoolean();
        }
        if (comp.has("rotation")) {
            localRotation = comp.get("rotation").getAsBoolean();
        }
        if (comp.has("velocity")) {
            localVelocity = comp.get("velocity").getAsBoolean();
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        // Store local space flags on emitter for use by other systems
        emitter.localSpacePosition = localPosition;
        emitter.localSpaceRotation = localRotation;
        emitter.localSpaceVelocity = localVelocity;
    }
}
