package org.Lcing.snowstorm_engine.runtime;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理 Snowstorm 粒子的纹理加载。
 * 缓存纹理并处理资源包路径。
 */
public class TextureManager {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    /**
     * 获取或创建给定纹理路径的 ResourceLocation。
     * 支持基岩版风格路径 (例如 "textures/particle/particles")
     */
    @Nullable
    public static ResourceLocation getTexture(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        return TEXTURE_CACHE.computeIfAbsent(path, TextureManager::parseTexturePath);
    }

    private static ResourceLocation parseTexturePath(String path) {
        // 处理不同的路径格式
        // 基岩版: "textures/particle/particles"
        // 完整: "namespace:textures/particle/particles.png"

        String namespace = "minecraft";
        String texturePath = path;

        // 检查命名空间分隔符
        if (path.contains(":")) {
            String[] parts = path.split(":", 2);
            namespace = parts[0];
            texturePath = parts[1];
        }

        // 确保路径以 textures/ 开头
        if (!texturePath.startsWith("textures/")) {
            texturePath = "textures/" + texturePath;
        }

        // 确保 .png 扩展名
        if (!texturePath.endsWith(".png")) {
            texturePath = texturePath + ".png";
        }

        return new ResourceLocation(namespace, texturePath);
    }

    /**
     * 绑定指定的纹理用于渲染。
     */
    public static void bindTexture(ResourceLocation texture) {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().bindForSetup(texture);
        }
    }

    /**
     * 清除纹理缓存。
     */
    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}
