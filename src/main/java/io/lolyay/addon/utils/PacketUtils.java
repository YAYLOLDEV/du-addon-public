package io.lolyay.addon.utils;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.EVENT_BUS;

public class PacketUtils {
    private static final int MAX_PACKETS_PER_TICK = 20;
    private static final Queue<QueueEntry> packetQueue = new LinkedList<>();

    private record QueueEntry(Packet<?> packet, Runnable onSent) {
    }

    @PostInit
    public static void init() {
        EVENT_BUS.subscribe(PacketUtils.class);
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        processPacketQueue();
    }

    private static void processPacketQueue() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.networkHandler == null)
            return;

        int sent = 0;
        while (!packetQueue.isEmpty() && sent < MAX_PACKETS_PER_TICK) {
            QueueEntry entry = packetQueue.poll();
            if (entry != null && entry.packet != null) {
                player.networkHandler.sendPacket(entry.packet);
                sent++;
                // Run callback if this was the last packet with a callback
                if (entry.onSent != null) {
                    entry.onSent.run();
                }
            }
        }
    }

    /**
     * Queue a packet to be sent. Max 20 packets are sent per tick.
     */
    public static void queuePacket(Packet<?> packet) {
        if (packet != null) {
            packetQueue.add(new QueueEntry(packet, null));
        }
    }

    /**
     * Queue a packet with a callback that runs when this specific packet is sent.
     */
    public static void queuePacket(Packet<?> packet, Runnable onSent) {
        if (packet != null) {
            packetQueue.add(new QueueEntry(packet, onSent));
        }
    }

    /**
     * Queue multiple packets, with a callback that runs after the last one is sent.
     */
    public static void queuePackets(List<Packet<?>> packets, Runnable onAllSent) {
        if (packets == null || packets.isEmpty()) {
            if (onAllSent != null)
                onAllSent.run();
            return;
        }
        for (int i = 0; i < packets.size(); i++) {
            Packet<?> packet = packets.get(i);
            if (i == packets.size() - 1) {
                packetQueue.add(new QueueEntry(packet, onAllSent));
            } else {
                packetQueue.add(new QueueEntry(packet, null));
            }
        }
    }

    /**
     * Send a packet immediately, bypassing the queue.
     */
    public static void sendPacketImmediate(Packet<?> packet) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.networkHandler != null && packet != null) {
            player.networkHandler.sendPacket(packet);
        }
    }

    public static int getQueueSize() {
        return packetQueue.size();
    }

    public static void clearQueue() {
        packetQueue.clear();
    }

    private static void sendPacket(Packet<?> packet) {
        queuePacket(packet);
    }

    public static void sendFakePositionPacket() {
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                1, // definitely in a block
                2, // we dont want to fly
                3,
                true, // will fck anticheats
                false);
        sendPacket(packet);
    }

    public static void sendPositionPacket(Vec3d position, boolean onground) {
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                position.x,
                position.y, // we dont want to fly
                position.z,
                onground,
                false);
        sendPacket(packet);
    }

    public static void sendPositionRepeatedPackets(Vec3d position, int times) {
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                position.x,
                position.y, // we dont want to fly
                position.z,
                false,
                false);
        for (int i = 0; i == times; i++) {
            sendPacket(packet);
            System.out.println("sent packet");
        }
    }

    public static void sendEntityHitPacket(Entity entity) {
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(
                entity,
                false);
        sendPacket(packet);
    }

    public static void sendJumpTickPackets(double height, Vec3d from) {
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                from.x,
                from.y + height,
                from.z,
                false,
                false);
        sendPacket(packet);

        packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                from.x,
                from.y,
                from.z,
                true,
                false);
        sendPacket(packet);
    }

    public static void sendBowReleasePacket() {
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                new BlockPos(0, 0, 0),
                Direction.NORTH,
                0);
        sendPacket(packet);
    }

    public static void sendStartSprintingPacket() {
        PlayerInputC2SPacket packet = new PlayerInputC2SPacket(
                new PlayerInput(false, false, false, false, false, false, true));
        sendPacket(packet);
    }

    public static void sendStartSneakingPacket() {
        PlayerInputC2SPacket packet = new PlayerInputC2SPacket(
                new PlayerInput(false, false, false, false, false, true, false));
        sendPacket(packet);
    }

    public static void sendStopSneakingAndSprintingPacket() {
        PlayerInputC2SPacket packet = new PlayerInputC2SPacket(
                new PlayerInput(false, false, false, false, false, false, false));
        sendPacket(packet);
    }

    public static void sendTeleportConfirmPacket(int tpid) {
        TeleportConfirmC2SPacket packet = new TeleportConfirmC2SPacket(tpid);
        sendPacket(packet);
    }

    public static void sendElytraFlyPacket() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ClientCommandC2SPacket packet = new ClientCommandC2SPacket(player,
                    ClientCommandC2SPacket.Mode.START_FALL_FLYING);
            sendPacket(packet);
        }
    }

    public static void sendGroundPaket(boolean onground) {
        PlayerMoveC2SPacket.OnGroundOnly packet = new PlayerMoveC2SPacket.OnGroundOnly(
                onground, false);
        sendPacket(packet);
    }

    public static void sendBundleSelectPacket(int bundleSlotInInv, int slotInBundle) {
        BundleItemSelectedC2SPacket packet = new BundleItemSelectedC2SPacket(
                bundleSlotInInv,
                slotInBundle);
        sendPacket(packet);
    }

    public static void sendHandInteractPacket(Hand hand) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            PlayerInteractItemC2SPacket packet = new PlayerInteractItemC2SPacket(
                    hand,
                    69420,
                    player.headYaw,
                    player.getPitch());
            sendPacket(packet);
        }
    }

    public static void sendDropCurrentItemPacket() {
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.DROP_ITEM,
                new BlockPos(0, 0, 0),
                Direction.DOWN);
        sendPacket(packet);
    }

    public static void sendEditBookPacket(int slotid, List<String> text, String title) {
        BookUpdateC2SPacket packet = new BookUpdateC2SPacket(
                slotid,
                text,
                Optional.of(title));
        sendPacket(packet);
    }

    public static void sendSelectHotbarSlotPacket(int slot) {
        UpdateSelectedSlotC2SPacket packet = new UpdateSelectedSlotC2SPacket(slot);
        sendPacket(packet);
    }

    public static void sendCommandPacket(String command) {
        CommandExecutionC2SPacket packet = new CommandExecutionC2SPacket(command);
        sendPacket(packet);
    }
}
