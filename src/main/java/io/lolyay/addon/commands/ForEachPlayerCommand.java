package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.modules.settingsmodules.ForEachSettings;
import io.lolyay.addon.utils.timer.MsTimer;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

import java.util.UUID;

public class ForEachPlayerCommand extends Command {
    public ForEachPlayerCommand() {
        super("foreachplayer", "Repeat a command for each player, with %PLAYER% and %PLAYER_UUID% and %INDEX% placeholders");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("cmd", StringArgumentType.greedyString())
                .executes((context) -> {
                   String cmd = context.getArgument("cmd", String.class);
                   assert mc.player != null; // Should never happen, how can you run a command and not be connected?

                    ForEachSettings settings = Modules.get().get(ForEachSettings.class);
                    int delay = settings.delay.get();
                    int index = 0;
                    int offset = 0;

                    for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
                        if (player.getProfile() == null // Try to filter out NPCs
                                || (player.getLatency() == 0 && !MinecraftClient.getInstance().isInSingleplayer())
                                || player.getProfile().properties() == null
                                || player.getProfile().id().equals(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                            continue;

                        if (player.getProfile().name().equals(mc.player.getGameProfile().name())
                                && !settings.self.get())
                            continue;

                        int currentIndex = index++;

                        String command = cmd
                                .replaceAll("(?i)%index%", String.valueOf(currentIndex))
                                .replaceAll("(?i)%player%", player.getProfile().name())
                                .replaceAll("(?i)%player_uuid%", player.getProfile().id().toString());

                        MsTimer.schedule(
                            () -> ChatUtils.sendPlayerMsg(command),
                            offset
                        );

                        offset += delay;
                    }
                   return SINGLE_SUCCESS;
               }));
    }
}
