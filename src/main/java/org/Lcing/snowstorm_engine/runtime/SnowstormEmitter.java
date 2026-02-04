package org.Lcing.snowstorm_engine.runtime;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.Lcing.snowstorm_engine.definition.ParticleDefinition;
import org.Lcing.snowstorm_engine.molang.MolangContext;
import org.Lcing.snowstorm_engine.runtime.components.ComponentRegistry;
import org.Lcing.snowstorm_engine.runtime.components.IParticleComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mojang.math.Vector4f;

/**
 * Snowstorm 发射器。
 * 负责管理一组粒子，处理它们的生成、更新和渲染状态。
 */
public class SnowstormEmitter {
    // 核心
    private final ParticleDefinition definition;
    private final MolangContext context;
    private final List<SnowstormParticle> particles = new ArrayList<>();
    private final List<IParticleComponent> components = new ArrayList<>();
    private final Map<String, SnowstormCurve> curves = new HashMap<>();

    // 状态
    private float age = 0;
    public boolean isSpawning = true;
    public double x, y, z; // 发射器世界坐标

    // 本地空间设置
    public boolean localSpacePosition = false;
    public boolean localSpaceRotation = false;
    public boolean localSpaceVelocity = false;

    // 容量
    private int maxParticles = 1000;

    // 纹理与材质
    private String texturePath;
    private ResourceLocation textureLocation;
    private MaterialType materialType = MaterialType.PARTICLES_BLEND;

    public void setMaxParticles(int max) {
        this.maxParticles = max;
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public SnowstormEmitter(ParticleDefinition definition) {
        this.definition = definition;
        this.context = new MolangContext();

        // 解析描述中的纹理和材质
        var desc = definition.getEffect().getDescription();
        if (desc != null) {
            var renderParams = desc.getRenderParameters();
            if (renderParams != null) {
                this.texturePath = renderParams.getTexturePath();
                this.materialType = MaterialType.fromString(renderParams.getMaterial());

                // 加载纹理
                if (this.texturePath != null) {
                    this.textureLocation = TextureManager.getTexture(this.texturePath);
                    // System.out.println("[Snowstorm] Texture: " + this.texturePath + " -> " +
                    // this.textureLocation);
                }
            }
        }

        // 初始化组件
        Map<String, JsonElement> defComponents = definition.getEffect().getComponents();
        if (defComponents != null) {
            for (Map.Entry<String, JsonElement> entry : defComponents.entrySet()) {
                String key = entry.getKey();
                IParticleComponent comp = ComponentRegistry.create(key);
                if (comp != null) {
                    comp.fromJson(entry.getValue());
                    components.add(comp);
                } else {
                    // System.out.println("Unknown component: " + key);
                }
            }
        }

        // 解析曲线
        Map<String, JsonElement> defCurves = definition.getEffect().getCurves();
        if (defCurves != null) {
            for (Map.Entry<String, JsonElement> entry : defCurves.entrySet()) {
                SnowstormCurve curve = SnowstormCurve.fromJson(entry.getValue());
                if (curve != null) {
                    curves.put(entry.getKey(), curve);
                    // System.out.println("[Snowstorm] Parsed curve: " + entry.getKey());
                }
            }
        }
        // System.out.println("[Snowstorm] Total curves parsed: " + curves.size());
    }

    private java.util.function.Supplier<com.mojang.math.Matrix4f> transformProvider;
    private com.mojang.math.Matrix4f lastTransform;

    public void setTransformProvider(java.util.function.Supplier<com.mojang.math.Matrix4f> provider) {
        this.transformProvider = provider;
        this.updateFromProvider();
    }

    public com.mojang.math.Matrix4f getLastTransform() {
        return lastTransform;
    }

    private void updateFromProvider() {
        if (transformProvider != null) {
            com.mojang.math.Matrix4f mat = transformProvider.get();
            if (mat != null) {
                this.lastTransform = mat;
                // Matrix4f 平移分量: m03 (x), m13 (y), m23 (z)
                // 通过 Access Transformer 暴露
                this.x = mat.m03;
                this.y = mat.m13;
                this.z = mat.m23;
            }
        }
    }

    public void tick(float dt) {
        double prevX = this.x;
        double prevY = this.y;
        double prevZ = this.z;

        if (transformProvider != null) {
            updateFromProvider();
        }

        // 计算发射器的位移增量
        double dx = this.x - prevX;
        double dy = this.y - prevY;
        double dz = this.z - prevZ;

        age += dt;
        context.setVariable("variable.emitter_age", age);

        // 1. 更新组件 (发射器逻辑)
        for (IParticleComponent comp : components) {
            comp.update(this, dt);
        }

        // 2. 更新粒子
        Iterator<SnowstormParticle> it = particles.iterator();
        while (it.hasNext()) {
            SnowstormParticle p = it.next();

            // 处理本地空间: 如果发射器移动了，粒子也随之移动
            if (this.localSpacePosition) {
                p.x += dx;
                p.y += dy;
                p.z += dz;
                p.prevX += dx;
                p.prevY += dy;
                p.prevZ += dz;
            }

            // 首先: 更新粒子基本状态并评估曲线
            // 这将在上下文中设置曲线变量 (例如 variable.size)
            p.update(dt, curves);

            // 然后: 更新可能使用了曲线变量的组件
            for (IParticleComponent comp : components) {
                comp.updateParticle(p, dt);
            }

            if (p.isDead) {
                it.remove();
            }
        }
    }

    public void spawnParticle() {
        // 创建粒子
        MolangContext pCtx = new MolangContext(); // 应该继承发射器的上下文吗？
        SnowstormParticle p = new SnowstormParticle(x, y, z, pCtx);

        // 首先初始化曲线，以便曲线变量可用于组件初始化
        p.initializeCurves(curves);

        // 通过组件初始化
        for (IParticleComponent comp : components) {
            comp.onInitializeParticle(p);
        }

        particles.add(p);
    }

    public List<SnowstormParticle> getParticles() {
        return particles;
    }

    public int getParticleCount() {
        return particles.size();
    }

    public MolangContext getContext() {
        return context;
    }

    public List<IParticleComponent> getComponents() {
        return components;
    }

    public Map<String, SnowstormCurve> getCurves() {
        return curves;
    }

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }

    private float maxLifetime = Float.MAX_VALUE;
    private boolean markedForRemoval = false;

    public float getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(float maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public void markForRemoval() {
        this.markedForRemoval = true;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }
}
