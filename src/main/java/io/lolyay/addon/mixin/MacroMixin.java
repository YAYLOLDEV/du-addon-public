package io.lolyay.addon.mixin;

import io.lolyay.addon.modules.GuiMacros;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Macro.class)
public abstract class MacroMixin {

    @Redirect(method = "onAction(ZII)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", opcode = Opcodes.GETFIELD))
    private Screen redirect(MinecraftClient mc) {
        return Modules.get().isActive(GuiMacros.class) ? null : mc.currentScreen;
    }
}
