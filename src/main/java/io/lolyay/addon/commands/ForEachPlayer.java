package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.modules.settingsmodules.ForEachSettings;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
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
                   assert mc.player != null; // Should never happen, how can you run a command and not be connected?
                   assert mc.getNetworkHandler() != null;
                   MeteorExecutor.execute(() -> {
                        ForEachSettings settings = Modules.get().get(ForEachSettings.class);
                        int i = 0;
                        for(PlayerListEntry player : mc.getNetworkHandler().getPlayerList()) {
                            if(player.getProfile() == null ||
                            (player.getLatency() == 0 && !MinecraftClient.getInstance().isInSingleplayer()) ||
                            player.getProfile().properties() == null ||
                            player.getProfile().id().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) continue;
                            if(player.getProfile().name().equals(mc.player.getGameProfile().name()) && !settings.self.get())
                                continue;
                            String command = cmd
                                .replace("%INDEX%", String.valueOf(i++))
                                .replace("%index%", String.valueOf(i++))
                                .replace("%PLAYER%", player.getProfile().name())
                                .replace("%player%", player.getProfile().name())
                                .replace("%PLAYER_UUID%", player.getProfile().name())
                                .replace("%player_uuid%", player.getProfile().name());
                            ChatUtils.sendPlayerMsg(command);
                            try {
                                Thread.sleep(settings.delay.get());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                   return SINGLE_SUCCESS;
               }));
    }
}
