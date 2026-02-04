package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * 实现 minecraft:emitter_local_space
 * 启用时，粒子在发射器的局部空间中模拟。
 */
public class LocalSpaceComponent implements IParticleComponent {

    private boolean localPosition = false;
    private boolean localRotation = false;
    private boolean localVelocity = false;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        if (comp.has("position")) {
            localPosition = comp.get("position").getAsBoolean();
        }
        if (comp.has("rotation")) {
            localRotation = comp.get("rotation").getAsBoolean();
        }
        if (comp.has("velocity")) {
            localVelocity = comp.get("velocity").getAsBoolean();
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        // 在发射器上存储局部空间标志以供其他系统使用
        emitter.localSpacePosition = localPosition;
        emitter.localSpaceRotation = localRotation;
        emitter.localSpaceVelocity = localVelocity;
    }
}
