package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class ShapePointComponent implements IParticleComponent {

    // x, y, z expressions
    private final IMolangExpression[] offset = new IMolangExpression[] { IMolangExpression.ZERO, IMolangExpression.ZERO,
            IMolangExpression.ZERO };
    private final IMolangExpression[] direction = new IMolangExpression[] { IMolangExpression.ZERO,
            IMolangExpression.ZERO, IMolangExpression.ZERO };

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject obj = json.getAsJsonObject(); // No need specifically check for offset/direction? Usually present.

        parseVector(obj.get("offset"), offset);
        parseVector(obj.get("direction"), direction);
    }

    private void parseVector(JsonElement el, IMolangExpression[] target) {
        if (el != null && el.isJsonArray()) {
            JsonArray arr = el.getAsJsonArray();
            if (arr.size() >= 3) {
                target[0] = MolangParser.parseJson(arr.get(0));
                target[1] = MolangParser.parseJson(arr.get(1));
                target[2] = MolangParser.parseJson(arr.get(2));
            }
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        // Set Position (Offset from Emitter)
        // Assume Particle Context is already populated with necessary variables (like
        // particle_random_1)
        float ox = offset[0].eval(particle.getContext());
        float oy = offset[1].eval(particle.getContext());
        float oz = offset[2].eval(particle.getContext());

        // Debug: Log offset and random value
        float random1 = particle.getContext().resolve("variable.particle_random_1");
        if (Math.random() < 0.02) {
            System.out.println("[Snowstorm] Offset: (" +
                    String.format("%.2f", ox) + ", " +
                    String.format("%.2f", oy) + ", " +
                    String.format("%.2f", oz) + ") random1=" +
                    String.format("%.3f", random1));
        }

        particle.x += ox;
        particle.y += oy;
        particle.z += oz;

        // Set Direction (Normalized? Or just direction vector)
        // Snowstorm usually treats this as the direction vector for velocity.
        // Velocity magnitude is controlled by 'particle_initial_speed'.
        // So we store the direction in the particle logic, possibly overwriting
        // velocity for now.

        float dx = direction[0].eval(particle.getContext());
        float dy = direction[1].eval(particle.getContext());
        float dz = direction[2].eval(particle.getContext());

        particle.vx = dx;
        particle.vy = dy;
        particle.vz = dz;
    }
}
