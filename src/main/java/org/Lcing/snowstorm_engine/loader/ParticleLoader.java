package org.Lcing.snowstorm_engine.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.Lcing.snowstorm_engine.definition.ParticleDefinition;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ParticleLoader {

    // 创建 Gson 实例，配置为宽松模式 (如果需要，接受非标准 JSON)
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .create();

    /**
     * 将 .particle.json 输入流解析为 ParticleDefinition 对象。
     * 
     * @param inputStream 包含 JSON 数据的数据流
     * @return 解析后的粒子定义对象
     * @throws RuntimeException 如果解析失败
     */
    public static ParticleDefinition load(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            ParticleDefinition definition = GSON.fromJson(reader, ParticleDefinition.class);

            // 基本验证，确保它可以是一个有效的粒子文件
            if (definition == null || definition.getEffect() == null) {
                throw new IllegalArgumentException("无效的粒子 JSON 结构: 缺少 'particle_effect'");
            }

            return definition;
        } catch (Exception e) {
            throw new RuntimeException("解码粒子定义失败", e);
        }
    }
}
