package io.lolyay.addon.utils.clip;

import io.lolyay.addon.utils.PacketUtils;
import io.lolyay.addon.utils.timer.TickTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;


public class Hclipper {
    private static final double MAX_DISTANCE_PER_PACKET = 8;
    private static final int MAX_PACKETS = 20;
    private static final double MAX_DISTANCE_PER_TICK = MAX_DISTANCE_PER_PACKET * MAX_PACKETS; // 200 blocks

    /**
     * Clip horizontally from current position by the given offset.
     *
     * @param deltaX horizontal X offset
     * @param deltaZ horizontal Z offset
     */
    public static void clip(double deltaX, double deltaZ) {
        clipWithCallback(deltaX, deltaZ, null);
    }

    /**
     * Clip horizontally from current position by the given offset with callback.
     *
     * @param deltaX   horizontal X offset
     * @param deltaZ   horizontal Z offset
     * @param onFinish callback when all chunks are sent
     */
    public static void clipWithCallback(double deltaX, double deltaZ, Runnable onFinish) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null)
            return;

        Vec3d from = mc.player.getPos();
        Vec3d to = from.add(deltaX, 0, deltaZ);
        clipFromToWithCallback(from, to, onFinish);
    }

    /**
     * Clip horizontally from a specific position to another position.
     */
    public static void clipFromTo(Vec3d from, Vec3d to) {
        clipFromToWithCallback(from, to, null);
    }

    /**
     * Clip horizontally from a specific position to another position with callback.
     */
    public static void clipFromToWithCallback(Vec3d from, Vec3d to, Runnable onFinish) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null)
            return;

        double deltaX = to.x - from.x;
        double deltaZ = to.z - from.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance <= MAX_DISTANCE_PER_TICK) {
            sendClipPackets(from, to);
            if (onFinish != null) {
                onFinish.run();
            }
        } else {
            scheduleChunkedClip(from, to, onFinish);
        }
    }

    /**
     * Schedule chunked horizontal clipping for distances > 200 blocks.
     * Sends 200 blocks per tick with delay between chunks.
     * Uses player's current position at execution time to prevent Y drift.
     */
    private static void scheduleChunkedClip(Vec3d from, Vec3d to, Runnable onFinish) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null)
            return;

        double deltaX = to.x - from.x;
        double deltaZ = to.z - from.z;
        double totalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Normalize direction
        final double dirX = deltaX / totalDistance;
        final double dirZ = deltaZ / totalDistance;

        int totalChunks = (int) Math.ceil(totalDistance / MAX_DISTANCE_PER_TICK);

        double currentY = mc.player.getY();
        Vec3d chunkFrom = new Vec3d(mc.player.getX(), currentY, mc.player.getZ());
        double chunkDistance = Math.min(MAX_DISTANCE_PER_TICK, totalDistance);
        Vec3d chunkTo = new Vec3d(
                chunkFrom.x + dirX * chunkDistance,
                currentY,
                chunkFrom.z + dirZ * chunkDistance);
        sendClipPackets(chunkFrom, chunkTo);

        final double finalTargetX = to.x;
        final double finalTargetZ = to.z;
        final int chunksRemaining = totalChunks - 1;

        scheduleNextChunk(dirX, dirZ, finalTargetX, finalTargetZ, 1, chunksRemaining, onFinish);
    }

    /**
     * Recursively schedule the next chunk using player's current position.
     */
    private static void scheduleNextChunk(double dirX, double dirZ, double targetX, double targetZ,
            int chunkIndex, int totalRemainingChunks, Runnable onFinish) {
        if (chunkIndex > totalRemainingChunks) {
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }

        TickTimer.getInstance().schedule(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null)
                return;

            double currentX = mc.player.getX();
            double currentY = mc.player.getY();
            double currentZ = mc.player.getZ();

            double remainingDeltaX = targetX - currentX;
            double remainingDeltaZ = targetZ - currentZ;
            double remainingDistance = Math.sqrt(remainingDeltaX * remainingDeltaX + remainingDeltaZ * remainingDeltaZ);

            double chunkDistance = Math.min(MAX_DISTANCE_PER_TICK, remainingDistance);

            Vec3d chunkFrom = new Vec3d(currentX, currentY, currentZ);
            Vec3d chunkTo = new Vec3d(
                    currentX + dirX * chunkDistance,
                    currentY,
                    currentZ + dirZ * chunkDistance);

            sendClipPackets(chunkFrom, chunkTo);

            boolean isLastChunk = (chunkIndex == totalRemainingChunks);
            if (isLastChunk) {
                if (onFinish != null) {
                    onFinish.run();
                }
            } else {
                scheduleNextChunk(dirX, dirZ, targetX, targetZ, chunkIndex + 1, totalRemainingChunks, onFinish);
            }
        }, 20);
    }

    /**
     * Send clip packets for a single chunk (up to MAX_DISTANCE_PER_TICK blocks).
     */
    private static void sendClipPackets(Vec3d from, Vec3d to) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null)
            return;

        double deltaX = to.x - from.x;
        double deltaZ = to.z - from.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        int packetsRequired = (int) Math.ceil(distance / MAX_DISTANCE_PER_PACKET);
        if (packetsRequired < 1) {
            packetsRequired = 1;
        }
        // Cap at MAX_PACKETS since this is a single chunk
        if (packetsRequired > MAX_PACKETS) {
            packetsRequired = MAX_PACKETS;
        }
        packetsRequired = MAX_PACKETS;

        if (mc.player.hasVehicle()) {
            // Vehicle mode
            for (int i = 0; i < (packetsRequired - 1); i++) {
                PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPosition(to.x, from.y, to.z);
            PacketUtils.sendPacketImmediate(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
        } else {
            // Player mode
            for (int i = 0; i < (packetsRequired - 1); i++) {
                PacketUtils.sendPacketImmediate(
                        new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
            }
            // Final packet with actual position
            PacketUtils.sendPacketImmediate(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            to.x, from.y, to.z, false, mc.player.horizontalCollision));
            mc.player.setPosition(to.x, from.y, to.z);
        }
    }

    /**
     * Clip horizontally from current position to target X/Z, preserving current Y.
     */
    public static void clipTo(double targetX, double targetZ) {
        clipToWithCallback(targetX, targetZ, null);
    }

    /**
     * Clip horizontally from current position to target X/Z with callback.
     */
    public static void clipToWithCallback(double targetX, double targetZ, Runnable onFinish) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null)
            return;

        Vec3d from = mc.player.getPos();
        Vec3d to = new Vec3d(targetX, from.y, targetZ);
        clipFromToWithCallback(from, to, onFinish);
    }

    /**
     * Move player horizontally with callback when done.
     *
     * @param from     starting position
     * @param to       target position (Y is taken from 'from')
     * @param onFinish callback to run after all movement packets are sent
     */
    public static void movePlayerHorizontal(Vec3d from, Vec3d to, Runnable onFinish) {
        clipFromToWithCallback(from, to, onFinish);
    }
}
