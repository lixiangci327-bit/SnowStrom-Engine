package org.Lcing.snowstorm_engine;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.stream.Collectors;

// 此处的值应与 META-INF/mods.toml 文件中的 Mod ID 匹配
@Mod("snowstorm_engine")
public class Snowstorm_engine {

        // 直接引用 slf4j 日志记录器
        private static final Logger LOGGER = LogUtils.getLogger();

        public Snowstorm_engine() {
                // 注册 setup 方法以进行模组加载
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
                // 注册 enqueueIMC 方法以进行模组加载
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
                // 注册 processIMC 方法以进行模组加载
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

                // 将我们自己注册到服务器和其他感兴趣的游戏事件中
                MinecraftForge.EVENT_BUS.register(this);

                // 注册测试实体
                org.Lcing.snowstorm_engine.test.TestEntityRegistry
                                .register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        private void setup(final FMLCommonSetupEvent event) {
                // 预初始化代码
                LOGGER.info("HELLO FROM PREINIT");
                LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

                // 注册网络包
                org.Lcing.snowstorm_engine.network.PacketHandler.register();

                // 初始化 Snowstorm 组件
                LOGGER.info("正在初始化 Snowstorm 组件...");
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_rate_steady",
                                org.Lcing.snowstorm_engine.runtime.components.RateSteadyComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_lifetime_looping",
                                org.Lcing.snowstorm_engine.runtime.components.LifetimeLoopingComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_shape_point",
                                org.Lcing.snowstorm_engine.runtime.components.ShapePointComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_lifetime_expression",
                                org.Lcing.snowstorm_engine.runtime.components.ParticleLifetimeComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_initial_speed",
                                org.Lcing.snowstorm_engine.runtime.components.InitialSpeedComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_motion_dynamic",
                                org.Lcing.snowstorm_engine.runtime.components.MotionDynamicComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_appearance_billboard",
                                org.Lcing.snowstorm_engine.runtime.components.BillboardAppearanceComponent::new);

                // 第二阶段组件
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_rate_instant",
                                org.Lcing.snowstorm_engine.runtime.components.RateInstantComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_lifetime_once",
                                org.Lcing.snowstorm_engine.runtime.components.LifetimeOnceComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_shape_sphere",
                                org.Lcing.snowstorm_engine.runtime.components.ShapeSphereComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_initial_spin",
                                org.Lcing.snowstorm_engine.runtime.components.InitialSpinComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register("minecraft:emitter_shape_box",
                                org.Lcing.snowstorm_engine.runtime.components.ShapeBoxComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_appearance_tinting",
                                org.Lcing.snowstorm_engine.runtime.components.TintingComponent::new);

                // 第三阶段 - 完全兼容组件
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_local_space",
                                org.Lcing.snowstorm_engine.runtime.components.LocalSpaceComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_shape_disc",
                                org.Lcing.snowstorm_engine.runtime.components.ShapeDiscComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_shape_custom",
                                org.Lcing.snowstorm_engine.runtime.components.ShapeCustomComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_shape_entity_aabb",
                                org.Lcing.snowstorm_engine.runtime.components.ShapeEntityAABBComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_rate_manual",
                                org.Lcing.snowstorm_engine.runtime.components.RateManualComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:emitter_lifetime_expression",
                                org.Lcing.snowstorm_engine.runtime.components.LifetimeExpressionComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_motion_parametric",
                                org.Lcing.snowstorm_engine.runtime.components.MotionParametricComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_motion_collision",
                                org.Lcing.snowstorm_engine.runtime.components.MotionCollisionComponent::new);
                org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry.register(
                                "minecraft:particle_appearance_lighting",
                                org.Lcing.snowstorm_engine.runtime.components.LightingComponent::new);
        }

        private void enqueueIMC(final InterModEnqueueEvent event) {
                // 分发 IMC 到另一个模组的示例代码
                InterModComms.sendTo("snowstorm_engine", "helloworld", () -> {
                        LOGGER.info("Hello world from the MDK");
                        return "Hello world";
                });
        }

        private void processIMC(final InterModProcessEvent event) {
                // 接收并处理来自其他模组的 IMC 的示例代码
                LOGGER.info("Got IMC {}",
                                event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
        }

        @SubscribeEvent
        public void onRegisterCommands(RegisterCommandsEvent event) {
                org.Lcing.snowstorm_engine.command.SnowstormCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
                // 服务器启动时做一些事
                LOGGER.info("HELLO from server starting");
        }

        // 你可以使用 EventBusSubscriber 自动在包含的类上订阅事件
        // (这是订阅模组事件总线以接收注册表事件)
        @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
        public static class RegistryEvents {
                @SubscribeEvent
                public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
                        // 在此处注册新方块
                        LOGGER.info("HELLO from Register Block");
                }
        }
}
