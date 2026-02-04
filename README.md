# Snowstorm Engine

Snowstorm Engine 是一个为 Minecraft Forge (1.18.2) 设计的粒子引擎，旨在兼容基岩版 (Bedrock Edition) 的粒子格式 (`.particle.json`)。它允许开发者在 Java 版中使用类似基岩版的 JSON 数据驱动的粒子系统，并支持 Molang 表达式。

## 📋 前置要求 (Dependencies)

在运行本模组前，请确保安装以下前置：

- **Minecraft**: 1.18.2
- **Forge**: 推荐最新版本
- **GeckoLib**: `geckolib-forge-1.18` (版本 3.0.57 或更高)

## 📂 资源文件路径 (File Structure)

为了让引擎正确加载粒子文件，请将 `.particle.json` 文件放置在资源包的以下路径中：

```text
src/main/resources/assets/modid/snowstorm_engine/particles/
```

其中 `modid` 是你的模组 ID (namespace)。
例如，如果你的模组 ID 是 `snowstorm_engine`，那么路径应该是：
`assets/snowstorm_engine/snowstorm_engine/particles/example.particle.json`

或者如果你的模组 ID 是 `example_mod`，路径应该是：
`assets/example_mod/snowstorm_engine/particles/example.particle.json`

粒子纹理通常位于：
`assets/<modid>/textures/particle/`。

## 🎮 使用方法 (Usage)

### 命令 (Commands)

模组提供了一个调试命令用于在游戏中生成粒子：

- **生成粒子**:
  ```mcfunction
  /snowstorm spawn <particle_identifier>
  ```
  例如：如果你的粒子 ID 为 `minecraft:test_particle`，则输入：
  `/snowstorm spawn minecraft:test_particle` (如果文件名匹配 ID，也可以尝试文件名，建议使用 ID)

- **清除所有粒子**:
  ```mcfunction
  /snowstorm clear
  ```

### 主要功能 (Features)

- **基岩版格式支持**: 支持解析标准的 `.particle.json` 文件结构。
- **Molang 支持**: 内置 Molang 解析器，支持变量（如 `variable.particle_age`）和数学表达式。
- **组件系统**:
  - `minecraft:emitter_rate_steady` (稳定发射率)
  - `minecraft:emitter_lifetime_looping` (循环生命周期)
  - `minecraft:emitter_shape_point` / `box` / `sphere` (发射形状)
  - `minecraft:particle_appearance_billboard` (公告板渲染)
  - `minecraft:particle_motion_dynamic` (动态运动)
  - 等等...

## 🛠️ 开发与构建 (Development)

本项目使用 Gradle 构建。在根目录下运行以下命令进行构建：

```bash
.\gradlew.bat build
```

构建产物将位于 `build/libs/` 目录下。
