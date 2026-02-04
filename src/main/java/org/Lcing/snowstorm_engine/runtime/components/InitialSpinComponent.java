package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:particle_initial_spin
 * 设置粒子的初始旋转和旋转速率。
 */
public class InitialSpinComponent implements IParticleComponent {

    private IMolangExpression rotation = IMolangExpression.ZERO;
    private IMolangExpression rotationRate = IMolangExpression.ZERO;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了，没有包装
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("rotation")) {
            rotation = MolangParser.parseJson(comp.get("rotation"));
            if (rotation == null) {
                rotation = IMolangExpression.ZERO;
            }
        }

        if (comp.has("rotation_rate")) {
            rotationRate = MolangParser.parseJson(comp.get("rotation_rate"));
            if (rotationRate == null) {
                rotationRate = IMolangExpression.ZERO;
            }
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        var ctx = particle.getContext();

        // 设置初始旋转（度）
        particle.rotation = rotation.eval(ctx);

        // 设置旋转速率（度/秒）
        particle.rotationRate = rotationRate.eval(ctx);
    }
}
