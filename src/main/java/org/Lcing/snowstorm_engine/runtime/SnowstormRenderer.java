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

    // 默认纹理仅用于测试
    private static final ResourceLocation DEBUG_TEXTURE = new ResourceLocation("minecraft", "textures/block/stone.png");
    private static int debugCounter = 0;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // 在粒子之后渲染 (自定义效果的标准阶段，解决云遮挡问题)
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // RenderSystem.disableDepthTest(); // 可选: 禁用深度测试，如果你想让它们绘制在所有东西之上
        RenderSystem.depthMask(false); // 不写入深度缓冲区
        RenderSystem.disableCull(); // 重要: 禁用剔除，以便我们能看到四边形的背面（如果需要）

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        // 关键: 备份当前模型视图矩阵并应用 PoseStack 的矩阵
        // 这确保粒子在世界空间中渲染，而不是屏幕空间
        RenderSystem.backupProjectionMatrix();

        Matrix4f modelViewMatrix = poseStack.last().pose();

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(modelViewMatrix);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 使用 Tesselator 正确管理缓冲区内存
        com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        // 遍历所有粒子
        for (SnowstormEmitter emitter : SnowstormManager.getInstance().getEmitters()) {
            // 绑定发射器的纹理 (或回退到调试纹理)
            ResourceLocation texture = emitter.getTextureLocation();
            if (texture == null) {
                texture = DEBUG_TEXTURE;
            }
            RenderSystem.setShaderTexture(0, texture);

            for (SnowstormParticle p : emitter.getParticles()) {
                // 预渲染更新 (动画等)
                for (org.Lcing.snowstorm_engine.runtime.components.IParticleComponent comp : emitter.getComponents()) {
                    comp.onRenderParticle(p, event.getPartialTick());
                }
                renderParticle(buffer, p, poseStack, camPos, event.getPartialTick());
            }
        }

        tesselator.end();

        // 恢复状态
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderParticle(BufferBuilder buffer, SnowstormParticle p, PoseStack poseStack, Vec3 camPos,
            float partialTick) {
        // 插值位置
        double ix = p.prevX + (p.x - p.prevX) * partialTick;
        double iy = p.prevY + (p.y - p.prevY) * partialTick;
        double iz = p.prevZ + (p.z - p.prevZ) * partialTick;

        // 使用相机相对坐标进行旋转逻辑 (指向相机的向量)
        float relX = (float) (ix - camPos.x);
        float relY = (float) (iy - camPos.y);
        float relZ = (float) (iz - camPos.z);

        // 公告板数学 (相机旋转)
        net.minecraft.client.Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        com.mojang.math.Quaternion rotation;

        // 四边形尺寸 (来自粒子)
        float sx = p.sizeX;
        float sy = p.sizeY;

        Vector3f[] corners = null;

        switch (p.renderMode) {
            case LOOKAT_DIRECTION:
                // 基于速度的方向
                double len = Math.sqrt(p.vx * p.vx + p.vy * p.vy + p.vz * p.vz);
                if (len > 0.001) {
                    Vector3f dir = new Vector3f((float) (p.vx / len), (float) (p.vy / len), (float) (p.vz / len));
                    // 通常基岩版: 纹理的 Y 轴 = 速度方向。
                    // 我们想要构建一个基底，其中 Y = Dir。
                    // Z = 指向粒子的向量 (面向相机? 还是仅仅是正交?)
                    // 实际上基岩版文档说: "基于其运动方向定向"。

                    // 让我们使用标准方法:
                    // Y = Dir (方向)
                    // Z = (CamPos - ParticlePos).normalize() -> 指向相机的向量
                    // X = Y cross Z

                    Vector3f yAxis = dir;
                    Vector3f zAxis = new Vector3f(-relX, -relY, -relZ); // 指向相机的向量
                    zAxis.normalize();

                    Vector3f xAxis = new Vector3f(yAxis.x(), yAxis.y(), yAxis.z());
                    xAxis.cross(zAxis);
                    xAxis.normalize();

                    // 重新正交化 Z
                    zAxis = new Vector3f(xAxis.x(), xAxis.y(), xAxis.z());
                    zAxis.cross(yAxis);
                    zAxis.normalize();

                    // 构建角落
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
                    rotation = camera.rotation(); // 回退
                }
                break;
            case ROTATE_Y:
            case LOOKAT_Y:
                // 锁定 Y 轴
                // 我们想要面向相机，但只绕 Y 轴旋转。
                // Z = (CamPos - ParticlePos) 投影在 XZ 平面上。

                float dx = -relX;
                float dz = -relZ;
                float angle = (float) Math.atan2(dx, dz); // 来自 Z 轴的角度

                // 构建简单的 Y 轴旋转四元数
                rotation = com.mojang.math.Quaternion.fromXYZ(0, angle, 0);
                break;
            case DIRECTION_X:
                rotation = com.mojang.math.Quaternion.fromXYZ(0, 0, 0); // 修正？还是绕 X 轴旋转以面向相机？
                // 通常意味着 "绕 X 轴旋转以面向相机"
                // 但目前让我们保持未实现或最小化。
                rotation = camera.rotation();
                break;
            case EMITTER_TRANSFORM_XY:
                // 平面与发射器 XY 对齐 (法线 = Z)
                // 由于目前还不支持发射器旋转，这就是世界 XY。
                rotation = com.mojang.math.Quaternion.fromXYZ(0, 0, 0);
                break;
            case EMITTER_TRANSFORM_XZ:
                // 平面与发射器 XZ 对齐 (法线 = Y)
                // 绕 X 旋转 90 度以平放?
                // 标准四边形是 XY。旋转 X +90 -> XZ。
                rotation = com.mojang.math.Quaternion.fromXYZ((float) Math.toRadians(90), 0, 0);
                break;
            case EMITTER_TRANSFORM_YZ:
                // 平面与发射器 YZ 对齐 (法线 = X)
                // 绕 Y 旋转 90 度? -> ZY。
                // 标准四边形 XY。旋转 Y +90 -> ZY。
                rotation = com.mojang.math.Quaternion.fromXYZ(0, (float) Math.toRadians(90), 0);
                break;
            case ROTATE_XYZ:
            case LOOKAT_XYZ:
            default:
                // 标准公告板 (ROTATE_XYZ 或 LOOKAT_XYZ)
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

        // 平移到粒子位置 (相机相对)
        // 标准 Minecraft 渲染使用相机相对坐标以避免精度丢失。
        // 我们必须减去相机位置。
        float finalX = (float) (ix - camPos.x);
        float finalY = (float) (iy - camPos.y);
        float finalZ = (float) (iz - camPos.z);

        // 应用偏移以获得相对角落
        for (int k = 0; k < 4; k++) {
            Vector3f v = corners[k]; // 这些是偏移
            v.add(finalX, finalY, finalZ); // 角落的相对位置
        }

        // 添加到缓冲区，带有动态 UV 和颜色
        // 切记：此处不要使用矩阵 - 相机相对坐标已经是正确的
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
