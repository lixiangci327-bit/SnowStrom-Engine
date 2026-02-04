package org.Lcing.snowstorm_engine.test;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SnowstormTestModel extends AnimatedGeoModel<SnowstormTestEntity> {
    @Override
    public ResourceLocation getModelLocation(SnowstormTestEntity object) {
        return new ResourceLocation("snowstorm_engine", "geo/test1.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SnowstormTestEntity object) {
        return new ResourceLocation("snowstorm_engine", "textures/item/test_icon.png"); // Fallback texture
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SnowstormTestEntity animatable) {
        return new ResourceLocation("snowstorm_engine", "animations/test1.animation.json");
    }
}
