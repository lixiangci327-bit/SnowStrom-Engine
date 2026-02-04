package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:emitter_shape_sphere
 * 粒子在球体体积内或其表面生成。
 */
public class ShapeSphereComponent implements IParticleComponent {

    private IMolangExpression offsetX, offsetY, offsetZ;
    private IMolangExpression radius;
    private boolean surfaceOnly = false;
    private String directionMode = "outwards"; // "outwards", "inwards", 或自定义向量
    private IMolangExpression dirX, dirY, dirZ;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        // 解析 offset [x, y, z]
        if (comp.has("offset") && comp.get("offset").isJsonArray()) {
            var arr = comp.getAsJsonArray("offset");
            offsetX = MolangParser.parseJson(arr.get(0));
            offsetY = MolangParser.parseJson(arr.get(1));
            offsetZ = MolangParser.parseJson(arr.get(2));
        } else {
            offsetX = offsetY = offsetZ = IMolangExpression.ZERO;
        }

        // 解析 radius
        radius = MolangParser.parseJson(comp.get("radius"));
        if (radius == null) {
            radius = IMolangExpression.constant(1);
        }

        // 解析 surface_only
        if (comp.has("surface_only")) {
            surfaceOnly = comp.get("surface_only").getAsBoolean();
        }

        // 解析 direction
        if (comp.has("direction")) {
            JsonElement dirElem = comp.get("direction");
            if (dirElem.isJsonPrimitive()) {
                directionMode = dirElem.getAsString();
            } else if (dirElem.isJsonArray()) {
                directionMode = "custom";
                var arr = dirElem.getAsJsonArray();
                dirX = MolangParser.parseJson(arr.get(0));
                dirY = MolangParser.parseJson(arr.get(1));
                dirZ = MolangParser.parseJson(arr.get(2));
            }
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        var ctx = particle.getContext();

        // 在球体内生成随机点
        float r = radius.eval(ctx);

        // 随机方向 (球体表面均匀分布)
        double theta = ctx.getRandom().nextDouble() * 2 * Math.PI;
        double phi = Math.acos(2 * ctx.getRandom().nextDouble() - 1);

        double sinPhi = Math.sin(phi);
        double nx = sinPhi * Math.cos(theta);
        double ny = sinPhi * Math.sin(theta);
        double nz = Math.cos(phi);

        // 到中心的随机距离
        float dist;
        if (surfaceOnly) {
            dist = r;
        } else {
            // 立方根用于均匀体积分布
            dist = r * (float) Math.cbrt(ctx.getRandom().nextDouble());
        }

        // 应用偏移
        float ox = offsetX.eval(ctx);
        float oy = offsetY.eval(ctx);
        float oz = offsetZ.eval(ctx);

        particle.x += ox + nx * dist;
        particle.y += oy + ny * dist;
        particle.z += oz + nz * dist;

        // 基于模式设置方向
        switch (directionMode) {
            case "outwards":
                particle.vx = (float) nx;
                particle.vy = (float) ny;
                particle.vz = (float) nz;
                break;
            case "inwards":
                particle.vx = (float) -nx;
                particle.vy = (float) -ny;
                particle.vz = (float) -nz;
                break;
            case "custom":
                particle.vx = dirX != null ? dirX.eval(ctx) : 0;
                particle.vy = dirY != null ? dirY.eval(ctx) : 0;
                particle.vz = dirZ != null ? dirZ.eval(ctx) : 0;
                break;
            default:
                // 默认为向外
                particle.vx = (float) nx;
                particle.vy = (float) ny;
                particle.vz = (float) nz;
        }
    }
}
