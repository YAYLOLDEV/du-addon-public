package io.lolyay.addon.mixin;

import io.lolyay.addon.DupersUnitedPublicAddon;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BookUpdateC2SPacket.class)
public class BookUpdateC2SPacketMixin {

    @ModifyArg(
        method = "<clinit>", // Targets the static initializer
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/codec/PacketCodecs;string(I)Lnet/minecraft/network/codec/PacketCodec;",
            ordinal = 1 // 0 is pages (1024), 1 is title (32)
        )
    )
    private static int modifyTitleLength(int original) {
        DupersUnitedPublicAddon.LOG.info("Replaced Book packet Title len");
        return 256; // Replace 32 with 256
    }
}
