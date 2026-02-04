package org.Lcing.snowstorm_engine.runtime;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * Snowstorm 发射器全局管理器。
 * 负责处理所有活跃发射器的 Tick 更新。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SnowstormManager {

    private static final SnowstormManager INSTANCE = new SnowstormManager();
    // 活跃的发射器列表
    private final List<SnowstormEmitter> emitters = new ArrayList<>();
    // 等待添加的发射器列表（防止在遍历时修改集合导致 ConcurrentModificationException）
    private final List<SnowstormEmitter> pendingEmitters = new ArrayList<>();

    public static SnowstormManager getInstance() {
        return INSTANCE;
    }

    private int tickCounter = 0;
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    /**
     * 添加一个新的发射器到等待队列。
     * 它将在下一个 tick 开始时被合并到主列表中。
     */
    public void addEmitter(SnowstormEmitter emitter) {
        synchronized (pendingEmitters) {
            pendingEmitters.add(emitter);
        }
    }

    /**
     * 在指定位置创建并激活一个发射器。
     *
     * @param identifier 粒子 ID
     * @param x          世界坐标 X
     * @param y          世界坐标 Y
     * @param z          世界坐标 Z
     * @return 创建的发射器对象，如果 ID 未找到则返回 null。
     */
    public SnowstormEmitter createEmitter(String identifier, double x, double y, double z) {
        org.Lcing.snowstorm_engine.definition.ParticleDefinition def = getParticleDefinition(identifier);
        if (def == null)
            return null;

        SnowstormEmitter emitter = new SnowstormEmitter(def);
        emitter.x = x;
        emitter.y = y;
        emitter.z = z;
        emitter.spawnParticle();

        addEmitter(emitter);
        return emitter;
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

        // 1. 合并新添加的发射器
        synchronized (pendingEmitters) {
            if (!pendingEmitters.isEmpty()) {
                emitters.addAll(pendingEmitters);
                pendingEmitters.clear();
            }
        }

        Iterator<SnowstormEmitter> it = emitters.iterator();
        while (it.hasNext()) {
            SnowstormEmitter emitter = it.next();

            // 安全捕获 emitter 更新中的错误，防止单个粒子错误崩溃整个管理器
            try {
                emitter.tick(dt);
            } catch (Exception e) {
                LOGGER.error("[Snowstorm] Emitter tick error: ", e);
                emitter.markForRemoval(); // 出错的发射器标记为移除
            }

            // 移除被标记的发射器
            if (emitter.isMarkedForRemoval()) {
                it.remove();
            }
        }

        if (tickCounter % 20 == 0 && !emitters.isEmpty()) {
            int totalParticles = emitters.stream().mapToInt(SnowstormEmitter::getParticleCount).sum();
            // LOGGER.info("[Snowstorm] Active Emitters: {}, Total Particles: {}",
            // emitters.size(), totalParticles);
        }
    }

    public void clear() {
        emitters.clear();
        synchronized (pendingEmitters) {
            pendingEmitters.clear();
        }
        LOGGER.info("[Snowstorm] 已清除所有发射器。");
    }

    @SubscribeEvent
    public static void onClientLogout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent event) {
        INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onClientLogin(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent event) {
        INSTANCE.clear();
    }

    private final java.util.Map<String, org.Lcing.snowstorm_engine.definition.ParticleDefinition> particleRegistry = new java.util.HashMap<>();

    public void reloadParticles(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
        particleRegistry.clear();
        LOGGER.info("[Snowstorm] 正在重新加载粒子...");

        java.util.Collection<ResourceLocation> locations = resourceManager.listResources("snowstorm_engine/particles",
                path -> path.endsWith(".particle.json") || path.endsWith(".json"));

        for (ResourceLocation location : locations) {
            try {
                net.minecraft.server.packs.resources.Resource resource = resourceManager.getResource(location);
                try (java.io.InputStream is = resource.getInputStream()) {
                    org.Lcing.snowstorm_engine.definition.ParticleDefinition def = org.Lcing.snowstorm_engine.loader.ParticleLoader
                            .load(is);
                    String id = def != null ? def.getIdentifier() : null;
                    if (id != null) {
                        particleRegistry.put(id, def);
                        LOGGER.info("[Snowstorm] 已加载粒子 '{}' 来自 '{}'", id, location);
                    } else {
                        LOGGER.error("[Snowstorm] 无法从 '{}' 加载粒子: 缺少标识符 (identifier)", location);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[Snowstorm] 加载粒子 '{}' 时发生错误", location, e);
            }
        }
        LOGGER.info("[Snowstorm] 共加载 {} 个粒子。", particleRegistry.size());
    }

    public org.Lcing.snowstorm_engine.definition.ParticleDefinition getParticleDefinition(String identifier) {
        return particleRegistry.get(identifier);
    }

    public List<SnowstormEmitter> getEmitters() {
        return emitters;
    }
}
