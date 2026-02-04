package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:emitter_shape_disc
 * 粒子在圆盘（平面圆）上生成。
 */
public class ShapeDiscComponent implements IParticleComponent {

    private IMolangExpression offsetX, offsetY, offsetZ;
    private IMolangExpression radius;
    private IMolangExpression normalX, normalY, normalZ; // 圆盘法线
    private boolean surfaceOnly = false;
    private String directionMode = "outwards";
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

        // 解析 plane_normal (默认 Y 轴向上)
        if (comp.has("plane_normal") && comp.get("plane_normal").isJsonArray()) {
            var arr = comp.getAsJsonArray("plane_normal");
            normalX = MolangParser.parseJson(arr.get(0));
            normalY = MolangParser.parseJson(arr.get(1));
            normalZ = MolangParser.parseJson(arr.get(2));
        } else {
            normalX = IMolangExpression.ZERO;
            normalY = IMolangExpression.constant(1);
            normalZ = IMolangExpression.ZERO;
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
        var rand = ctx.getRandom();

        float r = radius.eval(ctx);

        // 圆盘上的随机角度
        double angle = rand.nextDouble() * 2 * Math.PI;

        // 到中心的距离
        float dist;
        if (surfaceOnly) {
            dist = r;
        } else {
            // 平方根用于圆盘上的均匀分布
            dist = r * (float) Math.sqrt(rand.nextDouble());
        }

        // 获取法向量
        float nx = normalX.eval(ctx);
        float ny = normalY.eval(ctx);
        float nz = normalZ.eval(ctx);

        // 归一化
        float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (nLen > 0.001f) {
            nx /= nLen;
            ny /= nLen;
            nz /= nLen;
        } else {
            ny = 1; // 默认 Y 轴向上
        }

        // 创建两个垂直于法线的向量
        float ux, uy, uz, vx, vy, vz;
        if (Math.abs(ny) > 0.9f) {
            // 法线接近 Y 轴，使用 X 轴作为参考
            ux = 1;
            uy = 0;
            uz = 0;
        } else {
            ux = 0;
            uy = 1;
            uz = 0;
        }
        // v = normal x u
        vx = ny * uz - nz * uy;
        vy = nz * ux - nx * uz;
        vz = nx * uy - ny * ux;
        // 归一化 v
        float vLen = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= vLen;
        vy /= vLen;
        vz /= vLen;
        // u = v x normal
        ux = vy * nz - vz * ny;
        uy = vz * nx - vx * nz;
        uz = vx * ny - vy * nx;

        // 圆盘上的位置: offset + (cos(angle)*u + sin(angle)*v) * dist
        float cosA = (float) Math.cos(angle);
        float sinA = (float) Math.sin(angle);

        float px = (cosA * ux + sinA * vx) * dist;
        float py = (cosA * uy + sinA * vy) * dist;
        float pz = (cosA * uz + sinA * vz) * dist;

        // 应用偏移
        float ox = offsetX.eval(ctx);
        float oy = offsetY.eval(ctx);
        float oz = offsetZ.eval(ctx);

        particle.x += ox + px;
        particle.y += oy + py;
        particle.z += oz + pz;

        // 设置方向
        switch (directionMode) {
            case "outwards":
                // 方向沿着圆盘（垂直于法线，从中心向外）
                float len = (float) Math.sqrt(px * px + py * py + pz * pz);
                if (len > 0.001f) {
                    particle.vx = px / len;
                    particle.vy = py / len;
                    particle.vz = pz / len;
                }
                break;
            case "inwards":
                len = (float) Math.sqrt(px * px + py * py + pz * pz);
                if (len > 0.001f) {
                    particle.vx = -px / len;
                    particle.vy = -py / len;
                    particle.vz = -pz / len;
                }
                break;
            case "custom":
                particle.vx = dirX != null ? dirX.eval(ctx) : 0;
                particle.vy = dirY != null ? dirY.eval(ctx) : 0;
                particle.vz = dirZ != null ? dirZ.eval(ctx) : 0;
                break;
            default:
                // 使用法线方向
                particle.vx = nx;
                particle.vy = ny;
                particle.vz = nz;
        }
    }
}
