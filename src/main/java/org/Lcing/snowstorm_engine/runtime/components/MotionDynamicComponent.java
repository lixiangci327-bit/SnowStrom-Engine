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
            return; // 通常为空对象 {} 用于默认物理
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
        // 物理更新
        // pos += velocity * dt
        particle.x += particle.vx * dt;
        particle.y += particle.vy * dt;
        particle.z += particle.vz * dt;

        // 加速度
        // v += a * dt
        float ax = linearAcceleration[0].eval(particle.getContext());
        float ay = linearAcceleration[1].eval(particle.getContext());
        float az = linearAcceleration[2].eval(particle.getContext());

        // 调试: 偶尔记录加速度
        if (Math.random() < 0.002) {
            // System.out.println("[Snowstorm] Accel: (" +
            // String.format("%.2f", ax) + ", " +
            // String.format("%.2f", ay) + ", " +
            // String.format("%.2f", az) + ")");
        }

        particle.vx += ax * dt;
        particle.vy += ay * dt;
        particle.vz += az * dt;

        // 阻力
        // v *= (1 - drag * dt) ? 或者是更复杂的公式？
        // 基础实现：
        float drag = linearDrag.eval(particle.getContext());
        if (drag > 0) {
            // 限制 dragFactor 以防止反向加速或不稳定的速度增长
            float dragFactor = Math.max(0, Math.min(1, 1.0f - drag * dt));
            particle.vx *= dragFactor;
            particle.vy *= dragFactor;
            particle.vz *= dragFactor;
        }

        // 移动后更新 Molang 变量
        particle.getContext().setVariable("variable.particle_x", (float) particle.x);
        particle.getContext().setVariable("variable.particle_y", (float) particle.y);
        particle.getContext().setVariable("variable.particle_z", (float) particle.z);
    }
}
