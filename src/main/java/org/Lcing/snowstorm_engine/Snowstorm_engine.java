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

// The value here should match an entry in the META-INF/mods.toml file
@Mod("snowstorm_engine")
public class Snowstorm_engine {

        // Directly reference a slf4j logger
        private static final Logger LOGGER = LogUtils.getLogger();

        public Snowstorm_engine() {
                // Register the setup method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
                // Register the enqueueIMC method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
                // Register the processIMC method for modloading
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

                // Register ourselves for server and other game events we are interested in
                MinecraftForge.EVENT_BUS.register(this);
        }

        private void setup(final FMLCommonSetupEvent event) {
                // Some preinit code
                LOGGER.info("HELLO FROM PREINIT");
                LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

                // Register Networking
                org.Lcing.snowstorm_engine.network.PacketHandler.register();

                // Initialize Snowstorm Components
                LOGGER.info("Initializing Snowstorm Components...");
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

                // Phase 2 Components
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

                // Phase 3 - Full Compatibility Components
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
                // Some example code to dispatch IMC to another mod
                InterModComms.sendTo("snowstorm_engine", "helloworld", () -> {
                        LOGGER.info("Hello world from the MDK");
                        return "Hello world";
                });
        }

        private void processIMC(final InterModProcessEvent event) {
                // Some example code to receive and process InterModComms from other mods
                LOGGER.info("Got IMC {}",
                                event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
        }

        @SubscribeEvent
        public void onRegisterCommands(RegisterCommandsEvent event) {
                org.Lcing.snowstorm_engine.command.SnowstormCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
                // Do something when the server starts
                LOGGER.info("HELLO from server starting");
        }

        // You can use EventBusSubscriber to automatically subscribe events on the
        // contained class (this is subscribing to the MOD
        // Event bus for receiving Registry Events)
        @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
        public static class RegistryEvents {
                @SubscribeEvent
                public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
                        // Register a new block here
                        LOGGER.info("HELLO from Register Block");
                }
        }
}
