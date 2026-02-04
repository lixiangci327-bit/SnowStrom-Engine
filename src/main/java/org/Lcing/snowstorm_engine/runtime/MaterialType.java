package org.Lcing.snowstorm_engine.runtime;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * 处理 Snowstorm 粒子的材质/混合模式配置。
 * 基岩版材质：particles_add, particles_alpha, particles_blend
 */
public enum MaterialType {
    PARTICLES_ADD {
        @Override
        public void apply() {
            // 叠加混合: src + dst
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc(); // 目前使用默认值（稍后将覆盖）
            // 真正的叠加应为: glBlendFunc(GL_SRC_ALPHA, GL_ONE)
        }
    },

    PARTICLES_ALPHA {
        @Override
        public void apply() {
            // Alpha 测试: alpha=0 的像素透明，其他不透明
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // 在较新的 MC 版本中，Alpha 测试通过着色器处理
        }
    },

    PARTICLES_BLEND {
        @Override
        public void apply() {
            // 标准 Alpha 混合
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
    };

    public abstract void apply();

    /**
     * 从 JSON 字符串解析材质类型。
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
