package io.lolyay.addon.mixin;

import io.lolyay.addon.dupedb.gui.DupeDBGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addDupeDbButton(CallbackInfo ci) {
        addDrawableChild(new ButtonWidget.Builder(Text.literal("Dupes"), ignored -> MinecraftClient.getInstance().setScreen(new DupeDBGui()))
            .position(5, height / 2 + 20 - 140)
            .build()
        );
    }
}
