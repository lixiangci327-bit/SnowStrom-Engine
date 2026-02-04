package org.Lcing.snowstorm_engine.runtime;

import org.Lcing.snowstorm_engine.molang.MolangContext;

public class SnowstormParticle {
    public double x, y, z;
    public double vx, vy, vz;

    // Lifecycle
    public float age = 0;
    public float lifetime = 1.0f;
    public boolean isDead = false;

    // Render Fields
    public float sizeX = 0.25f;
    public float sizeY = 0.25f;
    public float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
    public org.Lcing.snowstorm_engine.runtime.components.FacingCameraMode renderMode = org.Lcing.snowstorm_engine.runtime.components.FacingCameraMode.ROTATE_XYZ;

    // Spin Fields (degrees)
    public float rotation = 0;
    public float rotationRate = 0; // degrees/second

    // Color (RGBA, 0.0-1.0)
    public float colorR = 1.0f, colorG = 1.0f, colorB = 1.0f, colorA = 1.0f;

    // Lighting
    public boolean useLighting = false;

    private final MolangContext context;

    public SnowstormParticle(double x, double y, double z, MolangContext context) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.context = context;

        // Initialize standard variables
        // In a real implementation, we would evaluate
        // "minecraft:particle_lifetime_expression" here
        // For now, we default to 1-3 seconds randomly if not set
        this.lifetime = 1.0f + context.getRandom().nextFloat() * 2.0f;

        // Set random variables for this particle
        context.setVariable("variable.particle_random_1", context.getRandom().nextFloat());
        context.setVariable("variable.particle_random_2", context.getRandom().nextFloat());

        // Initialize age/lifetime for first-frame curve evaluation
        context.setVariable("variable.particle_age", 0f);
        context.setVariable("variable.particle_lifetime", lifetime);
    }

    /**
     * Initialize curve variables before components use them.
     * Must be called before onInitializeParticle() so curve-based size expressions
     * work.
     */
    public void initializeCurves(java.util.Map<String, SnowstormCurve> curves) {
        if (curves != null && !curves.isEmpty()) {
            for (java.util.Map.Entry<String, SnowstormCurve> entry : curves.entrySet()) {
                float val = entry.getValue().eval(context);
                context.setVariable(entry.getKey(), val);
                System.out.println("[Snowstorm] Curve init: " + entry.getKey() + " = " + val);
            }
        }
    }

    public void update(float dt, java.util.Map<String, SnowstormCurve> curves) {
        // Update Age
        age += dt;
        context.setVariable("variable.particle_age", age);
        context.setVariable("variable.particle_lifetime", lifetime);

        if (age >= lifetime) {
            // Debug: Always log particle death to diagnose timing
            System.out.println("[Snowstorm] DIED: age=" +
                    String.format("%.3f", age) + "s, lifetime=" +
                    String.format("%.3f", lifetime) + "s");
            isDead = true;
            return;
        }

        // Evaluate Curves
        if (curves != null) {
            for (java.util.Map.Entry<String, SnowstormCurve> entry : curves.entrySet()) {
                float val = entry.getValue().eval(context);
                context.setVariable(entry.getKey(), val);
            }
        }

        // NOTE: Position update (pos += velocity * dt) is done in
        // MotionDynamicComponent
        // to keep physics calculations together with acceleration.

        // Apply rotation rate
        rotation += rotationRate * dt;

        // Update Molang position variables so other expressions can use them
        context.setVariable("variable.particle_x", (float) x);
        context.setVariable("variable.particle_y", (float) y);
        context.setVariable("variable.particle_z", (float) z);
        context.setVariable("variable.particle_rotation", rotation);
    }

    public MolangContext getContext() {
        return context;
    }
}
