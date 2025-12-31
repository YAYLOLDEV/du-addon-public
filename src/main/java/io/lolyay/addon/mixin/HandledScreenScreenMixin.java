package io.lolyay.addon.mixin;

import com.mojang.serialization.DataResult;
import io.lolyay.addon.ChannelKeeper;
import io.lolyay.addon.modules.GuiSlotNbt;
import io.lolyay.addon.ui.EnterableTextField;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(HandledScreen.class)
public abstract class HandledScreenScreenMixin<T extends ScreenHandler> extends Screen {
    @Final
    @Shadow
    protected T handler;

    protected HandledScreenScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at=@At("TAIL"))
    public void init(CallbackInfo ci) {
        if(!Modules.get().isActive(GuiSlotNbt.class)) return;
        if(handler instanceof GenericContainerScreenHandler screenHandler) {
            TextFieldWidget widget = new EnterableTextField(textRenderer, 50, 20, Text.empty(),txt -> {
                try {
                    int x = Integer.parseInt(txt.getText());
                    NbtElement element = screenHandler.getInventory().getStack(x).toNbt(mc.player.getRegistryManager());
                    ChatUtils.sendMsg(NbtHelper.toPrettyPrintedText(element));
                    mc.keyboard.setClipboard(NbtHelper.toPrettyPrintedText(element).toString());
                } catch (NumberFormatException e) {
                    ChatUtils.error("Invalid slot ID: " + txt.getText());
                }
            });
            widget.setX(10);
            widget.setY(10);
            widget.setPlaceholder(Text.literal("Slot ID"));
            addDrawableChild(widget);
        }

    }
}
