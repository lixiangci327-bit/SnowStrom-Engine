package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * 实现 minecraft:particle_appearance_lighting
 * 启用方块光/天光对粒子颜色的影响。
 */
public class LightingComponent implements IParticleComponent {

    // 目前只是一个标志 - 实际实现将使用来自世界的光照等级

    @Override
    public void fromJson(JsonElement json) {
        // 该组件没有字段 - 它的存在即启用光照
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        particle.useLighting = true;
    }
}
