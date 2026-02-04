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

public class SnowstormEmitter {
    // Core
    private final ParticleDefinition definition;
    private final MolangContext context;
    private final List<SnowstormParticle> particles = new ArrayList<>();
    private final List<IParticleComponent> components = new ArrayList<>();
    private final Map<String, SnowstormCurve> curves = new HashMap<>();

    // State
    private float age = 0;
    public boolean isSpawning = true;
    public double x, y, z; // World Position of Emitter

    // Local Space Settings
    public boolean localSpacePosition = false;
    public boolean localSpaceRotation = false;
    public boolean localSpaceVelocity = false;

    // Capacity
    private int maxParticles = 1000;

    // Texture & Material
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

        // Parse description for texture and material
        var desc = definition.getEffect().getDescription();
        if (desc != null) {
            var renderParams = desc.getRenderParameters();
            if (renderParams != null) {
                this.texturePath = renderParams.getTexturePath();
                this.materialType = MaterialType.fromString(renderParams.getMaterial());

                // Load texture
                if (this.texturePath != null) {
                    this.textureLocation = TextureManager.getTexture(this.texturePath);
                    System.out.println("[Snowstorm] Texture: " + this.texturePath + " -> " + this.textureLocation);
                }
            }
        }

        // Initialize Components
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

        // Parse Curves
        Map<String, JsonElement> defCurves = definition.getEffect().getCurves();
        if (defCurves != null) {
            for (Map.Entry<String, JsonElement> entry : defCurves.entrySet()) {
                SnowstormCurve curve = SnowstormCurve.fromJson(entry.getValue());
                if (curve != null) {
                    curves.put(entry.getKey(), curve);
                    System.out.println("[Snowstorm] Parsed curve: " + entry.getKey());
                }
            }
        }
        System.out.println("[Snowstorm] Total curves parsed: " + curves.size());
    }

    public void tick(float dt) {
        age += dt;
        context.setVariable("variable.emitter_age", age);

        // 1. Update Components (Emitter Logic)
        for (IParticleComponent comp : components) {
            comp.update(this, dt);
        }

        // 2. Update Particles
        Iterator<SnowstormParticle> it = particles.iterator();
        while (it.hasNext()) {
            SnowstormParticle p = it.next();

            // First: Update particle basic state and evaluate curves
            // This sets curve variables (e.g., variable.size) in the context
            p.update(dt, curves);

            // Then: Update components that may use curve variables
            for (IParticleComponent comp : components) {
                comp.updateParticle(p, dt);
            }

            if (p.isDead) {
                it.remove();
            }
        }
    }

    public void spawnParticle() {
        // Create Particle
        MolangContext pCtx = new MolangContext(); // Should inherit from emitter context?
        SnowstormParticle p = new SnowstormParticle(x, y, z, pCtx);

        // Initialize curves FIRST so curve variables are available for component
        // initialization
        p.initializeCurves(curves);

        // Initialize via Components
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
