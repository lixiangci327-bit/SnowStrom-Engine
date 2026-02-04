package org.Lcing.snowstorm_engine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import org.Lcing.snowstorm_engine.definition.ParticleDefinition;
import org.Lcing.snowstorm_engine.loader.ParticleLoader;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormManager;

import org.Lcing.snowstorm_engine.network.SpawnEmitterPacket;

import java.io.InputStream;

public class SnowstormCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("snowstorm")
                .then(Commands.literal("spawn")
                        // /snowstorm spawn (uses default)
                        .executes(ctx -> spawnEmitter(ctx, "test1-re.particle.json"))
                        // /snowstorm spawn <filename>
                        .then(Commands.argument("file", StringArgumentType.string())
                                .executes(ctx -> spawnEmitter(ctx, StringArgumentType.getString(ctx, "file")))))
                .then(Commands.literal("clear")
                        .executes(SnowstormCommand::clearEmitters)));
    }

    private static int spawnEmitter(CommandContext<CommandSourceStack> context, String filename) {
        try {
            CommandSourceStack source = context.getSource();

            // Load Definition from resources
            String path = "/snowstorm_engine/particles/" + filename;
            InputStream is = SnowstormCommand.class.getResourceAsStream(path);
            if (is == null) {
                source.sendFailure(new TextComponent("Could not find particle file: " + filename));
                source.sendFailure(new TextComponent("Expected path: " + path));
                return 0;
            }

            // Send Packet to all clients (Simulate on Client)
            net.minecraft.world.phys.Vec3 pos = source.getEntityOrException().position();
            double x = pos.x;
            double y = pos.y + 1.5;
            double z = pos.z;

            SpawnEmitterPacket packet = new SpawnEmitterPacket(filename, x, y, z);
            org.Lcing.snowstorm_engine.network.PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                    packet);

            source.sendSuccess(new TextComponent(
                    "[Snowstorm] Broadcasted spawn packet for '" + filename + "'"),
                    true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(new TextComponent("Error sending particle packet: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int clearEmitters(CommandContext<CommandSourceStack> context) {
        SnowstormManager.getInstance().clear();
        context.getSource().sendSuccess(new TextComponent("[Snowstorm] Cleared all emitters"), true);
        return 1;
    }
}
