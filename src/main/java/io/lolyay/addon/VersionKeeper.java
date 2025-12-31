package io.lolyay.addon;

import lombok.Getter;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.registry.VersionedIdentifier;

import java.util.ArrayList;
import java.util.Objects;

@Getter
public class VersionKeeper {
    public static String version = "Couldn't find version";
    public static ArrayList<String> resourcePacks = new ArrayList<>();

    public static void gotPacket(SelectKnownPacksS2CPacket packet) {
        for (VersionedIdentifier knownPack : packet.knownPacks()) {
            if (knownPack.isVanilla() && Objects.equals(knownPack.id(), "core")) {
                version = knownPack.version();
            }
            resourcePacks.add(knownPack.namespace() + ":" + knownPack.id() + " (v" + knownPack.version() + ")");
        }
    }
}
