package io.lolyay.addon.modules.dupes;

import io.lolyay.addon.DupersUnitedPublicAddon;
import io.lolyay.addon.utils.PacketUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class PaperBookDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Boolean> dropItems = sgGeneral.add(new BoolSetting.Builder()
        .name("Drop Inventory")
        .description("Drops your inventory before duping, If you just want to kick players around you, turn this off.")
        .defaultValue(true)
        .build()
    );

    public PaperBookDupe() {
        super(DupersUnitedPublicAddon.CATEGORY, "book-dupe", "Dupes your inventory on Paper. Reconnect before duping to save your inventory. Only works on servers under 1.21.1");

    }
    @Override
    public void onActivate(){
        if(mc.player == null || mc.player.getMainHandStack() == null){
            toggle();
            return;
        }
        ItemStack currentItem = mc.player.getMainHandStack();
        if(currentItem.isEmpty() || currentItem.getItem() == null || !currentItem.getRegistryEntry().matchesId(Identifier.of("minecraft:writable_book"))) {
            ChatUtils.error("You need to be holding a Writable Book!");
            toggle();
            return;
        }

        if(dropItems.get()) dropItems();

        PacketUtils.sendEditBookPacket(mc.player.getInventory().getSlotWithStack(currentItem), List.of("DupersUnited?"),"NeedToHave42CharsInHereHAHA123456789012345");
        mc.disconnect(new DisconnectedScreen(mc.currentScreen, Text.of("Duped?"),Text.of("Hopefully")));
        toggle();
    }

    private void dropItems(){
        if(!dropItems.get()) return;
        for (ItemStack item : mc.player.getInventory().main) {
            if(item.isEmpty() || item.getItem() == null) continue;
            InvUtils.drop().slot(mc.player.getInventory().getSlotWithStack(item));
        }
    }

}
