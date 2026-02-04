package org.Lcing.snowstorm_engine.runtime;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Handles material/blend mode configuration for Snowstorm particles.
 * Bedrock materials: particles_add, particles_alpha, particles_blend
 */
public enum MaterialType {
    PARTICLES_ADD {
        @Override
        public void apply() {
            // Additive blending: src + dst
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc(); // For now, use default (will override later)
            // True additive would be: glBlendFunc(GL_SRC_ALPHA, GL_ONE)
        }
    },

    PARTICLES_ALPHA {
        @Override
        public void apply() {
            // Alpha test: pixels with alpha=0 are transparent, others are opaque
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // In newer MC versions, alpha test is handled via shaders
        }
    },

    PARTICLES_BLEND {
        @Override
        public void apply() {
            // Standard alpha blending
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    };

    public abstract void apply();

    /**
     * Parse material type from JSON string.
     */
    public static MaterialType fromString(String name) {
        if (name == null) {
            return PARTICLES_BLEND;
        }

        switch (name.toLowerCase()) {
            case "particles_add":
                return PARTICLES_ADD;
            case "particles_alpha":
                return PARTICLES_ALPHA;
            case "particles_blend":
            default:
                return PARTICLES_BLEND;
        }
    }
}
