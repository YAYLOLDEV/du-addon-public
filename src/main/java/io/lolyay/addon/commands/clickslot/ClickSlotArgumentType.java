package io.lolyay.addon.commands.clickslot;

import net.minecraft.command.argument.EnumArgumentType;

public class ClickSlotArgumentType extends EnumArgumentType<SlotActionTypeI> {
    public ClickSlotArgumentType() {
        super(SlotActionTypeI.codec(), SlotActionTypeI::values);
    }
}
