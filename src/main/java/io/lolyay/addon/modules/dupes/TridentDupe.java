package io.lolyay.addon.modules.dupes;

import io.lolyay.addon.DupersUnitedPublicAddon;
import io.lolyay.addon.utils.timer.TickTimer;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

// Inspired by https://github.com/Killetx/TridentDupe/blob/main/src/main/java/com/example/addon/modules/TridentDupe.java
// Credit to Killet, Laztec & Ionar :3
public class TridentDupe extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> chargeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("charge-delay")
        .description("Delay between trident charge and throw. Increase if experiencing issues/lag.")
        .defaultValue(5)
        .build()
    );
    private final Setting<Double> loopDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("loop-delay")
        .description("Delay between dupe attempts (in seconds).")
        .defaultValue(0.1)
        .min(0)
        .sliderMax(1)
        .build());
    private final Setting<Boolean> dropTridents = sgGeneral.add(new BoolSetting.Builder()
        .name("drop-tridents")
        .description("Drops tridents in your last hotbar slot.")
        .defaultValue(true)
        .build()
    );
    private final TickTimer timer = TickTimer.getInstance();
    private boolean cancel = false;

    public TridentDupe() {
        super(DupersUnitedPublicAddon.CATEGORY, "trident-dupe", "Works in 1.13 and above. Patched on Paper. Credit to Killet, Laztec & Ionar :3");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) {
            toggle();
            return;
        }
        timer.clear();
        cancel = false;
        dupe();
    }

    @Override
    public void onDeactivate() {
        timer.clear();
        cancel = false;
    }

    private void dupe() {
        if (!isActive() || mc.player == null) return;

        // 1. Setup: Find tool and start charging
        int toolSlot = findBestTool();
        mc.player.getInventory().setSelectedSlot(toolSlot);

        // Start using item
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        cancel = true;

        // 2. Schedule the Release phase
        int chargeTicks = (int) (chargeDelay.get() * 20);

        timer.schedule(() -> {
            if (!isActive()) return;

            cancel = false; // Allow packets for inventory manipulation
            // Perform the dupe swap
            clickSlot(3, SlotActionType.SWAP);

            // Send release packet directly to server
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN, 0
            ));

            if (dropTridents.get()) clickSlot(44, SlotActionType.THROW);

            cancel = true; // Re-enable packet blocking

            // 3. Schedule next loop
            int nextLoopTicks = (int) (loopDelay.get() * 20);
            timer.schedule(this::dupe, nextLoopTicks);

        }, chargeTicks);
    }

    private int findBestTool() {
        int bestSlot = 0;
        int lowestDamage = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.TRIDENT) || stack.isOf(Items.BOW)) {
                if (stack.getDamage() < lowestDamage) {
                    lowestDamage = stack.getDamage();
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    private void clickSlot(int slot, SlotActionType type) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, type, mc.player);
    }

    @EventHandler()
    private void onSendPacket(PacketEvent.Send event) {
        if (!cancel) return;

        if (event.packet instanceof ClickSlotC2SPacket || event.packet instanceof PlayerActionC2SPacket) {
            event.cancel();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        toggle();
    }

}
