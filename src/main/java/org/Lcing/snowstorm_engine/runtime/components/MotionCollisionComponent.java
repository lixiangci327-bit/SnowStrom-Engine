package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements minecraft:particle_motion_collision
 * Enables collision detection with blocks.
 */
public class MotionCollisionComponent implements IParticleComponent {

    private IMolangExpression collisionRadius;
    private IMolangExpression coefficientOfRestitution; // Bounce factor
    private IMolangExpression collisionDrag;
    private boolean expireOnContact = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        // Parse collision_radius
        collisionRadius = MolangParser.parseJson(comp.get("collision_radius"));
        if (collisionRadius == null) {
            collisionRadius = IMolangExpression.constant(0.1f);
        }

        // Parse coefficient_of_restitution (bounciness)
        coefficientOfRestitution = MolangParser.parseJson(comp.get("coefficient_of_restitution"));
        if (coefficientOfRestitution == null) {
            coefficientOfRestitution = IMolangExpression.constant(0);
        }

        // Parse collision_drag
        collisionDrag = MolangParser.parseJson(comp.get("collision_drag"));
        if (collisionDrag == null) {
            collisionDrag = IMolangExpression.ZERO;
        }

        // Parse expire_on_contact
        if (comp.has("expire_on_contact")) {
            expireOnContact = comp.get("expire_on_contact").getAsBoolean();
        }
    }

    @Override
    public void updateParticle(SnowstormParticle particle, float dt) {
        if (particle.isDead)
            return;

        var ctx = particle.getContext();
        float radius = collisionRadius.eval(ctx);

        // Simple ground collision (Y = 0)
        // In a real implementation, this would check against actual blocks
        double predictedY = particle.y + particle.vy * dt;

        if (predictedY - radius < 0) {
            // Collision with ground
            if (expireOnContact) {
                particle.isDead = true;
                return;
            }

            // Bounce
            float bounce = coefficientOfRestitution.eval(ctx);
            particle.vy = -particle.vy * bounce;
            particle.y = radius;

            // Apply friction/drag
            float drag = collisionDrag.eval(ctx);
            particle.vx *= (1 - drag);
            particle.vz *= (1 - drag);

            // Set collision flag for Molang
            ctx.setVariable("variable.has_collision", 1);
        }

        // TODO: Implement full block collision using Level.getBlockState
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        particle.getContext().setVariable("variable.has_collision", 0);
    }
}
