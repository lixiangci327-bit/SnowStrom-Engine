package org.Lcing.snowstorm_engine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.Lcing.snowstorm_engine.definition.ParticleDefinition;
import org.Lcing.snowstorm_engine.loader.ParticleLoader;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormManager;

import java.io.InputStream;
import java.util.function.Supplier;

public class SpawnEmitterPacket {
    private final String filename;
    private final double x;
    private final double y;
    private final double z;

    public SpawnEmitterPacket(String filename, double x, double y, double z) {
        this.filename = filename;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SpawnEmitterPacket(FriendlyByteBuf buf) {
        this.filename = buf.readUtf();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(filename);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client Side Execution
            try {
                // Load Definition from resources (Client side resource access)
                String path = "/snowstorm_engine/particles/" + filename;
                InputStream is = getClass().getResourceAsStream(path);
                if (is == null) {
                    System.err.println("[Snowstorm] Could not find particle file on client: " + filename);
                    return;
                }

                ParticleDefinition def = ParticleLoader.load(is);

                // Create Emitter
                SnowstormEmitter emitter = new SnowstormEmitter(def);
                emitter.x = x;
                emitter.y = y;
                emitter.z = z;

                // Register to Manager
                SnowstormManager.getInstance().addEmitter(emitter);

            } catch (Exception e) {
                System.err.println("[Snowstorm] Error spawning particle on client: " + e.getMessage());
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
