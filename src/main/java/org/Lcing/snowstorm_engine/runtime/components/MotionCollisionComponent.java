package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:particle_motion_collision
 * 启用与方块的碰撞检测。
 */
public class MotionCollisionComponent implements IParticleComponent {

    private IMolangExpression collisionRadius;
    private IMolangExpression coefficientOfRestitution; // 弹跳系数
    private IMolangExpression collisionDrag;
    private boolean expireOnContact = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        // 解析 collision_radius
        collisionRadius = MolangParser.parseJson(comp.get("collision_radius"));
        if (collisionRadius == null) {
            collisionRadius = IMolangExpression.constant(0.1f);
        }

        // 解析 coefficient_of_restitution (弹性)
        coefficientOfRestitution = MolangParser.parseJson(comp.get("coefficient_of_restitution"));
        if (coefficientOfRestitution == null) {
            coefficientOfRestitution = IMolangExpression.constant(0);
        }

        // 解析 collision_drag
        collisionDrag = MolangParser.parseJson(comp.get("collision_drag"));
        if (collisionDrag == null) {
            collisionDrag = IMolangExpression.ZERO;
        }

        // 解析 expire_on_contact
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

        // 简单的地面碰撞 (Y = 0)
        // 在真实实现中，这将根据实际方块进行检查
        double predictedY = particle.y + particle.vy * dt;

        if (predictedY - radius < 0) {
            // 与地面碰撞
            if (expireOnContact) {
                particle.isDead = true;
                return;
            }

            // 反弹
            float bounce = coefficientOfRestitution.eval(ctx);
            particle.vy = -particle.vy * bounce;
            particle.y = radius;

            // 应用摩擦/阻力
            float drag = collisionDrag.eval(ctx);
            particle.vx *= (1 - drag);
            particle.vz *= (1 - drag);

            // 设置 Molang 的碰撞标志
            ctx.setVariable("variable.has_collision", 1);
        }

        // TODO: 使用 Level.getBlockState 实现完整的方块碰撞
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        particle.getContext().setVariable("variable.has_collision", 0);
    }
}
