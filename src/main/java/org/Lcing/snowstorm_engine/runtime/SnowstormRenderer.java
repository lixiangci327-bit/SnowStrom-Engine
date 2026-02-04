package org.Lcing.snowstorm_engine.runtime;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.Lcing.snowstorm_engine.runtime.components.FacingCameraMode;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SnowstormRenderer {

    // Default texture just for testing
    private static final ResourceLocation DEBUG_TEXTURE = new ResourceLocation("minecraft", "textures/block/stone.png");

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Render after Translucent blocks so particles respect depth but handle
        // transparency
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();

        // Setup Render State
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // RenderSystem.disableDepthTest(); // Optional: Disable depth test if you want
        // them to draw over everything
        RenderSystem.depthMask(false); // Don't write to depth buffer
        RenderSystem.disableCull(); // IMPORTANT: Disable culling so we can see the back of the quad if needed

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        // CRITICAL: Backup current model-view matrix and apply PoseStack's matrix
        // This ensures particles are rendered in world space, not screen space
        RenderSystem.backupProjectionMatrix();
        Matrix4f modelViewMatrix = poseStack.last().pose();
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(modelViewMatrix);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Manual BufferBuilder to avoid Tessellator import issues
        BufferBuilder buffer = new BufferBuilder(512);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        // Iterate all particles
        for (SnowstormEmitter emitter : SnowstormManager.getInstance().getEmitters()) {
            // Bind the emitter's texture (or fallback to debug texture)
            ResourceLocation texture = emitter.getTextureLocation();
            if (texture == null) {
                texture = DEBUG_TEXTURE;
            }
            RenderSystem.setShaderTexture(0, texture);

            for (SnowstormParticle p : emitter.getParticles()) {
                // Pre-render updates (Animation etc)
                for (org.Lcing.snowstorm_engine.runtime.components.IParticleComponent comp : emitter.getComponents()) {
                    comp.onRenderParticle(p, event.getPartialTick());
                }
                renderParticle(buffer, p, poseStack, camPos, event.getPartialTick());
            }
        }

        buffer.end();
        com.mojang.blaze3d.vertex.BufferUploader.end(buffer);

        // Restore State
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderParticle(BufferBuilder buffer, SnowstormParticle p, PoseStack poseStack, Vec3 camPos,
            float partialTick) {
        // Interpolate position
        double ix = p.x;
        double iy = p.y;
        double iz = p.z;

        // Use Camera-Relative coordinates for Rotation Logic (Vector TO Camera)
        float relX = (float) (ix - camPos.x);
        float relY = (float) (iy - camPos.y);
        float relZ = (float) (iz - camPos.z);

        // Billboard Math (Camera Rotation)
        net.minecraft.client.Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        com.mojang.math.Quaternion rotation;

        // Quad Size (from particle)
        float sx = p.sizeX;
        float sy = p.sizeY;

        Vector3f[] corners = null;

        switch (p.renderMode) {
            case LOOKAT_DIRECTION:
                // Direction from velocity
                double len = Math.sqrt(p.vx * p.vx + p.vy * p.vy + p.vz * p.vz);
                if (len > 0.001) {
                    Vector3f dir = new Vector3f((float) (p.vx / len), (float) (p.vy / len), (float) (p.vz / len));
                    // Normal Bedrock usually: Y-axis of texture = Velocity.
                    // We want to construct a basis where Y = Dir.
                    // Z = Vector TO Particle (Facing Camera? Or just orthogonal?)
                    // Actually Bedrock docs say: "Oriented based on its movement direction".

                    // Let's use the standard approach:
                    // Y = Dir
                    // Z = (CamPos - ParticlePos).normalize() -> Vector TO Camera
                    // X = Y cross Z

                    Vector3f yAxis = dir;
                    Vector3f zAxis = new Vector3f(-relX, -relY, -relZ); // Vector TO Camera
                    zAxis.normalize();

                    Vector3f xAxis = new Vector3f(yAxis.x(), yAxis.y(), yAxis.z());
                    xAxis.cross(zAxis);
                    xAxis.normalize();

                    // Re-orthogonalize Z
                    zAxis = new Vector3f(xAxis.x(), xAxis.y(), xAxis.z());
                    zAxis.cross(yAxis);
                    zAxis.normalize();

                    // Construct Corners
                    corners = new Vector3f[4];
                    // (-sx, -sy) -> -xAxis * sx - yAxis * sy
                    corners[0] = new Vector3f(xAxis.x(), xAxis.y(), xAxis.z());
                    corners[0].mul(-sx);
                    Vector3f t0 = new Vector3f(yAxis.x(), yAxis.y(), yAxis.z());
                    t0.mul(-sy);
                    corners[0].add(t0);

                    corners[1] = new Vector3f(xAxis.x(), xAxis.y(), xAxis.z());
                    corners[1].mul(sx);
                    Vector3f t1 = new Vector3f(yAxis.x(), yAxis.y(), yAxis.z());
                    t1.mul(-sy);
                    corners[1].add(t1);

                    corners[2] = new Vector3f(xAxis.x(), xAxis.y(), xAxis.z());
                    corners[2].mul(sx);
                    Vector3f t2 = new Vector3f(yAxis.x(), yAxis.y(), yAxis.z());
                    t2.mul(sy);
                    corners[2].add(t2);

                    corners[3] = new Vector3f(xAxis.x(), xAxis.y(), xAxis.z());
                    corners[3].mul(-sx);
                    Vector3f t3 = new Vector3f(yAxis.x(), yAxis.y(), yAxis.z());
                    t3.mul(sy);
                    corners[3].add(t3);

                    rotation = null;
                } else {
                    rotation = camera.rotation(); // Fallback
                }
                break;
            case ROTATE_Y:
            case LOOKAT_Y:
                // Locked Y Axis
                // We want to face camera but only rotate around Y.
                // Z = (CamPos - ParticlePos) projected on XZ plane.

                float dx = -relX;
                float dz = -relZ;
                float angle = (float) Math.atan2(dx, dz); // Angle from Z axis

                // Construct simple Y-rotation quaternion
                rotation = com.mojang.math.Quaternion.fromXYZ(0, angle, 0);
                break;
            case DIRECTION_X:
                rotation = com.mojang.math.Quaternion.fromXYZ(0, 0, 0); // Fixed? Or rotating around X to face cam?
                // Usually means "Rotate around X to face camera"
                // But for now let's leave unimplemented or minimal.
                rotation = camera.rotation();
                break;
            case EMITTER_TRANSFORM_XY:
                // Plane aligned with Emitter XY (Normal = Z)
                // Since Emitter rotation is not yet supported, this is World XY.
                rotation = com.mojang.math.Quaternion.fromXYZ(0, 0, 0);
                break;
            case EMITTER_TRANSFORM_XZ:
                // Plane aligned with Emitter XZ (Normal = Y)
                // Rotate 90 deg around X to lay flat?
                // Standard Quad is XY. Rotation X +90 -> XZ.
                rotation = com.mojang.math.Quaternion.fromXYZ((float) Math.toRadians(90), 0, 0);
                break;
            case EMITTER_TRANSFORM_YZ:
                // Plane aligned with Emitter YZ (Normal = X)
                // Rotate 90 deg around Y? -> ZY.
                // Standard Quad XY. Rotate Y +90 -> ZY.
                rotation = com.mojang.math.Quaternion.fromXYZ(0, (float) Math.toRadians(90), 0);
                break;
            case ROTATE_XYZ:
            case LOOKAT_XYZ:
            default:
                // Standard Billboard (ROTATE_XYZ or LOOKAT_XYZ)
                rotation = camera.rotation();
                break;
        }

        if (rotation != null) {
            corners = new Vector3f[] {
                    new Vector3f(-sx, -sy, 0),
                    new Vector3f(sx, -sy, 0),
                    new Vector3f(sx, sy, 0),
                    new Vector3f(-sx, sy, 0)
            };

            for (int k = 0; k < 4; k++) {
                Vector3f v = corners[k];
                v.transform(rotation);
            }
        }

        // Translate to Particle Pos (Camera Relative)
        // Standard Minecraft Rendering uses Camera Relative coords to avoid precision
        // loss.
        // We must subtract the Camera Position.
        float finalX = (float) (ix - camPos.x);
        float finalY = (float) (iy - camPos.y);
        float finalZ = (float) (iz - camPos.z);

        // Apply offsets to get Relative Corners
        for (int k = 0; k < 4; k++) {
            Vector3f v = corners[k]; // These are OFFSETS
            v.add(finalX, finalY, finalZ); // Relative Pos of corner
        }

        // Add to Buffer with Dynamic UVs and Color
        // DO NOT use matrix here - camera-relative coords are already correct
        int r = (int) (p.colorR * 255);
        int g = (int) (p.colorG * 255);
        int b = (int) (p.colorB * 255);
        int a = (int) (p.colorA * 255);
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(p.u0, p.v1).color(r, g, b, a).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(p.u1, p.v1).color(r, g, b, a).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(p.u1, p.v0).color(r, g, b, a).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(p.u0, p.v0).color(r, g, b, a).endVertex();
    }
}
