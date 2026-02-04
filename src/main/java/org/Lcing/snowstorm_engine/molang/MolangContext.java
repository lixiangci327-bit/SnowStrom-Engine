package org.Lcing.snowstorm_engine.molang;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Molang 执行上下文。
 * 存储变量、随机种子，并提供环境查询。
 */
public class MolangContext {
    private final Map<String, Float> variables = new HashMap<>();
    private final Random random = new Random();

    public void setVariable(String name, float value) {
        variables.put(name, value);
    }

    public float getVariable(String name) {
        return variables.getOrDefault(name, 0.0f);
    }

    /**
     * 解析一个值。如果需要，处理标准变量和特殊查询。
     */
    public float resolve(String name) {
        // 特殊查询处理可以在这里进行
        if (name.equals("math.random")) {
            return random.nextFloat();
        }
        return getVariable(name);
    }

    public Random getRandom() {
        return random;
    }
}
