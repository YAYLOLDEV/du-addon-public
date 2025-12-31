package io.lolyay.addon.commands.clickslot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
               .then(argument("action", new ClickSlotArgumentType())
               .executes((context) -> {
                   int slot = context.getArgument("slot", Integer.class);
                   int button = context.getArgument("button", Integer.class);
                   SlotActionTypeI action = context.getArgument("action", SlotActionTypeI.class);
                   SlotActionType actionType = action.toActionType();
                   int syncId = mc.currentScreen instanceof GenericContainerScreen screen ? screen.getScreenHandler().syncId : 0;
                   mc.interactionManager.clickSlot(syncId, slot, button, actionType, mc.player);
                   return SINGLE_SUCCESS;
               }))));
    }
}
