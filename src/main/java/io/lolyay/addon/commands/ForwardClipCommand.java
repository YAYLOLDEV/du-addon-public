package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.utils.clip.Hclipper;
import io.lolyay.addon.utils.timer.TickTimer;
import io.lolyay.addon.utils.clip.Vclipper;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AntiVoid;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Vec3d;

//25% chance to end up at bedrock (:
public class ForwardClipCommand extends Command {
    private static final double MAX_VCLIP_DISTANCE = 200;
    private static final double BELOW_BEDROCK = -66;
    private static final double ABOVE_BUILD_LIMIT = 320;

    public ForwardClipCommand() {
        super("fclip", "Forward Clip Though Blocks. 25% Chance to end up at bedrock!", "forwardclip");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            boolean wasAVOn = Modules.get().isActive(AntiVoid.class);
            if (!wasAVOn)
                Modules.get().get(AntiVoid.class).toggle();
            double blocks = context.getArgument("blocks", Double.class);
            Vec3d forward = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
            double startX = mc.player.getX();
            double startY = mc.player.getY();
            double startZ = mc.player.getZ();
            double targetX = startX + forward.x * blocks;
            double targetY = startY;
            double targetZ = startZ + forward.z * blocks;

            double originalGravity = mc.player.getAttributes().getValue(EntityAttributes.GRAVITY);
            mc.player.getAttributes().setBaseFrom(new AttributeContainer(
                    new DefaultAttributeContainer.Builder().add(EntityAttributes.GRAVITY, 0).build()));

            double currentY = mc.player.getY();
            double distanceToBelowBedrock = Math.abs(currentY - BELOW_BEDROCK);
            double distanceToAboveBuildLimit = Math.abs(ABOVE_BUILD_LIMIT - currentY);

            double clipHeight;
            if (distanceToBelowBedrock <= distanceToAboveBuildLimit && distanceToBelowBedrock <= MAX_VCLIP_DISTANCE) {
                clipHeight = BELOW_BEDROCK;
            } else if (distanceToAboveBuildLimit <= MAX_VCLIP_DISTANCE) {
                clipHeight = ABOVE_BUILD_LIMIT;
            } else if (distanceToBelowBedrock <= MAX_VCLIP_DISTANCE) {
                clipHeight = BELOW_BEDROCK;
            } else {
                ChatUtils.error("Cannot hclip: both destinations exceed 200 block limit.");
                mc.player.getAttributes().setBaseFrom(new AttributeContainer(
                        new DefaultAttributeContainer.Builder().add(EntityAttributes.GRAVITY, originalGravity)
                                .build()));
                return SINGLE_SUCCESS;
            }

            final double finalClipHeight = clipHeight;
            TickTimer timer = TickTimer.getInstance();

            timer.schedule(() -> {
                double relativeUp = finalClipHeight - mc.player.getY();
                Vclipper.clip(relativeUp);
                ChatUtils.info("Vclipped to height " + finalClipHeight);
            }, 2);

            timer.schedule(() -> {
                Vec3d pos = new Vec3d(mc.player.getX(), finalClipHeight, mc.player.getZ());
                Vec3d xPos = new Vec3d(targetX, finalClipHeight, mc.player.getZ());
                Hclipper.clipFromTo(pos, xPos);
            }, 7);

            timer.schedule(() -> {
                Vec3d pos = new Vec3d(mc.player.getX(), finalClipHeight, mc.player.getZ());
                Vec3d zPos = new Vec3d(targetX, finalClipHeight, targetZ);
                Hclipper.clipFromTo(pos, zPos);
            }, 7);

            timer.schedule(() -> {
                Vec3d currentPos = new Vec3d(targetX, finalClipHeight, targetZ);
                double relativeDown = targetY - finalClipHeight;

                Vclipper.clipFrom(currentPos, relativeDown);

                mc.player.setPosition(targetX, targetY + 0.1, targetZ);
                if (!wasAVOn)
                    Modules.get().get(AntiVoid.class).toggle();
                ChatUtils.info("Successfully hclipped to " + String.format("%.1f", targetX) + " "
                        + String.format("%.1f", targetY) + " " + String.format("%.1f", targetZ));

                timer.schedule(() -> {
                    double playerY = mc.player.getY();
                    boolean stuckAtSafeHeight = Math.abs(playerY - BELOW_BEDROCK) < 5
                            || Math.abs(playerY - ABOVE_BUILD_LIMIT) < 5;

                    if (stuckAtSafeHeight) {
                        ChatUtils.warning("Stuck at safe height, returning to start position...");

                        timer.schedule(() -> {
                            Vec3d pos = new Vec3d(mc.player.getX(), playerY, mc.player.getZ());
                            Vec3d xPos = new Vec3d(startX, playerY, mc.player.getZ());
                            Hclipper.clipFromTo(pos, xPos);
                        }, 2);

                        timer.schedule(() -> {
                            Vec3d pos = new Vec3d(mc.player.getX(), playerY, mc.player.getZ());
                            Vec3d zPos = new Vec3d(startX, playerY, startZ);
                            Hclipper.clipFromTo(pos, zPos);
                        }, 2);

                        timer.schedule(() -> {
                            double relativeBack = startY - playerY;
                            Vclipper.clipFrom(new Vec3d(startX, playerY, startZ), relativeBack);
                            mc.player.setPosition(startX, startY + 0.1, startZ);

                            mc.player.getAttributes().setBaseFrom(new AttributeContainer(
                                    new DefaultAttributeContainer.Builder()
                                            .add(EntityAttributes.GRAVITY, originalGravity)
                                            .build()));
                            if (!wasAVOn)
                                Modules.get().get(AntiVoid.class).toggle();
                            ChatUtils.info("Returned to start position.");
                        }, 2);
                    } else {
                        mc.player.getAttributes().setBaseFrom(new AttributeContainer(
                                new DefaultAttributeContainer.Builder()
                                        .add(EntityAttributes.GRAVITY, originalGravity)
                                        .build()));
                    }
                }, 10);
            }, 2);

            return SINGLE_SUCCESS;
        }));
    }
}
