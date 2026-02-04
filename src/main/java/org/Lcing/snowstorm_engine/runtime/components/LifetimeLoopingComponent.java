package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

public class LifetimeLoopingComponent implements IParticleComponent {

    private IMolangExpression activeTime = IMolangExpression.constant(10.0f);
    private IMolangExpression sleepTime = IMolangExpression.ZERO;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("active_time")) {
            activeTime = MolangParser.parseJson(obj.get("active_time"));
        }
        if (obj.has("sleep_time")) {
            sleepTime = MolangParser.parseJson(obj.get("sleep_time"));
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        float active = activeTime.eval(emitter.getContext());
        float sleep = sleepTime.eval(emitter.getContext());
        float total = active + sleep;

        if (total <= 0)
            return; // 如果配置错误，防止除以零或无限循环

        float age = emitter.getAge();

        if (age >= active) {
            emitter.isSpawning = false;
        } else {
            emitter.isSpawning = true;
        }

        if (age >= total) {
            emitter.setAge(0);
            emitter.isSpawning = true;
        }
    }
}
