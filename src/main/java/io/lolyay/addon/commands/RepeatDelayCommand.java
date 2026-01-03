package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.utils.timer.MsTimer;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RepeatDelayCommand extends Command {
    public RepeatDelayCommand() {
        super("repeat-delay", "Repeats a Command X Times with a delay between each command");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("ms", IntegerArgumentType.integer(1))
               .then(argument("times", IntegerArgumentType.integer(1))
               .then(argument("cmd", StringArgumentType.greedyString())
               .executes((context) -> {
                   int ms = context.getArgument("ms", Integer.class);
                   int times = context.getArgument("times", Integer.class);
                   String cmd = context.getArgument("cmd", String.class);

                   for (int i = 0; i < times; i++) {
                       int delay = i * ms;
                       MsTimer.schedule(
                           () -> ChatUtils.sendPlayerMsg(cmd),
                           delay
                       );
                   }
                   return SINGLE_SUCCESS;
               }))));
    }
}
