package org.Lcing.snowstorm_engine.runtime;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages texture loading for Snowstorm particles.
 * Caches textures and handles resource pack paths.
 */
public class TextureManager {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    /**
     * Gets or creates a ResourceLocation for the given texture path.
     * Supports Bedrock-style paths (e.g., "textures/particle/particles")
     */
    @Nullable
    public static ResourceLocation getTexture(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        return TEXTURE_CACHE.computeIfAbsent(path, TextureManager::parseTexturePath);
    }

    private static ResourceLocation parseTexturePath(String path) {
        // Handle different path formats
        // Bedrock: "textures/particle/particles"
        // Full: "namespace:textures/particle/particles.png"

        String namespace = "minecraft";
        String texturePath = path;

        // Check for namespace separator
        if (path.contains(":")) {
            String[] parts = path.split(":", 2);
            namespace = parts[0];
            texturePath = parts[1];
        }

        // Ensure path starts with textures/
        if (!texturePath.startsWith("textures/")) {
            texturePath = "textures/" + texturePath;
        }

        // Ensure .png extension
        if (!texturePath.endsWith(".png")) {
            texturePath = texturePath + ".png";
        }

        return new ResourceLocation(namespace, texturePath);
    }

    /**
     * Binds the specified texture for rendering.
     */
    public static void bindTexture(ResourceLocation texture) {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().bindForSetup(texture);
        }
    }

    /**
     * Clears the texture cache.
     */
    public static void clearCache() {
        TEXTURE_CACHE.clear();
    }
}
