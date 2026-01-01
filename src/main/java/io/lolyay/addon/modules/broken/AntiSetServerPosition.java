package io.lolyay.addon.modules.broken;

import io.lolyay.addon.DupersUnitedPublicAddon;
import io.lolyay.addon.utils.PacketUtils;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.lolyay.addon.modules.broken.SuperReach.*;

public class AntiSetServerPosition extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyOnSuperReach = sgGeneral.add(
        new BoolSetting.Builder()
            .name("only-on-super-reach")
            .description("Only intercept server teleports triggered by Super Reach.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> logTeleports = sgGeneral.add(
        new BoolSetting.Builder()
            .name("log")
            .description("Log intercepted server teleports.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> turnoffOnDisconnect = sgGeneral.add(
        new BoolSetting.Builder()
            .name("disable-on-leave")
            .description("Turn off AntiSetServerPosition when you disconnects.")
            .defaultValue(true)
            .build()
    );

    public static boolean waitingForDesyncPacket = false;
    private ScheduledFuture<?> desyncResetTask;



    public AntiSetServerPosition() {
        super(
            DupersUnitedPublicAddon.CATEGORY,
            "anti-server-tp",
            "Intercepts server position corrections (e.g. .tp, .forwardclip, Super Reach).\n" +
                "WARNING: Extremely detectable. Expect kick-loops or bans."
        );
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (turnoffOnDisconnect.get()) toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof PlayerPositionLookS2CPacket packet)) return;

        if (onlyOnSuperReach.get() && !waitingForDesyncPacket) return;

        event.cancel();

        if (logTeleports.get()) {
            ChatUtils.info("ServerTP -> " + packet.change().position());
        }

        // Server requires this or movement is frozen
        PacketUtils.sendTeleportConfirmPacket(packet.teleportId());

        if (packet.change().position() == null) return;

        cancelDesyncReset();

        SuperReach.INSTANCE.movePlayerSmoothly(
            packet.change().position(),
            onlyOnSuperReach.get() ? currentTargetPos : mc.player.getPos(),
            () -> {
                waitingForDesyncPacket = true;

                if (logTeleports.get()) {
                    ChatUtils.info("Bypassed ServerTP (ID=" + packet.teleportId() + ")");
                }
            }
        );

        waitingForDesyncPacket = true;
        desyncResetTask = EXECUTOR.schedule(
            () -> waitingForDesyncPacket = false,
            publicDesyncWaitTime,
            TimeUnit.MILLISECONDS
        );
    }

    private void cancelDesyncReset() {
        if (desyncResetTask != null) {
            desyncResetTask.cancel(true);
            desyncResetTask = null;
        }
    }
}
