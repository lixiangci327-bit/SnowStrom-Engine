package org.Lcing.snowstorm_engine.runtime;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Global Manager for Snowstorm Emitters.
 * Handles the ticking of all active emitters.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SnowstormManager {

    private static final SnowstormManager INSTANCE = new SnowstormManager();
    private final List<SnowstormEmitter> emitters = new ArrayList<>();

    public static SnowstormManager getInstance() {
        return INSTANCE;
    }

    private int tickCounter = 0;
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public void addEmitter(SnowstormEmitter emitter) {
        emitters.add(emitter);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            INSTANCE.tick();
        }
    }

    private void tick() {
        tickCounter++;
        float dt = 0.05f; // Minecraft tick is 20 TPS = 0.05s

        Iterator<SnowstormEmitter> it = emitters.iterator();
        while (it.hasNext()) {
            SnowstormEmitter emitter = it.next();
            emitter.tick(dt);

            // Remove emitter if marked for removal
            if (emitter.isMarkedForRemoval()) {
                it.remove();
            }
        }

        if (tickCounter % 20 == 0 && !emitters.isEmpty()) {
            int totalParticles = emitters.stream().mapToInt(SnowstormEmitter::getParticleCount).sum();
            LOGGER.info("[Snowstorm] Active Emitters: {}, Total Particles: {}", emitters.size(), totalParticles);
        }
    }

    public void clear() {
        emitters.clear();
        LOGGER.info("[Snowstorm] Cleared all emitters.");
    }

    @SubscribeEvent
    public static void onClientLogout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent event) {
        INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onClientLogin(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent event) {
        INSTANCE.clear();
    }

    public List<SnowstormEmitter> getEmitters() {
        return emitters;
    }
}
