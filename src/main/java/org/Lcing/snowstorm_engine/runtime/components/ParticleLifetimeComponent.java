package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class ParticleLifetimeComponent implements IParticleComponent {
    private IMolangExpression maxLifetime = IMolangExpression.constant(1.0f);
    // expiration_expressions 对于 MVP 尚未实现

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("max_lifetime")) {
            maxLifetime = MolangParser.parseJson(obj.get("max_lifetime"));
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        float lifetime = maxLifetime.eval(particle.getContext());
        particle.lifetime = lifetime;
        // 调试: 记录寿命设置
        if (Math.random() < 0.02) {
            // System.out.println("[Snowstorm] Lifetime set: " + String.format("%.3f",
            // lifetime));
        }
    }
}
