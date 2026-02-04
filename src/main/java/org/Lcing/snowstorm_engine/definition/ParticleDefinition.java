package org.Lcing.snowstorm_engine.definition;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * 对应 .particle.json 文件的根结构
 * 使用 GSON 进行反序列化
 */
public class ParticleDefinition {

    @SerializedName("format_version")
    private String formatVersion;

    @SerializedName("particle_effect")
    private ParticleEffect section;

    public ParticleEffect getEffect() {
        return section;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    // 内部类：对应 "particle_effect" 对象
    public static class ParticleEffect {

        @SerializedName("description")
        private Description description;

        // 这里是关键！
        // 我们不把所有组件写死，而是用一个 Map 接收所有 key-value。
        // Key 是组件名（如 "minecraft:emitter_rate_steady"）
        // Value 是该组件的原始 JSON 数据（以便后续解析）
        @SerializedName("components")
        private Map<String, JsonElement> components;

        @SerializedName("curves")
        private Map<String, JsonElement> curves;

        public Description getDescription() {
            return description;
        }

        public Map<String, JsonElement> getComponents() {
            return components;
        }

        public Map<String, JsonElement> getCurves() {
            return curves;
        }
    }

    // 内部类：对应 "description" 对象
    public static class Description {
        @SerializedName("identifier")
        private String identifier;

        @SerializedName("basic_render_parameters")
        private BasicRenderParameters renderParameters;

        public String getIdentifier() {
            return identifier;
        }

        public BasicRenderParameters getRenderParameters() {
            return renderParameters;
        }
    }

    // 内部类：对应 "basic_render_parameters"
    public static class BasicRenderParameters {
        @SerializedName("material")
        private String material;

        @SerializedName("texture")
        private String texture;

        public String getTexturePath() {
            return texture;
        }

        public String getMaterial() {
            return material;
        }
    }
}
