package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:emitter_shape_entity_aabb
 * 在实体的包围盒内生成粒子。
 */
public class ShapeEntityAABBComponent implements IParticleComponent {

    private String directionMode = "outwards";
    private IMolangExpression dirX, dirY, dirZ;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        // 解析 direction
        if (comp.has("direction")) {
            JsonElement dirElem = comp.get("direction");
            if (dirElem.isJsonPrimitive()) {
                directionMode = dirElem.getAsString();
            } else if (dirElem.isJsonArray()) {
                directionMode = "custom";
                JsonArray arr = dirElem.getAsJsonArray();
                dirX = MolangParser.parseJson(arr.get(0));
                dirY = MolangParser.parseJson(arr.get(1));
                dirZ = MolangParser.parseJson(arr.get(2));
            }
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        var ctx = particle.getContext();
        var rand = ctx.getRandom();

        // 使用 Molang 变量中的实体包围盒（由发射器设置）
        // 如果未设置，默认为 1x2x1 的类似玩家的盒子
        float minX = ctx.resolve("variable.entity_aabb_min_x");
        float minY = ctx.resolve("variable.entity_aabb_min_y");
        float minZ = ctx.resolve("variable.entity_aabb_min_z");
        float maxX = ctx.resolve("variable.entity_aabb_max_x");
        float maxY = ctx.resolve("variable.entity_aabb_max_y");
        float maxZ = ctx.resolve("variable.entity_aabb_max_z");

        // 如果没有设置实体边界，使用默认值
        if (minX == 0 && maxX == 0) {
            minX = -0.3f;
            maxX = 0.3f;
        }
        if (minY == 0 && maxY == 0) {
            minY = 0;
            maxY = 1.8f;
        }
        if (minZ == 0 && maxZ == 0) {
            minZ = -0.3f;
            maxZ = 0.3f;
        }

        // AABB 内的随机位置
        float px = minX + rand.nextFloat() * (maxX - minX);
        float py = minY + rand.nextFloat() * (maxY - minY);
        float pz = minZ + rand.nextFloat() * (maxZ - minZ);

        particle.x += px;
        particle.y += py;
        particle.z += pz;

        // 基于模式的方向
        switch (directionMode) {
            case "outwards":
                // 从 AABB 中心向外的方向
                float cx = (minX + maxX) / 2;
                float cy = (minY + maxY) / 2;
                float cz = (minZ + maxZ) / 2;
                float dx = px - cx;
                float dy = py - cy;
                float dz = pz - cz;
                float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len > 0.001f) {
                    particle.vx = dx / len;
                    particle.vy = dy / len;
                    particle.vz = dz / len;
                }
                break;
            case "inwards":
                cx = (minX + maxX) / 2;
                cy = (minY + maxY) / 2;
                cz = (minZ + maxZ) / 2;
                dx = cx - px;
                dy = cy - py;
                dz = cz - pz;
                len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len > 0.001f) {
                    particle.vx = dx / len;
                    particle.vy = dy / len;
                    particle.vz = dz / len;
                }
                break;
            case "custom":
                particle.vx = dirX != null ? dirX.eval(ctx) : 0;
                particle.vy = dirY != null ? dirY.eval(ctx) : 0;
                particle.vz = dirZ != null ? dirZ.eval(ctx) : 0;
                break;
        }
    }
}
