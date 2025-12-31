package io.lolyay.addon.mixin;

import io.lolyay.addon.modules.dupes.ShulkerDupe;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBoxScreen.class)
public abstract class ShulkerBoxScreenMixin extends Screen {
    public ShulkerBoxScreenMixin(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        if (Modules.get().isActive(ShulkerDupe.class)) {
            addDrawableChild(new ButtonWidget.Builder(Text.literal("Dupe"), ShulkerDupe::dupe)
                .position(240, height / 2 + 35 - 140)
                .size(50, 15)
                .build()
            );
            addDrawableChild(new ButtonWidget.Builder(Text.literal("Dupe All"), ShulkerDupe::dupeAll)
                .position(295, height / 2 + 35 - 140)
                .size(50, 15)
                .build()
            );

        }
    }
}
