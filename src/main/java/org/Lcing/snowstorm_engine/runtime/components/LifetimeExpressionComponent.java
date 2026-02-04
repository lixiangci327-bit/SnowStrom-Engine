package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

/**
 * Implements minecraft:emitter_lifetime_expression
 * Emitter activation/expiration controlled by Molang expressions.
 */
public class LifetimeExpressionComponent implements IParticleComponent {

    private IMolangExpression activationExpression;
    private IMolangExpression expirationExpression;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;

        // json is already the component value
        JsonObject comp = json.getAsJsonObject();

        activationExpression = MolangParser.parseJson(comp.get("activation_expression"));
        if (activationExpression == null) {
            activationExpression = IMolangExpression.constant(1); // always active
        }

        expirationExpression = MolangParser.parseJson(comp.get("expiration_expression"));
        if (expirationExpression == null) {
            expirationExpression = IMolangExpression.ZERO; // never expires based on expression
        }
    }

    @Override
    public void update(SnowstormEmitter emitter, float dt) {
        var ctx = emitter.getContext();

        // Check activation
        float activation = activationExpression.eval(ctx);
        emitter.isSpawning = activation != 0;

        // Check expiration (non-zero = expire)
        float expiration = expirationExpression.eval(ctx);
        if (expiration != 0) {
            emitter.markForRemoval();
        }
    }
}
