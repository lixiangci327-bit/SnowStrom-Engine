package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:particle_motion_parametric
 * Position and rotation driven directly by Molang (overrides physics).
 */
public class MotionParametricComponent implements IParticleComponent {

    // Relative offset from spawn point
    private IMolangExpression relativeX, relativeY, relativeZ;
    // Direction facing
    private IMolangExpression directionX, directionY, directionZ;
    // Rotation around direction
    private IMolangExpression rotation;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        // Parse relative_position [x, y, z]
        if (comp.has("relative_position") && comp.get("relative_position").isJsonArray()) {
            JsonArray arr = comp.getAsJsonArray("relative_position");
            relativeX = MolangParser.parseJson(arr.get(0));
            relativeY = MolangParser.parseJson(arr.get(1));
            relativeZ = MolangParser.parseJson(arr.get(2));
        }

        // Parse direction [x, y, z]
        if (comp.has("direction") && comp.get("direction").isJsonArray()) {
            JsonArray arr = comp.getAsJsonArray("direction");
            directionX = MolangParser.parseJson(arr.get(0));
            directionY = MolangParser.parseJson(arr.get(1));
            directionZ = MolangParser.parseJson(arr.get(2));
        }

        // Parse rotation
        rotation = MolangParser.parseJson(comp.get("rotation"));
    }

    @Override
    public void updateParticle(SnowstormParticle particle, float dt) {
        var ctx = particle.getContext();

        // Update position (relative offset from spawn)
        if (relativeX != null && relativeY != null && relativeZ != null) {
            // Get spawn position (stored as origin)
            double spawnX = ctx.resolve("variable.spawn_x");
            double spawnY = ctx.resolve("variable.spawn_y");
            double spawnZ = ctx.resolve("variable.spawn_z");

            particle.x = spawnX + relativeX.eval(ctx);
            particle.y = spawnY + relativeY.eval(ctx);
            particle.z = spawnZ + relativeZ.eval(ctx);
        }

        // Update velocity/direction (for rendering purposes)
        if (directionX != null && directionY != null && directionZ != null) {
            particle.vx = directionX.eval(ctx);
            particle.vy = directionY.eval(ctx);
            particle.vz = directionZ.eval(ctx);
        }

        // Update rotation
        if (rotation != null) {
            particle.rotation = rotation.eval(ctx);
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        // Store spawn position for relative calculations
        var ctx = particle.getContext();
        ctx.setVariable("variable.spawn_x", (float) particle.x);
        ctx.setVariable("variable.spawn_y", (float) particle.y);
        ctx.setVariable("variable.spawn_z", (float) particle.z);
    }
}
