package org.Lcing.snowstorm_engine.test;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SnowstormTestRenderer extends GeoEntityRenderer<SnowstormTestEntity> {
    public SnowstormTestRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SnowstormTestModel());
    }
}
