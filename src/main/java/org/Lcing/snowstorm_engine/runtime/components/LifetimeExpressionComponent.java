package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * 实现 minecraft:emitter_lifetime_expression
 * 发射器的激活/过期由 Molang 表达式控制。
 */
public class LifetimeExpressionComponent implements IParticleComponent {

    private IMolangExpression activationExpression;
    private IMolangExpression expirationExpression;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json 已经是组件值了
        JsonObject comp = json.getAsJsonObject();

        activationExpression = MolangParser.parseJson(comp.get("activation_expression"));
        if (activationExpression == null) {
            activationExpression = IMolangExpression.constant(1); // 始终激活
        }

        expirationExpression = MolangParser.parseJson(comp.get("expiration_expression"));
        if (expirationExpression == null) {
            expirationExpression = IMolangExpression.ZERO; // 基于表达式永不过期
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        var ctx = emitter.getContext();

        // 检查激活状态
        float activation = activationExpression.eval(ctx);
        emitter.isSpawning = activation != 0;

        // 检查过期状态 (非零 = 过期)
        float expiration = expirationExpression.eval(ctx);
        if (expiration != 0) {
            emitter.markForRemoval();
        }
    }
}
