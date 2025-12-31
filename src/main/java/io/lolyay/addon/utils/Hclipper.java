package io.lolyay.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Horizontal clipper - similar to Vclipper but for horizontal movement.
 * Sends stationary packets first, then the final movement packet, all in the
 * same tick.
 * Max 10 blocks per packet, max 20 packets = max 200 blocks per clip per tick.
 * For distances > 200 blocks, splits into 200-block chunks with 1 tick delay
 * between each.
 */
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
     * Y coordinate is preserved from 'from' position.
     */
    public static void clipFromTo(Vec3d from, Vec3d to) {
        clipFromToWithCallback(from, to, null);
    }

    /**
     * Clip horizontally from a specific position to another position with callback.
     * Y coordinate is preserved from 'from' position.
     * For distances > 200 blocks, splits into chunks with 1 tick delay between
     * each.
     */
    public static void clipFromToWithCallback(Vec3d from, Vec3d to, Runnable onFinish) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null)
            return;

        double deltaX = to.x - from.x;
        double deltaZ = to.z - from.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance <= MAX_DISTANCE_PER_TICK) {
            // Distance fits in one tick, send immediately
            sendClipPackets(from, to);
            if (onFinish != null) {
                onFinish.run();
            }
        } else {
            // Distance exceeds 200 blocks, split into chunks with 1 tick delay
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

        // Send first chunk immediately (no delay) - use current Y
        double currentY = mc.player.getY();
        Vec3d chunkFrom = new Vec3d(mc.player.getX(), currentY, mc.player.getZ());
        double chunkDistance = Math.min(MAX_DISTANCE_PER_TICK, totalDistance);
        Vec3d chunkTo = new Vec3d(
                chunkFrom.x + dirX * chunkDistance,
                currentY,
                chunkFrom.z + dirZ * chunkDistance);
        sendClipPackets(chunkFrom, chunkTo);

        // Schedule remaining chunks - each uses player's current position at execution
        // time
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

            // Get player's CURRENT position at execution time
            double currentX = mc.player.getX();
            double currentY = mc.player.getY();
            double currentZ = mc.player.getZ();

            // Calculate remaining distance to target
            double remainingDeltaX = targetX - currentX;
            double remainingDeltaZ = targetZ - currentZ;
            double remainingDistance = Math.sqrt(remainingDeltaX * remainingDeltaX + remainingDeltaZ * remainingDeltaZ);

            // Calculate this chunk's movement (up to MAX_DISTANCE_PER_TICK)
            double chunkDistance = Math.min(MAX_DISTANCE_PER_TICK, remainingDistance);

            // Use the original direction, not recalculated (to maintain straight line)
            Vec3d chunkFrom = new Vec3d(currentX, currentY, currentZ);
            Vec3d chunkTo = new Vec3d(
                    currentX + dirX * chunkDistance,
                    currentY, // Use CURRENT Y, not pre-calculated
                    currentZ + dirZ * chunkDistance);

            sendClipPackets(chunkFrom, chunkTo);

            // Schedule next chunk or call callback
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
     * Sends stationary packets first, then the final movement packet.
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
            // Player mode - send stationary packets first, then the movement packet
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
     * For distances <= 200 blocks, sends all packets immediately in same tick.
     * For distances > 200 blocks, splits into 200-block chunks with 1 tick delay.
     *
     * @param from     starting position
     * @param to       target position (Y is taken from 'from')
     * @param onFinish callback to run after all movement packets are sent
     */
    public static void movePlayerHorizontal(Vec3d from, Vec3d to, Runnable onFinish) {
        clipFromToWithCallback(from, to, onFinish);
    }
}
