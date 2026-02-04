package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class InitialSpeedComponent implements IParticleComponent {
    private IMolangExpression speed = IMolangExpression.constant(1.0f);

    @Override
    public void fromJson(JsonElement json) {
        this.speed = MolangParser.parseJson(json);
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        float s = speed.eval(particle.getContext());

        // 归一化当前速度（方向）然后按速度缩放
        // 假设当前 vx,vy,vz 是由 ShapePoint 设置的方向
        double len = Math.sqrt(particle.vx * particle.vx + particle.vy * particle.vy + particle.vz * particle.vz);
        if (len > 0.0001) {
            particle.vx = (particle.vx / len) * s;
            particle.vy = (particle.vy / len) * s;
            particle.vz = (particle.vz / len) * s;
        } else {
            // 如果没有方向，速度不产生影响（或者随机方向？通常为 0）
        }
    }
}
