package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.utils.MsTimer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

public class RepeatCommand extends Command {

    public RepeatCommand() {
        super("repeat", "Repeats a Command X Times");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("times", IntegerArgumentType.integer(1))
            .then(argument("cmd", StringArgumentType.greedyString())
                .executes((context) -> {

                   int times = context.getArgument("times", Integer.class);
                   String cmd = context.getArgument("cmd", String.class);
                    MeteorExecutor.execute(() -> {
                        for(int i = 0; i < times; i++) {
                            ChatUtils.sendPlayerMsg(cmd);
                        }
                    });
                   return SINGLE_SUCCESS;
               })));
    }
}
