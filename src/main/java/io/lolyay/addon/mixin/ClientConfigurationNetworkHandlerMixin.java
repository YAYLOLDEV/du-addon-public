package io.lolyay.addon.mixin;

import io.lolyay.addon.ChannelKeeper;
import io.lolyay.addon.VersionKeeper;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationNetworkHandler.class)
public abstract class ClientConfigurationNetworkHandlerMixin {
    @Inject(method = "onSelectKnownPacks", at = @At("HEAD"))
    private void onSelectKnownPacks(SelectKnownPacksS2CPacket packet, CallbackInfo ci) {
        VersionKeeper.gotPacket(packet);
        MeteorClient.EVENT_BUS.post(new PacketEvent.Receive(packet, null));
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void onCustomPayload(CustomPayload payload, CallbackInfo ci) {
        if(!ChannelKeeper.channels.contains(payload.getId().toString()))
            ChannelKeeper.channels.add(payload.getId().toString());
    }
}
