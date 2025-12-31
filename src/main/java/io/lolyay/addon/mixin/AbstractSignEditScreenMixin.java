package io.lolyay.addon.mixin;

import io.lolyay.addon.modules.dupes.TradeDupe;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
    @Final
    @Shadow
    protected SignBlockEntity blockEntity;

    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (Modules.get().isActive(TradeDupe.class)) {

            this.addDrawableChild(ButtonWidget.builder(Text.literal("AxTrade Dupe"), (button) -> {
                ChatUtils.info("Sending Packets...");

                mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(this.blockEntity.getPos(), true, "1", "", "", ""));
                mc.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("a"));

            }).dimensions(10 , 10, 100, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("LoverFella Dupe"), (button) -> {
                ChatUtils.info("Sending Packets...");

                mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(this.blockEntity.getPos(), true, "e", "", "", ""));
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));

            }).dimensions(10, 40, 100, 20).build());
        }
    }
}
