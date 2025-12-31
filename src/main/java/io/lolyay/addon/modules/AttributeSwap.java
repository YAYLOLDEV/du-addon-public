package io.lolyay.addon.modules;

import io.lolyay.addon.DupersUnitedPublicAddon;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;

public class AttributeSwap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Boolean> shouldBreakShield = sgGeneral.add(new BoolSetting
        .Builder()
        .name("break-shield")
        .description("Swap to an axe to try to disable the shield of someone.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> slot = sgGeneral.add(new IntSetting
        .Builder()
        .name("target-slot")
        .description("The hotbar slot to swap to.")
        .sliderRange(1, 9)
        .min(1)
        .defaultValue(1)
        .build()
    );

    private final SettingGroup sgSwapBack = settings.createGroup("Swap Back");

    private final Setting<Boolean> swapBack = sgSwapBack.add(new BoolSetting
        .Builder()
        .name("swap-back-enabled")
        .description("Swap back to the original slot after a delay.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> delay = sgSwapBack.add(new IntSetting
        .Builder()
        .name("swap-back-delay")
        .description("Delay in ticks before swapping back.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 20)
        .visible(swapBack::get)
        .build()
    );

    private int prevSlot = -1;
    private int remainingSwapbackDelay = 0;

    public AttributeSwap() {
        super(DupersUnitedPublicAddon.CATEGORY, "attribute-swap", "Swaps attributes of with the target slot on attack to eg preseve item damage.");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (swapBack.get()) {
            prevSlot = mc.player.getInventory().selectedSlot;
        }
        if (shouldBreakShield.get()) {
            if (event.entity instanceof PlayerEntity player) {
                if (player.isBlocking()) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.getInventory().main.get(i);
                        if (stack.getItem() instanceof AxeItem) {
                            InvUtils.swap(i, false);
                            break;
                        }
                    }
                }
            }
        } else // dont break shield
            InvUtils.swap(slot.get() - 1, false);

        if (swapBack.get() && prevSlot != -1) {
            remainingSwapbackDelay = delay.get();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (remainingSwapbackDelay > 0) {
            remainingSwapbackDelay--;
            if (remainingSwapbackDelay == 0 && prevSlot != -1) {
                InvUtils.swap(prevSlot, false);
                prevSlot = -1;
            }
        }
    }
}
