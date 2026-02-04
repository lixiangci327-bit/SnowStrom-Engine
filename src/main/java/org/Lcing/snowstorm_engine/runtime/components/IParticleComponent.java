package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonElement;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public interface IParticleComponent {
    /**
     * Called when the component is being initialized from JSON.
     * 
     * @param json The JSON element for this component.
     */
    void fromJson(JsonElement json);

    /**
     * Called every tick on the Emitter.
     * Use this for global logic like spawning control.
     */
    default void update(SnowstormEmitter emitter, float dt) {
    }

    /**
     * Called when the emitter resets for a new loop cycle.
     */
    default void onEmitterLoopReset(SnowstormEmitter emitter) {
    }

    /**
     * Called when a new particle is spawned.
     * Use this to initialize particle state (position, velocity, etc).
     */
    default void onInitializeParticle(SnowstormParticle particle) {
    }

    /**
     * Called every tick on every active particle.
     * Use this for particle physics and updates.
     */
    default void updateParticle(SnowstormParticle particle, float dt) {
    }

    /**
     * Called before rendering to set up render state (e.g. UVs, Coloring).
     */
    default void onRenderParticle(SnowstormParticle particle, float partialTick) {
    }
}
