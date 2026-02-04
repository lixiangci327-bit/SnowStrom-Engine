package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public interface IParticleComponent {
    /**
     * 当组件从 JSON 初始化时调用。
     * 
     * @param json 此组件的 JSON 元素。
     */
    void fromJson(JsonElement json);

    /**
     * 在发射器每 tick 调用。
     * 用于全局逻辑，如生成控制。
     */
    default void update(SnowstormEmitter emitter, float dt) {
    }

    /**
     * 当发射器重置进行新的循环周期时调用。
     */
    default void onEmitterLoopReset(SnowstormEmitter emitter) {
    }

    /**
     * 当新粒子生成时调用。
     * 用于初始化粒子状态（位置、速度等）。
     */
    default void onInitializeParticle(SnowstormParticle particle) {
    }

    /**
     * 在每个活动粒子的每 tick 调用。
     * 用于粒子物理和更新。
     */
    default void updateParticle(SnowstormParticle particle, float dt) {
    }

    /**
     * 在渲染之前调用，以设置渲染状态（例如 UV、着色）。
     */
    default void onRenderParticle(SnowstormParticle particle, float partialTick) {
    }
}
