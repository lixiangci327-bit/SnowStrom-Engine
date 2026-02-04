package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * 实现 minecraft:emitter_lifetime_once
 * 发射器执行一次并在 active_time 后过期。
 */
public class LifetimeOnceComponent implements IParticleComponent {

    private IMolangExpression activeTime = IMolangExpression.constant(10);
    private boolean started = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("active_time")) {
            activeTime = MolangParser.parseJson(comp.get("active_time"));
            if (activeTime == null) {
                activeTime = IMolangExpression.constant(10);
            }
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        if (!started) {
            // 启动时评估一次 active_time
            float lifetime = activeTime.eval(emitter.getContext());
            emitter.setMaxLifetime(lifetime);
            emitter.isSpawning = true;
            started = true;
        }

        // 检查发射器是否已超过其寿命
        float age = emitter.getAge();
        float maxLife = emitter.getMaxLifetime();

        if (age >= maxLife) {
            // 停止生成新粒子，但还不要移除发射器
            emitter.isSpawning = false;

            // 仅当所有粒子都死亡时才标记为移除
            if (emitter.getParticleCount() == 0) {
                emitter.markForRemoval();
            }
        }
    }
}
