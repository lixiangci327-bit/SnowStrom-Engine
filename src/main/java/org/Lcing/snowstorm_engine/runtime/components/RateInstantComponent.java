package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:emitter_rate_instant
 * 当发射器启动/循环时，所有粒子一次性发射。
 */
public class RateInstantComponent implements IParticleComponent {

    private IMolangExpression numParticles;
    private boolean spawned = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("num_particles")) {
            numParticles = MolangParser.parseJson(comp.get("num_particles"));
        }
        if (numParticles == null) {
            numParticles = IMolangExpression.constant(10);
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        if (!emitter.isSpawning)
            return;

        // 每次循环只生成一次
        if (!spawned) {
            int count = (int) numParticles.eval(emitter.getContext());
            for (int i = 0; i < count; i++) {
                emitter.spawnParticle();
            }
            spawned = true;
        }
    }

    @Override
    public void onEmitterLoopReset(SnowstormEmitter emitter) {
        // 为下一次循环重置
        spawned = false;
    }
}
