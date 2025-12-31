package io.lolyay.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

public class HotbarScreenutils {
    public static int getAsServerID(int id){
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if(!(screen instanceof GenericContainerScreen)) return noScreenCalc(id);
        return ((GenericContainerScreen) screen).getScreenHandler().getInventory().size() + 27 + id;

    }
    private static int noScreenCalc(int id){
        return 36 + id;
    }
}
