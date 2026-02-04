package org.Lcing.snowstorm_engine.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = "snowstorm_engine", bus = Mod.EventBusSubscriber.Bus.MOD)
public class TestEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES,
            "snowstorm_engine");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            "snowstorm_engine");

    public static final RegistryObject<EntityType<SnowstormTestEntity>> TEST_ENTITY = ENTITIES.register(
            "snowstorm_test_entity",
            () -> EntityType.Builder.of(SnowstormTestEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .build(new ResourceLocation("snowstorm_engine", "snowstorm_test_entity").toString()));

    public static final RegistryObject<Item> TEST_ENTITY_EGG = ITEMS.register("snowstorm_test_entity_spawn_egg",
            () -> new ForgeSpawnEggItem(TEST_ENTITY, 0xE7E7E7, 0xFFB5B5,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_ENTITY.get(), SnowstormTestEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TEST_ENTITY.get(), SnowstormTestRenderer::new);
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
    }
}
