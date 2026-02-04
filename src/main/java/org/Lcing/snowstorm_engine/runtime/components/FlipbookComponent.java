package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

/**
 * Implements flipbook UV animation for particle_appearance_billboard.
 * Handles animated texture frames over particle lifetime.
 */
public class FlipbookComponent implements IParticleComponent {

    private IMolangExpression baseU, baseV;
    private IMolangExpression sizeU, sizeV;
    private IMolangExpression stepU, stepV;
    private IMolangExpression framesPerSecond;
    private IMolangExpression maxFrame;
    private boolean stretchToLifetime = false;
    private boolean loop = false;

    // Texture dimensions for UV normalization
    private float textureWidth = 128;
    private float textureHeight = 128;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject root = json.getAsJsonObject();
        JsonObject billboard = root.getAsJsonObject("minecraft:particle_appearance_billboard");
        if (billboard == null)
            return;

        JsonObject uv = billboard.getAsJsonObject("uv");
        if (uv == null)
            return;

        // Get texture dimensions
        if (uv.has("texture_width")) {
            textureWidth = uv.get("texture_width").getAsFloat();
        }
        if (uv.has("texture_height")) {
            textureHeight = uv.get("texture_height").getAsFloat();
        }

        JsonObject flipbook = uv.getAsJsonObject("flipbook");
        if (flipbook == null)
            return;

        // Parse base_UV [u, v]
        if (flipbook.has("base_UV") && flipbook.get("base_UV").isJsonArray()) {
            JsonArray arr = flipbook.getAsJsonArray("base_UV");
            baseU = MolangParser.parseJson(arr.get(0));
            baseV = MolangParser.parseJson(arr.get(1));
        } else {
            baseU = baseV = IMolangExpression.ZERO;
        }

        // Parse size_UV [u, v]
        if (flipbook.has("size_UV") && flipbook.get("size_UV").isJsonArray()) {
            JsonArray arr = flipbook.getAsJsonArray("size_UV");
            sizeU = MolangParser.parseJson(arr.get(0));
            sizeV = MolangParser.parseJson(arr.get(1));
        } else {
            sizeU = sizeV = IMolangExpression.constant(1);
        }

        // Parse step_UV [u, v]
        if (flipbook.has("step_UV") && flipbook.get("step_UV").isJsonArray()) {
            JsonArray arr = flipbook.getAsJsonArray("step_UV");
            stepU = MolangParser.parseJson(arr.get(0));
            stepV = MolangParser.parseJson(arr.get(1));
        } else {
            stepU = sizeU;
            stepV = IMolangExpression.ZERO;
        }

        // Parse frames_per_second
        framesPerSecond = MolangParser.parseJson(flipbook.get("frames_per_second"));
        if (framesPerSecond == null) {
            framesPerSecond = IMolangExpression.constant(8);
        }

        // Parse max_frame
        maxFrame = MolangParser.parseJson(flipbook.get("max_frame"));
        if (maxFrame == null) {
            maxFrame = IMolangExpression.constant(1);
        }

        // Parse stretch_to_lifetime
        if (flipbook.has("stretch_to_lifetime")) {
            stretchToLifetime = flipbook.get("stretch_to_lifetime").getAsBoolean();
        }

        // Parse loop
        if (flipbook.has("loop")) {
            loop = flipbook.get("loop").getAsBoolean();
        }
    }

    @Override
    public void onRenderParticle(SnowstormParticle particle, float partialTick) {
        if (baseU == null || baseV == null)
            return;

        var ctx = particle.getContext();

        float bu = baseU.eval(ctx);
        float bv = baseV.eval(ctx);
        float su = sizeU.eval(ctx);
        float sv = sizeV.eval(ctx);
        float stU = stepU.eval(ctx);
        float stV = stepV.eval(ctx);
        int max = (int) maxFrame.eval(ctx);

        // Calculate current frame
        int frame;
        if (stretchToLifetime) {
            // Frame based on age/lifetime ratio
            float progress = particle.age / particle.lifetime;
            frame = (int) (progress * max);
        } else {
            // Frame based on FPS
            float fps = framesPerSecond.eval(ctx);
            frame = (int) (particle.age * fps);
        }

        // Handle looping/clamping
        if (loop) {
            frame = frame % max;
        } else {
            frame = Math.min(frame, max - 1);
        }

        // Calculate UV coordinates (in texels)
        float u0 = bu + stU * frame;
        float v0 = bv + stV * frame;
        float u1 = u0 + su;
        float v1 = v0 + sv;

        // Normalize to 0-1 range
        particle.u0 = u0 / textureWidth;
        particle.v0 = v0 / textureHeight;
        particle.u1 = u1 / textureWidth;
        particle.v1 = v1 / textureHeight;
    }
}
