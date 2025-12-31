package io.lolyay.addon.modules;

import io.lolyay.addon.DupersUnitedPublicAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class PacketDelay extends Module {
    SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Queue<Packet<?>> packets = new LinkedList<>();
    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("C2S-packets")
        .description("Client-to-server packets to delay.")
        .filter((aClass) -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    public PacketDelay() {
        super(DupersUnitedPublicAddon.CATEGORY, "packet-delay", "Delays packets.");
    }


    public void onDeactivate() {
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            mc.getNetworkHandler().sendPacket(packet);
        }

    }

    @Override
    public void onActivate() {
        packets.clear();
    }

    @EventHandler(priority = 999)
    private void onPacket(PacketEvent.Send event) {
        @SuppressWarnings("unchecked")
        Class<? extends Packet<?>> clazz = (Class<? extends Packet<?>>) event.packet.getClass();
        if (c2sPackets.get().contains(clazz)) {
            packets.add(event.packet);
            event.cancel();
        }

    }
}
