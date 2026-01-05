package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.arguments.EnumArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.slot.SlotActionType;

public class ClickSlotCommand extends Command {

    public ClickSlotCommand() {
        super("clickslot", "Clicks a slot!", "cs", "cslot");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("slot", IntegerArgumentType.integer())
               .then(argument("button", IntegerArgumentType.integer())
               .then(argument("action", EnumArgumentType.enumArgument(SlotActionType.PICKUP))
               .executes((context) -> {
                   assert mc.interactionManager != null; // Again, you shouldn't be able to run commands if you're not in a game
                   int slot = context.getArgument("slot", Integer.class);
                   int button = context.getArgument("button", Integer.class);
                   SlotActionType action = context.getArgument("action", SlotActionType.class);
                   int syncId = mc.currentScreen instanceof GenericContainerScreen screen ? screen.getScreenHandler().syncId : 0;
                   mc.interactionManager.clickSlot(syncId, slot, button, action, mc.player);
                   return SINGLE_SUCCESS;
               }))));
    }
}
