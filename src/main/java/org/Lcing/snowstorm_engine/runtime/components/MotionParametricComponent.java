package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:particle_motion_parametric
 * 位置和旋转直接由 Molang 驱动（覆盖物理）。
 */
public class MotionParametricComponent implements IParticleComponent {

    // 相对生成点的偏移
    private IMolangExpression relativeX, relativeY, relativeZ;
    // 朝向
    private IMolangExpression directionX, directionY, directionZ;
    // 绕方向旋转
    private IMolangExpression rotation;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        // 解析 relative_position [x, y, z]
        if (comp.has("relative_position") && comp.get("relative_position").isJsonArray()) {
            JsonArray arr = comp.getAsJsonArray("relative_position");
            relativeX = MolangParser.parseJson(arr.get(0));
            relativeY = MolangParser.parseJson(arr.get(1));
            relativeZ = MolangParser.parseJson(arr.get(2));
        }

        // 解析 direction [x, y, z]
        if (comp.has("direction") && comp.get("direction").isJsonArray()) {
            JsonArray arr = comp.getAsJsonArray("direction");
            directionX = MolangParser.parseJson(arr.get(0));
            directionY = MolangParser.parseJson(arr.get(1));
            directionZ = MolangParser.parseJson(arr.get(2));
        }

        // 解析 rotation
        rotation = MolangParser.parseJson(comp.get("rotation"));
    }

    @Override
    public void updateParticle(SnowstormParticle particle, float dt) {
        var ctx = particle.getContext();

        // 更新位置 (相对于生成点的偏移)
        if (relativeX != null && relativeY != null && relativeZ != null) {
            // 获取生成位置 (存储为原点)
            double spawnX = ctx.resolve("variable.spawn_x");
            double spawnY = ctx.resolve("variable.spawn_y");
            double spawnZ = ctx.resolve("variable.spawn_z");

            particle.x = spawnX + relativeX.eval(ctx);
            particle.y = spawnY + relativeY.eval(ctx);
            particle.z = spawnZ + relativeZ.eval(ctx);
        }

        // 更新速度/方向 (用于渲染目的)
        if (directionX != null && directionY != null && directionZ != null) {
            particle.vx = directionX.eval(ctx);
            particle.vy = directionY.eval(ctx);
            particle.vz = directionZ.eval(ctx);
        }

        // 更新旋转
        if (rotation != null) {
            particle.rotation = rotation.eval(ctx);
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        // 存储生成位置用于相对计算
        var ctx = particle.getContext();
        ctx.setVariable("variable.spawn_x", (float) particle.x);
        ctx.setVariable("variable.spawn_y", (float) particle.y);
        ctx.setVariable("variable.spawn_z", (float) particle.z);
    }
}
