package org.Lcing.snowstorm_engine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.Lcing.snowstorm_engine.definition.ParticleDefinition;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormManager;

import java.util.function.Supplier;

public class SpawnEmitterPacket {
    private final String identifier;
    private final double x;
    private final double y;
    private final double z;

    public SpawnEmitterPacket(String identifier, double x, double y, double z) {
        this.identifier = identifier;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SpawnEmitterPacket(FriendlyByteBuf buf) {
        this.identifier = buf.readUtf();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 客户端执行
            try {
                // 从注册表中查找粒子
                ParticleDefinition def = SnowstormManager.getInstance().getParticleDefinition(identifier);

                if (def == null) {
                    System.err.println("[Snowstorm] Could not find particle definition for identifier: " + identifier);
                    return;
                }

                // 创建发射器
                SnowstormEmitter emitter = new SnowstormEmitter(def);
                emitter.x = x;
                emitter.y = y;
                emitter.z = z;

                // 注册到管理器
                SnowstormManager.getInstance().addEmitter(emitter);

            } catch (Exception e) {
                System.err.println("[Snowstorm] Error spawning particle on client: " + e.getMessage());
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
