package org.Lcing.snowstorm_engine.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import org.Lcing.snowstorm_engine.runtime.SnowstormEmitter;
import org.Lcing.snowstorm_engine.runtime.SnowstormManager;

import org.Lcing.snowstorm_engine.network.SpawnEmitterPacket;

public class SnowstormCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("snowstorm")
                .then(Commands.literal("spawn")
                        // /snowstorm spawn (默认配置)
                        .executes(ctx -> spawnEmitter(ctx, "test1-re.particle.json"))
                        // /snowstorm spawn <文件名>
                        .then(Commands.argument("file", StringArgumentType.string())
                                .executes(ctx -> spawnEmitter(ctx, StringArgumentType.getString(ctx, "file")))))
                .then(Commands.literal("clear")
                        .executes(SnowstormCommand::clearEmitters)));
    }

    private static int spawnEmitter(CommandContext<CommandSourceStack> context, String identifier) {
        try {
            CommandSourceStack source = context.getSource();

            // 检查粒子是否存在于注册表中
            if (SnowstormManager.getInstance().getParticleDefinition(identifier) == null) {
                source.sendFailure(new TextComponent("Could not find particle with identifier: " + identifier));
                return 0;
            }

            // 发送数据包给所有客户端
            net.minecraft.world.phys.Vec3 pos = source.getEntityOrException().position();
            double x = pos.x;
            double y = pos.y + 1.5;
            double z = pos.z;

            SpawnEmitterPacket packet = new SpawnEmitterPacket(identifier, x, y, z);
            org.Lcing.snowstorm_engine.network.PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                    packet);

            source.sendSuccess(new TextComponent(
                    "[Snowstorm] Broadcasted spawn packet for '" + identifier + "'"),
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
