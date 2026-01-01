package io.lolyay.addon.mixin;

import io.lolyay.addon.asm.BookPacketTransformer;
import meteordevelopment.meteorclient.asm.Asm;
import meteordevelopment.meteorclient.asm.AsmTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Asm.class)
public class AsmMixin {
    @Shadow(remap = false)
    private void add(AsmTransformer transformer) {
        throw new AssertionError();
    }

    @Inject(remap = false, at = @At("TAIL"), method = "init")
    private static void init(CallbackInfo ci) {
        ((AsmMixin) (Object) Asm.INSTANCE).add(new BookPacketTransformer());
    }
}
