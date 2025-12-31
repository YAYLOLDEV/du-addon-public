package io.lolyay.addon.ui;

import io.lolyay.addon.ChannelKeeper;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class EnterableTextField extends TextFieldWidget {
    private final Consumer<EnterableTextField> onEnter;
    public EnterableTextField(TextRenderer textRenderer, int width, int height, Text text, Consumer<EnterableTextField> onEnter) {
        super(textRenderer, width, height, text);
        this.onEnter = onEnter;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 257 || scanCode == 28)
            onEnter.accept(this);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
