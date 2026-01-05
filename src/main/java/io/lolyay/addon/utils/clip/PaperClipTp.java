package io.lolyay.addon.utils.clip;

import io.lolyay.addon.utils.timer.TickTimer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AntiVoid;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PaperClipTp {
    private static final double MAX_VCLIP_DISTANCE = 200;
    private static final double BELOW_BEDROCK = -66;
    private static final double ABOVE_BUILD_LIMIT = 320;

    public static void tp(double targetX, double targetY, double targetZ) {
        boolean wasAVOn = Modules.get().isActive(AntiVoid.class);
        if (!wasAVOn)
            Modules.get().get(AntiVoid.class).toggle();


        double currentY = mc.player.getY();
        double distanceToBedrock = Math.abs(currentY - BELOW_BEDROCK);
        double distanceToBuildLimit = Math.abs(ABOVE_BUILD_LIMIT - currentY);

        double clipHeight;
        if (distanceToBedrock <= distanceToBuildLimit
            && distanceToBedrock <= MAX_VCLIP_DISTANCE) {
            clipHeight = BELOW_BEDROCK;
        } else if (distanceToBuildLimit <= MAX_VCLIP_DISTANCE) {
            clipHeight = ABOVE_BUILD_LIMIT;
        } else { //TODO Still allow clipping if above build limit
            ChatUtils.error("Cannot perform clip: This can only happen if you're too high, or too low.");
            return;
        }

        final double finalClipHeight = clipHeight;
        TickTimer timer = TickTimer.getInstance();


        timer.schedule(() -> {
            double relativeUp = finalClipHeight - mc.player.getY();
            Vclipper.clip(relativeUp);
        }, 3);


        timer.schedule(() -> {
            Vec3d currentPlayerPos = new Vec3d(mc.player.getX(), finalClipHeight,
                mc.player.getZ());

            Vec3d xPos = new Vec3d(targetX, finalClipHeight, mc.player.getZ());

            Hclipper.clipFromToWithCallback(currentPlayerPos, xPos, () -> {
                timer.schedule(() -> {
                    Vec3d zPos = new Vec3d(targetX, finalClipHeight, targetZ);

                    Vec3d currentPos = new Vec3d(mc.player.getX(), finalClipHeight,
                        mc.player.getZ());


                    Hclipper.clipFromToWithCallback(currentPos, zPos, () -> {
                        timer.schedule(() -> {
                            double relativeDown = targetY - finalClipHeight;
                            Vclipper.clipFrom(
                                new Vec3d(targetX, finalClipHeight, targetZ),
                                relativeDown);

                            mc.player.setPosition(targetX, targetY + 0.1, targetZ);

                            if (!wasAVOn)
                                Modules.get().get(AntiVoid.class).toggle();

                            ChatUtils.info("Successfully teleported to " +
                                String.format("%.1f", targetX) + " " +
                                String.format("%.1f", targetY) + " " +
                                String.format("%.1f", targetZ));
                        }, 7);
                    });
                }, 7);
            });
        }, 7);
    }
}
