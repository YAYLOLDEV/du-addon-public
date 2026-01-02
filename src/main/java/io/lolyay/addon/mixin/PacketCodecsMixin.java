package io.lolyay.addon.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encoding.StringEncoding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketCodecs.class)
public interface PacketCodecsMixin {
    @Inject(method = "string(I)Lnet/minecraft/network/codec/PacketCodec;", at = @At("HEAD"), cancellable = true)
    private static void onString(int maxLength, CallbackInfoReturnable<PacketCodec<ByteBuf, String>> cir) {
        if (maxLength == 32) {
            cir.setReturnValue(new PacketCodec<ByteBuf, String>() {
                @Override
                public String decode(ByteBuf byteBuf) {
                    return StringEncoding.decode(byteBuf, 32767);
                }

                @Override
                public void encode(ByteBuf byteBuf, String string) {
                    StringEncoding.encode(byteBuf, string, 32767);
                }
            });
        }
    }
}
