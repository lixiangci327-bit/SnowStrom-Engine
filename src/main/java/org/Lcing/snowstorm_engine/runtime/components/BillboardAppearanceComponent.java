package org.Lcing.snowstorm_engine.runtime.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.Lcing.snowstorm_engine.molang.IMolangExpression;
import org.Lcing.snowstorm_engine.molang.MolangParser;
import org.Lcing.snowstorm_engine.runtime.SnowstormParticle;

public class BillboardAppearanceComponent implements IParticleComponent {

    // 尺寸表达式（可以是 Molang）
    private IMolangExpression sizeX = IMolangExpression.constant(0.25f);
    private IMolangExpression sizeY = IMolangExpression.constant(0.25f);

    // UV 逻辑
    private int textureWidth = 16;
    private int textureHeight = 16;

    // Facing
    private String facingMode = "lookat_xyz"; // 默认值

    // 翻书动画
    private boolean isFlipbook = false;
    private float[] baseUV = new float[] { 0, 0 };
    private float[] sizeUV = new float[] { 16, 16 };
    private float[] stepUV = new float[] { 0, 16 };
    private float fps = 20;
    private float maxFrame = 0;
    private boolean loop = false;

    // 静态 UV（用于非翻书动画）
    private float staticU0 = 0, staticV0 = 0, staticU1 = 1, staticV1 = 1;

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonObject())
            return;
        JsonObject obj = json.getAsJsonObject();

        // 尺寸 - 可以是 Molang 表达式
        if (obj.has("size")) {
            JsonArray arr = obj.getAsJsonArray("size");
            if (arr.size() >= 2) {
                sizeX = MolangParser.parseJson(arr.get(0));
                sizeY = MolangParser.parseJson(arr.get(1));
            }
        }

        if (obj.has("facing_camera_mode")) {
            facingMode = obj.get("facing_camera_mode").getAsString();
        }

        // UV
        if (obj.has("uv")) {
            JsonObject uvConfig = obj.getAsJsonObject("uv");
            if (uvConfig.has("texture_width"))
                textureWidth = uvConfig.get("texture_width").getAsInt();
            if (uvConfig.has("texture_height"))
                textureHeight = uvConfig.get("texture_height").getAsInt();

            // 静态 UV（非翻书动画）
            if (uvConfig.has("uv") && uvConfig.has("uv_size")) {
                JsonArray uvArr = uvConfig.getAsJsonArray("uv");
                JsonArray sizeArr = uvConfig.getAsJsonArray("uv_size");

                float u = uvArr.get(0).getAsFloat();
                float v = uvArr.get(1).getAsFloat();
                float w = sizeArr.get(0).getAsFloat();
                float h = sizeArr.get(1).getAsFloat();

                staticU0 = u / textureWidth;
                staticV0 = v / textureHeight;
                staticU1 = (u + w) / textureWidth;
                staticV1 = (v + h) / textureHeight;
            }

            if (uvConfig.has("flipbook")) {
                isFlipbook = true;
                JsonObject fb = uvConfig.getAsJsonObject("flipbook");
                parseUV(fb, "base_UV", baseUV);
                parseUV(fb, "size_UV", sizeUV);
                parseUV(fb, "step_UV", stepUV);
                if (fb.has("frames_per_second"))
                    fps = fb.get("frames_per_second").getAsFloat();
                if (fb.has("max_frame"))
                    maxFrame = fb.get("max_frame").getAsFloat();
                if (fb.has("loop"))
                    loop = fb.get("loop").getAsBoolean();
            }
        }
    }

    private void parseUV(JsonObject obj, String key, float[] target) {
        if (obj.has(key)) {
            JsonArray arr = obj.getAsJsonArray(key);
            target[0] = arr.get(0).getAsFloat();
            target[1] = arr.get(1).getAsFloat();
        }
    }

    @Override
    public void onInitializeParticle(SnowstormParticle particle) {
        // 评估尺寸表达式
        var ctx = particle.getContext();
        particle.sizeX = sizeX.eval(ctx);
        particle.sizeY = sizeY.eval(ctx);
        particle.renderMode = FacingCameraMode.fromString(facingMode);

        // System.out.println("[Snowstorm] Particle size: " + particle.sizeX + " x " +
        // particle.sizeY);

        // 设置初始 UV
        if (!isFlipbook) {
            particle.u0 = staticU0;
            particle.v0 = staticV0;
            particle.u1 = staticU1;
            particle.v1 = staticV1;
        } else {
            updateFlipbookUV(particle);
        }
    }

    @Override
    public void updateParticle(SnowstormParticle particle, float dt) {
        // 动态更新尺寸（用于动画尺寸）
        var ctx = particle.getContext();
        particle.sizeX = sizeX.eval(ctx);
        particle.sizeY = sizeY.eval(ctx);

        if (isFlipbook) {
            updateFlipbookUV(particle);
        }
    }

    private void updateFlipbookUV(SnowstormParticle p) {
        float frameIndex = 0;
        float totalTime = p.age;

        if (fps > 0) {
            frameIndex = (int) (totalTime * fps);
        }

        if (loop && maxFrame > 0) {
            frameIndex = frameIndex % maxFrame;
        } else if (maxFrame > 0) {
            frameIndex = Math.min(frameIndex, maxFrame - 1);
        }

        float uStart = baseUV[0] + stepUV[0] * frameIndex;
        float vStart = baseUV[1] + stepUV[1] * frameIndex;

        // 将像素转换为 0-1 UV
        p.u0 = uStart / (float) textureWidth;
        p.v0 = vStart / (float) textureHeight;
        p.u1 = (uStart + sizeUV[0]) / (float) textureWidth;
        p.v1 = (vStart + sizeUV[1]) / (float) textureHeight;
    }
}
