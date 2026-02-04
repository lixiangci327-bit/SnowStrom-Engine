package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * 实现 minecraft:emitter_rate_manual
 * 粒子仅通过手动触发生成（max_particles 充当容量）。
 */
public class RateManualComponent implements IParticleComponent {

    private IMolangExpression maxParticles;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("max_particles")) {
            maxParticles = MolangParser.parseJson(comp.get("max_particles"));
        }
        if (maxParticles == null) {
            maxParticles = IMolangExpression.constant(50);
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        // 手动模式不自动生成；只需确保遵守最大容量
        int max = (int) maxParticles.eval(emitter.getContext());
        emitter.setMaxParticles(max);
        // 生成通过 emitter.spawnParticle() 外部控制
    }
}
