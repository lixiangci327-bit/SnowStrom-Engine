package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class ShapePointComponent implements IParticleComponent {

    // x, y, z 表达式
    private final IMolangExpression[] offset = new IMolangExpression[] { IMolangExpression.ZERO, IMolangExpression.ZERO,
            IMolangExpression.ZERO };
    private final IMolangExpression[] direction = new IMolangExpression[] { IMolangExpression.ZERO,
            IMolangExpression.ZERO, IMolangExpression.ZERO };

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject obj = json.getAsJsonObject(); // 不需要专门检查 offset/direction？通常存在。

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
        // 设置位置 (相对于发射器的偏移)
        // 假设粒子上下文已经填充了必要的变量 (如 particle_random_1)
        float ox = offset[0].eval(particle.getContext());
        float oy = offset[1].eval(particle.getContext());
        float oz = offset[2].eval(particle.getContext());

        // 调试: 记录偏移和随机值
        float random1 = particle.getContext().resolve("variable.particle_random_1");
        if (Math.random() < 0.02) {
            // System.out.println("[Snowstorm] Offset: (" +
            // String.format("%.2f", ox) + ", " +
            // String.format("%.2f", oy) + ", " +
            // String.format("%.2f", oz) + ") random1=" +
            // String.format("%.3f", random1));
        }

        particle.x += ox;
        particle.y += oy;
        particle.z += oz;

        // 设置方向 (归一化? 或者只是方向向量)
        // Snowstorm 通常将其视为速度的方向向量。
        // 速度大小由 'particle_initial_speed' 控制。
        // 所以我们在粒子逻辑中存储方向，可能暂时覆盖速度。

        float dx = direction[0].eval(particle.getContext());
        float dy = direction[1].eval(particle.getContext());
        float dz = direction[2].eval(particle.getContext());

        if (Math.abs(dx) < 0.001 && Math.abs(dy) < 0.001 && Math.abs(dz) < 0.001) {
            // 点形状默认为随机方向 (各向同性)
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);

            particle.vx = Math.sin(phi) * Math.cos(theta);
            particle.vy = Math.sin(phi) * Math.sin(theta);
            particle.vz = Math.cos(phi);
        } else {
            particle.vx = dx;
            particle.vy = dy;
            particle.vz = dz;
        }
    }
}
