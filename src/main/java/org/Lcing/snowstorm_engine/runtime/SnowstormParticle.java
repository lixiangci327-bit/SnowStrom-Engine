package org.Lcing.snowstorm_engine.runtime;

import org.Lcing.snowstorm_engine.molang.MolangContext;

/**
 * 粒子实体类。
 * 存储单个粒子的所有状态数据（位置、速度、颜色、旋转等）。
 */
public class SnowstormParticle {
    public double x, y, z;
    public double prevX, prevY, prevZ;
    public double vx, vy, vz;

    // 生命周期
    public float age = 0;
    public float lifetime = 1.0f;
    public boolean isDead = false;

    // 渲染属性
    public float sizeX = 0.25f;
    public float sizeY = 0.25f;
    public float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
    public org.Lcing.snowstorm_engine.runtime.components.FacingCameraMode renderMode = org.Lcing.snowstorm_engine.runtime.components.FacingCameraMode.ROTATE_XYZ;

    // 旋转属性 (度)
    public float rotation = 0;
    public float rotationRate = 0; // 度/秒

    // 颜色 (RGBA, 0.0-1.0)
    public float colorR = 1.0f, colorG = 1.0f, colorB = 1.0f, colorA = 1.0f;

    // 光照
    public boolean useLighting = false;

    private final MolangContext context;

    public SnowstormParticle(double x, double y, double z, MolangContext context) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.context = context;

        // 初始化标准变量
        // 在实际实现中，我们应该在此处评估 "minecraft:particle_lifetime_expression"
        // 目前，如果未设置，我们默认随机 1-3 秒
        this.lifetime = 1.0f + context.getRandom().nextFloat() * 2.0f;

        // 设置此粒子的随机变量
        context.setVariable("variable.particle_random_1", context.getRandom().nextFloat());
        context.setVariable("variable.particle_random_2", context.getRandom().nextFloat());

        // 初始化 年龄/生命周期 以供第一帧曲线评估使用
        context.setVariable("variable.particle_age", 0f);
        context.setVariable("variable.particle_lifetime", lifetime);
    }

    /**
     * 在组件使用之前初始化曲线变量。
     * 必须在 onInitializeParticle() 之前调用，以确保基于曲线的尺寸表达式能正常工作。
     */
    public void initializeCurves(java.util.Map<String, SnowstormCurve> curves) {
        if (curves != null && !curves.isEmpty()) {
            for (java.util.Map.Entry<String, SnowstormCurve> entry : curves.entrySet()) {
                float val = entry.getValue().eval(context);
                context.setVariable(entry.getKey(), val);
                // System.out.println("[Snowstorm] Curve init: " + entry.getKey() + " = " +
                // val);
            }
        }
    }

    public void update(float dt, java.util.Map<String, SnowstormCurve> curves) {
        // 存储上一帧位置用于插值
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;

        // 更新年龄
        age += dt;
        context.setVariable("variable.particle_age", age);
        context.setVariable("variable.particle_lifetime", lifetime);

        if (age >= lifetime) {
            // 调试: 始终记录粒子死亡以诊断计时问题
            // System.out.println("[Snowstorm] DIED: age=" + String.format("%.3f", age) +
            // "s, lifetime=" + String.format("%.3f", lifetime) + "s");
            isDead = true;
            return;
        }

        // 评估曲线
        if (curves != null) {
            for (java.util.Map.Entry<String, SnowstormCurve> entry : curves.entrySet()) {
                float val = entry.getValue().eval(context);
                context.setVariable(entry.getKey(), val);
            }
        }

        // 注意: 位置更新 (pos += velocity * dt) 在 MotionDynamicComponent 中完成
        // 为了将物理计算与加速度保持在一起。

        // 应用旋转速率
        rotation += rotationRate * dt;

        // 更新 Molang 位置变量，以便其他表达式可以使用它们
        context.setVariable("variable.particle_x", (float) x);
        context.setVariable("variable.particle_y", (float) y);
        context.setVariable("variable.particle_z", (float) z);
        context.setVariable("variable.particle_rotation", rotation);
    }

    public MolangContext getContext() {
        return context;
    }
}
