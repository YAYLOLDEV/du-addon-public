package io.lolyay.addon.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Vclipper {

    public static int clip(double relativeTargetHeight) {
        MinecraftClient mc = MinecraftClient.getInstance();

        int packetsRequired = (int) Math.ceil(Math.abs(relativeTargetHeight / 10));

        if (packetsRequired > 20) {
            packetsRequired = 1;
        }

        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(),
                    mc.player.getVehicle().getY() + relativeTargetHeight, mc.player.getVehicle().getZ());
            PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                PacketUtils
                        .sendPacketImmediate(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
            }
            PacketUtils.sendPacketImmediate(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(),
                    mc.player.getY() + relativeTargetHeight, mc.player.getZ(), false, mc.player.horizontalCollision));
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + relativeTargetHeight, mc.player.getZ());
        }
        return SINGLE_SUCCESS;
    }

    public static int clipFrom(Vec3d from, double relativeTargetHeight) {

        MinecraftClient mc = MinecraftClient.getInstance();
        int packetsRequired = (int) Math.ceil(Math.abs(relativeTargetHeight / 10));

        if (packetsRequired > 20) {
            packetsRequired = 1;
        }

        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(),
                    mc.player.getVehicle().getY() + relativeTargetHeight, mc.player.getVehicle().getZ());
            PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                PacketUtils
                        .sendPacketImmediate(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
            }
            PacketUtils.sendPacketImmediate(new PlayerMoveC2SPacket.PositionAndOnGround(from.getX(),
                    from.getY() + relativeTargetHeight, from.getZ(), false, mc.player.horizontalCollision));
            mc.player.setPosition(from.getX(), from.getY() + relativeTargetHeight, from.getZ());
        }
        return SINGLE_SUCCESS;
    }

    public static int clipFromTo(Vec3d from, Vec3d to) {

        MinecraftClient mc = MinecraftClient.getInstance();

        double relativeTargetHeight = to.y - from.y;
        int packetsRequired = (int) Math.ceil(Math.abs(relativeTargetHeight / 10));

        if (packetsRequired > 20) {
            packetsRequired = 1;
        }

        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(),
                    mc.player.getVehicle().getY() + relativeTargetHeight, mc.player.getVehicle().getZ());
            PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                PacketUtils
                        .sendPacketImmediate(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
            }
            PacketUtils.sendPacketImmediate(new PlayerMoveC2SPacket.PositionAndOnGround(from.getX(),
                    from.getY() + relativeTargetHeight, from.getZ(), false, mc.player.horizontalCollision));
            mc.player.setPosition(from.getX(), from.getY() + relativeTargetHeight, from.getZ());
        }
        return SINGLE_SUCCESS;
    }

    public static boolean isBlockPassable(BlockPos pos) {
        BlockState blockState = MinecraftClient.getInstance().world.getBlockState(pos);
        return blockState.getCollisionShape(MinecraftClient.getInstance().world, pos).isEmpty();
    }

    public static BlockPos findVertical2BlockSpace(Vec3d location, int maxLow, int maxHeight, boolean searchDown) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world == null)
            return null;
        BlockPos startPos = BlockPos.ofFloored(location);

        if (searchDown) {
            for (int dy = -1; dy >= -maxLow; dy--) {
                BlockPos lower = startPos.add(0, dy, 0);
                BlockPos upper = lower.up(); // next block above lower
                if (isBlockPassable(lower) && isBlockPassable(upper)) {
                    System.out.println("Found 2-block space below at: " + lower);
                    return lower;
                }
            }
        } else {
            for (int dy = 1; dy <= maxHeight; dy++) {
                BlockPos lower = startPos.add(0, dy, 0);
                BlockPos upper = lower.up(); // next block above lower
                if (isBlockPassable(lower) && isBlockPassable(upper)) {
                    System.out.println("Found 2-block space above at: " + lower);
                    return lower;
                }
            }
        }

        return null;
    }
}
