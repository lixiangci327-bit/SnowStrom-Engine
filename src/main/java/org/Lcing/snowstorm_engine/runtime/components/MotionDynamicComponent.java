package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class MotionDynamicComponent implements IParticleComponent {

    private IMolangExpression[] linearAcceleration = new IMolangExpression[] { IMolangExpression.ZERO,
            IMolangExpression.ZERO, IMolangExpression.ZERO };
    private IMolangExpression linearDrag = IMolangExpression.ZERO;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return; // Usually empty object {} for default physics
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("linear_acceleration")) {
            JsonArray arr = obj.getAsJsonArray("linear_acceleration");
            linearAcceleration[0] = MolangParser.parseJson(arr.get(0));
            linearAcceleration[1] = MolangParser.parseJson(arr.get(1));
            linearAcceleration[2] = MolangParser.parseJson(arr.get(2));
        }

        if (obj.has("linear_drag_coefficient")) {
            linearDrag = MolangParser.parseJson(obj.get("linear_drag_coefficient"));
        }
    }

    @Override
    public void updateParticle(SnowstormParticle particle, float dt) {
        // Physics update
        // pos += velocity * dt
        particle.x += particle.vx * dt;
        particle.y += particle.vy * dt;
        particle.z += particle.vz * dt;

        // Acceleration
        // v += a * dt
        float ax = linearAcceleration[0].eval(particle.getContext());
        float ay = linearAcceleration[1].eval(particle.getContext());
        float az = linearAcceleration[2].eval(particle.getContext());

        // Debug: Log acceleration occasionally
        if (Math.random() < 0.002) {
            System.out.println("[Snowstorm] Accel: (" +
                    String.format("%.2f", ax) + ", " +
                    String.format("%.2f", ay) + ", " +
                    String.format("%.2f", az) + ")");
        }

        particle.vx += ax * dt;
        particle.vy += ay * dt;
        particle.vz += az * dt;

        // Drag
        // v *= (1 - drag * dt) ? Or more complex?
        // Basic implementation:
        float drag = linearDrag.eval(particle.getContext());
        if (drag > 0) {
            float dragFactor = Math.max(0, 1.0f - drag * dt);
            particle.vx *= dragFactor;
            particle.vy *= dragFactor;
            particle.vz *= dragFactor;
        }

        // Update Molang variables after move
        particle.getContext().setVariable("variable.particle_x", (float) particle.x);
        particle.getContext().setVariable("variable.particle_y", (float) particle.y);
        particle.getContext().setVariable("variable.particle_z", (float) particle.z);
    }
}
