package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.commands.clickslot.ClickSlotArgumentType;
import io.lolyay.addon.commands.clickslot.SlotActionTypeI;
import io.lolyay.addon.utils.MsTimer;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.slot.SlotActionType;

public class WaitCommand extends Command {

    public WaitCommand() {
        super("wait", "Waits for X ms before running the command", "sleep", "delay");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("ms", IntegerArgumentType.integer(1))
            .then(argument("cmd", StringArgumentType.greedyString())
                .executes((context) -> {

                   int ms = context.getArgument("ms", Integer.class);
                   String cmd = context.getArgument("cmd", String.class);
                   MsTimer.schedule(() -> {
                       ChatUtils.sendPlayerMsg(cmd);
                   }, ms);
                   return SINGLE_SUCCESS;
               })));
    }
}
