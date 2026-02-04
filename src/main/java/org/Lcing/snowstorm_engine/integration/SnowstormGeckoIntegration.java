package org.Lcing.snowstorm_engine.integration;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.ParticleKeyFrameEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.util.AnimationUtils;
import org.Lcing.snowstorm_engine.command.SnowstormCommand;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;

import java.util.function.Supplier;

public class SnowstormGeckoIntegration {

    /**
     * Registers the Snowstorm particle listener to the given AnimationController.
     * Use this in your entity's registerControllers method.
     */
    public static <T extends IAnimatable> void register(AnimationController<T> controller) {
        controller.registerParticleListener(SnowstormGeckoIntegration::onParticleEvent);
    }

    private static <T extends IAnimatable> void onParticleEvent(ParticleKeyFrameEvent<T> event) {
        // Only run on client (though particles usually are client-only anyway)
        // KeyFrameEvent.getEntity() is public
        Entity entity = (Entity) event.getEntity();
        if (!entity.level.isClientSide) {
            return;
        }

        String particleId = event.effect;
        String locatorName = event.locator;

        // 1. Resolve Transform Provider (Locator or Entity Root)
        Supplier<Matrix4f> transformProvider = null;

        if (locatorName != null && !locatorName.isEmpty()) {
            var modelProvider = AnimationUtils.getGeoModelForEntity(entity);
            if (modelProvider instanceof AnimatedGeoModel) {
                AnimatedGeoModel<?> geoModel = (AnimatedGeoModel<?>) modelProvider;
                AnimationProcessor<?> processor = geoModel.getAnimationProcessor();

                // First, try direct bone lookup (in case locator was converted to bone)
                IBone bone = processor.getBone(locatorName);

                if (bone instanceof GeoBone) {
                    GeoBone geoBone = (GeoBone) bone;
                    transformProvider = () -> {
                        return geoBone.getWorldSpaceXform().copy();
                    };
                } else {
                    // Locator not found as bone - use LocatorIndexManager to find parent bone
                    LocatorIndexManager.LocatorInfo locatorInfo = LocatorIndexManager.getInstance()
                            .getLocator(locatorName);

                    if (locatorInfo != null) {
                        // Found locator in index - get parent bone
                        IBone parentBone = processor.getBone(locatorInfo.parentBoneName);

                        if (parentBone instanceof GeoBone) {
                            GeoBone parentGeoBone = (GeoBone) parentBone;
                            // Capture offset values for lambda
                            final double ox = locatorInfo.offsetX;
                            final double oy = locatorInfo.offsetY;
                            final double oz = locatorInfo.offsetZ;

                            transformProvider = () -> {
                                Matrix4f parentMatrix = parentGeoBone.getWorldSpaceXform().copy();
                                // Apply local offset: transform the offset by parent matrix
                                // Note: GeckoLib uses a different coordinate system
                                // Offset X is inverted in GeckoLib (same as pivot)
                                Vector4f offsetVec = new Vector4f((float) -ox, (float) oy, (float) oz, 1.0f);
                                offsetVec.transform(parentMatrix);

                                // Create a translation matrix at the offset position
                                Matrix4f result = new Matrix4f();
                                result.setIdentity();
                                result.multiply(
                                        Matrix4f.createTranslateMatrix(offsetVec.x(), offsetVec.y(), offsetVec.z()));
                                return result;
                            };
                            // System.out.println("[Snowstorm] Using locator '" + locatorName + "' via
                            // parent bone '" + locatorInfo.parentBoneName + "'");
                        } else {
                            // System.out.println("[Snowstorm] Parent bone not found for locator: " +
                            // locatorName + " (parent: " + locatorInfo.parentBoneName + ")");
                        }
                    } else {
                        // System.out.println("[Snowstorm] Locator not found in index: " + locatorName);
                    }
                }
            }
        }

        // Fallback: Use entity position if no locator specified or found
        if (transformProvider == null) {
            // Create a dynamic provider that follows the entity position directly
            transformProvider = () -> {
                Matrix4f mat = new Matrix4f();
                mat.setIdentity();
                // Safe way to translate: Create translation matrix and multiply
                mat.multiply(com.mojang.math.Matrix4f.createTranslateMatrix((float) entity.getX(),
                        (float) entity.getY(), (float) entity.getZ()));
                return mat;
            };
        }

        // 2. Spawn Emitter
        // Use SnowstormManager directly
        org.Lcing.snowstorm_engine.runtime.SnowstormManager manager = org.Lcing.snowstorm_engine.runtime.SnowstormManager
                .getInstance();
        SnowstormEmitter emitter = manager.createEmitter(particleId, 0, 0, 0);

        if (emitter != null) {
            emitter.setTransformProvider(transformProvider);
            // Force an initial update so the emitter snaps to the correct position
            // immediately
            emitter.tick(0);
        } else {
            // System.out.println("[Snowstorm] Failed to spawn particle for event: " +
            // particleId);
            // particleId);
        }
    }
}
