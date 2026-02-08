package io.lolyay.addon.modules.dupes;

import io.lolyay.addon.DupersUnitedPublicAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;

public class ShulkerDupe extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public ShulkerDupe() {
        super(DupersUnitedPublicAddon.CATEGORY, "shulker-dupe", "Works in 1.19 and below. Patched on Paper.");
    }

    private final Setting<Boolean> usePickaxe = sgGeneral.add(new BoolSetting.Builder()
        .name("Use Pickaxe")
        .description("Uses Pickaxe when breaking shulker.")
        .defaultValue(true)
        .build()
    );
    public static boolean shouldDupe;
    public static boolean shouldDupeAll;
    private boolean wasTimerOn = false;

    @Override
    public void onActivate() {
        wasTimerOn = false;
        shouldDupeAll = false;
        shouldDupe = false;
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof ShulkerBoxScreen) {
            shouldDupeAll = false;
            shouldDupe = false;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (shouldDupe | shouldDupeAll) {
            if (Modules.get().get(Timer.class).isActive()) {
                wasTimerOn = true;
                Modules.get().get(Timer.class).toggle();
            }
            for (int i = 0; i < 8; i++) {
                if (usePickaxe.get() && (mc.player.getInventory().getStack(0).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(1).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(2).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(3).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(4).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(5).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(6).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(7).isIn(ItemTags.PICKAXES) || mc.player.getInventory().getStack(8).isIn(ItemTags.PICKAXES)) && !(mc.player.getMainHandStack().isIn(ItemTags.PICKAXES))) {
                    mc.player.getInventory().setSelectedSlot(mc.player.getInventory().selectedSlot + 1);
                    if (mc.player.getInventory().selectedSlot > 8) mc.player.getInventory().setSelectedSlot(0);
                }
            }
        } else {
            if (!Modules.get().get(Timer.class).isActive() && wasTimerOn) {
                wasTimerOn = false;
                Modules.get().get(Timer.class).toggle();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen instanceof ShulkerBoxScreen && mc.player != null && mc.interactionManager != null) {
            HitResult wow = mc.crosshairTarget;
            BlockHitResult a = (BlockHitResult) wow;
            if (shouldDupe | shouldDupeAll) {
                mc.interactionManager.updateBlockBreakingProgress(a.getBlockPos(), Direction.DOWN);
            }
        }
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Sent event) {
        if (event.packet instanceof PlayerActionC2SPacket && mc.interactionManager != null && mc.player != null) {
            if (shouldDupeAll) {
                if (((PlayerActionC2SPacket) event.packet).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                    for (int i = 0; i < 27; i++) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    }
                    shouldDupeAll = false;
                }
            } else if (shouldDupe) {
                if (((PlayerActionC2SPacket) event.packet).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                    shouldDupe = false;
                }
            }
        }
    }

    public static void dupe(ButtonWidget ignored){
        ShulkerDupe.shouldDupe = true;
    }

    public static void dupeAll(ButtonWidget ignored) {
        ShulkerDupe.shouldDupeAll = true;
    }
}
