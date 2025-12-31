package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.utils.clip.Hclipper;
import io.lolyay.addon.utils.timer.TickTimer;
import io.lolyay.addon.utils.clip.Vclipper;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AntiVoid;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Vec3d;

public class TpCommand extends Command {
    private static final double MAX_VCLIP_DISTANCE = 200;
    private static final double BELOW_BEDROCK = -66;
    private static final double ABOVE_BUILD_LIMIT = 320;

    public TpCommand() {
        super("tp", "Teleport. 25% Chance to end up at bedrock!", "teleport");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("x", DoubleArgumentType.doubleArg())
                        .then(argument("y", DoubleArgumentType.doubleArg())
                                .then(argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            boolean wasAVOn = Modules.get().isActive(AntiVoid.class);
                                            if (!wasAVOn)
                                                Modules.get().get(AntiVoid.class).toggle();
                                            double targetX = context.getArgument("x", Double.class);
                                            double targetY = context.getArgument("y", Double.class);
                                            double targetZ = context.getArgument("z", Double.class);


                                            double originalGravity = mc.player.getAttributes()
                                                    .getValue(EntityAttributes.GRAVITY);
                                            mc.player.getAttributes().setBaseFrom(new AttributeContainer(
                                                    new DefaultAttributeContainer.Builder()
                                                            .add(EntityAttributes.GRAVITY, 0).build()));


                                            double currentY = mc.player.getY();
                                            double distanceToBedrock = Math.abs(currentY - BELOW_BEDROCK);
                                            double distanceToBuildLimit = Math.abs(ABOVE_BUILD_LIMIT - currentY);

                                            double clipHeight;
                                            if (distanceToBedrock <= distanceToBuildLimit
                                                    && distanceToBedrock <= MAX_VCLIP_DISTANCE) {
                                                clipHeight = BELOW_BEDROCK;
                                            } else if (distanceToBuildLimit <= MAX_VCLIP_DISTANCE) {
                                                clipHeight = ABOVE_BUILD_LIMIT;
                                            } else if (distanceToBedrock <= MAX_VCLIP_DISTANCE) {
                                                clipHeight = BELOW_BEDROCK;
                                            } else {
                                                ChatUtils.error("Cannot teleport: target too far vertically.");
                                                mc.player.getAttributes().setBaseFrom(new AttributeContainer(
                                                        new DefaultAttributeContainer.Builder()
                                                                .add(EntityAttributes.GRAVITY, originalGravity)
                                                                .build()));
                                                return SINGLE_SUCCESS;
                                            }

                                            final double finalClipHeight = clipHeight;
                                            TickTimer timer = TickTimer.getInstance();


                                            timer.schedule(() -> {
                                                double relativeUp = finalClipHeight - mc.player.getY();
                                                Vclipper.clip(relativeUp);
                                                ChatUtils.info("Vclipped to height " + finalClipHeight);
                                            }, 3);



                                            timer.schedule(() -> {
                                                Vec3d pos = new Vec3d(mc.player.getX(), finalClipHeight,
                                                        mc.player.getZ());
                                                Vec3d xPos = new Vec3d(targetX, finalClipHeight, mc.player.getZ());


                                                Hclipper.clipFromToWithCallback(pos, xPos, () -> {
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


                                                                mc.player.getAttributes()
                                                                        .setBaseFrom(new AttributeContainer(
                                                                                new DefaultAttributeContainer.Builder()
                                                                                        .add(EntityAttributes.GRAVITY,
                                                                                                originalGravity)
                                                                                        .build()));
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

                                            return SINGLE_SUCCESS;
                                        }))));
    }

}
