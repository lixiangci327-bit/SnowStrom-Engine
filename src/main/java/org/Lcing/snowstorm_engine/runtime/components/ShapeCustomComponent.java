package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:emitter_shape_custom
 * 使用 Molang 偏移和方向的自定义点生成。
 */
public class ShapeCustomComponent implements IParticleComponent {

    private IMolangExpression offsetX, offsetY, offsetZ;
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

        // 解析 direction [x, y, z]
        if (comp.has("direction") && comp.get("direction").isJsonArray()) {
            var arr = comp.getAsJsonArray("direction");
            dirX = MolangParser.parseJson(arr.get(0));
            dirY = MolangParser.parseJson(arr.get(1));
            dirZ = MolangParser.parseJson(arr.get(2));
        } else {
            dirX = dirY = dirZ = IMolangExpression.ZERO;
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        var ctx = particle.getContext();

        // 应用自定义偏移
        particle.x += offsetX.eval(ctx);
        particle.y += offsetY.eval(ctx);
        particle.z += offsetZ.eval(ctx);

        // 应用自定义方向
        particle.vx = dirX.eval(ctx);
        particle.vy = dirY.eval(ctx);
        particle.vz = dirZ.eval(ctx);
    }
}
