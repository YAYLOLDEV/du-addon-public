package io.lolyay.addon.commands.clickslot;

import com.mojang.serialization.Codec;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.StringIdentifiable;

public enum SlotActionTypeI implements StringIdentifiable {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL;

    @Override
    public String asString() {
        return name();
    }

    public static Codec<SlotActionTypeI> codec() {
        return StringIdentifiable.createCodec(SlotActionTypeI::values, (x) -> byName(x).name().toUpperCase());
    }

    public static SlotActionTypeI byName(String name) {
        return valueOf(name.toUpperCase());
    }

    public SlotActionType toActionType(){
        return SlotActionType.valueOf(name());
    }
}
