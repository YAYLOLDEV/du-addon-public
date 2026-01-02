package io.lolyay.addon.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class EnterableTextField extends TextFieldWidget {
    // I'm SO surprised that the default TextFieldWidget doesn't have onEnter
    private final Consumer<EnterableTextField> onEnter;
    public EnterableTextField(TextRenderer textRenderer, int width, int height, Text text, Consumer<EnterableTextField> onEnter, Text placeHolder, int x, int y) {
        super(textRenderer, width, height, text);
        this.onEnter = onEnter;
        if(placeHolder != null)
            setPlaceholder(placeHolder);
        if(x != -1)
            setX(x);
        if(y != -1)
            setY(y);

    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        int scanCode = input.scancode();
        if((keyCode == 257 || scanCode == 28) && onEnter != null)
            onEnter.accept(this);
        return super.keyPressed(input);
    }

    public static Builder builder(TextRenderer textRenderer){
        return new Builder(textRenderer);
    }


    public static class Builder {
        private final TextRenderer textRenderer;
        private int width = -1;
        private int height = -1;
        private int x = -1;
        private int y = -1;
        private Consumer<EnterableTextField> onEnter = null;
        private Text placeholder = Text.empty();

        public Builder(TextRenderer textRenderer) {
            this.textRenderer = textRenderer;
        }

        public Builder placeholder(Text placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder onEnter(Consumer<EnterableTextField> onEnter) {
            this.onEnter = onEnter;
            return this;
        }

        public Builder dimensions(int width, int height, int x, int y) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public EnterableTextField build() {
            if(width == -1 || height == -1)
                throw new IllegalStateException("Width and height must be set before building EnterableTextField");
            return new EnterableTextField(textRenderer, width, height, Text.empty(), onEnter, placeholder, x ,y);
        }
    }
}
