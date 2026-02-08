package io.lolyay.addon.mixin;

import io.lolyay.addon.ChannelKeeper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void onCustomPayload(CustomPayload payload, CallbackInfo ci) {
        if(!ChannelKeeper.channels.contains(payload.getId().toString()))
            ChannelKeeper.channels.add(payload.getId().toString());
    }


}
