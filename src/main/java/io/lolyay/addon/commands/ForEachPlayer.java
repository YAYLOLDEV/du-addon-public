package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

import java.util.UUID;

public class ForEachPlayer extends Command {

    public ForEachPlayer() {
        super("foreachplayer", "Repeat a command for each player, with %PLAYER% and %PLAYER_UUID% and %INDEX% placeholders");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("cmd", StringArgumentType.greedyString())
                .executes((context) -> {
                   String cmd = context.getArgument("cmd", String.class);
                    MeteorExecutor.execute(() -> {
                        int i = 0;
                        for(PlayerListEntry player : mc.getNetworkHandler().getPlayerList()) {
                            if(player.getProfile() == null ||
                            (player.getLatency() == 0 && !MinecraftClient.getInstance().isInSingleplayer()) ||
                            player.getProfile().getProperties() == null ||
                            player.getProfile().getId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) continue;
                            String command = cmd
                                .replace("%INDEX%", String.valueOf(i++))
                                .replace("%PLAYER%", player.getProfile().getName())
                                .replace("%PLAYER_UUID%", player.getProfile().getName());
                            ChatUtils.sendPlayerMsg(command);
                        }
                    });
                   return SINGLE_SUCCESS;
               }));
    }
}
