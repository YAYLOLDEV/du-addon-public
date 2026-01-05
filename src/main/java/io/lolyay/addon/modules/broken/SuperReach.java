package io.lolyay.addon.modules.broken;

import io.lolyay.addon.DupersUnitedPublicAddon;
import io.lolyay.addon.events.MouseButtonEvent;
import io.lolyay.addon.utils.PacketUtils;
import io.lolyay.addon.utils.RayCastUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.lolyay.addon.modules.broken.AntiSetServerPosition.waitingForDesyncPacket;
import static io.lolyay.addon.utils.PacketUtils.sendEntityHitPacket;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

// This only works 70% of the time, there are WAY BETTER WAYS of implementing this, if you have time, please rewrite this module
public class SuperReach extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    // HOLY SETTINGS
    private final Setting<Double> reach = sgGeneral.add(new DoubleSetting.Builder()
            .name("reach")
            .description("The distance to add to your block reach.")
            .sliderMax(1000)
            .defaultValue(100)
            .build());

    private final Setting<Double> stepDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("step-distance")
            .description("The distance between the PlayerPosition Packets.")
            .sliderMax(5)
            .sliderMin(0.1)
            .defaultValue(4)
            .build());

    private final Setting<Double> aimbotSize = sgGeneral.add(new DoubleSetting.Builder()
            .name("aimbot-range")
            .description("This allows you to hit entities in a radius around your crosshair")
            .sliderRange(1, 100)
            .min(1)
            .defaultValue(4)
            .build());

    private final Setting<Boolean> waitForDesyncPacket = sgGeneral.add(new BoolSetting.Builder()
            .name("wait-for-desync")
            .description(
                    "Should we wait for a Desync Packet? (This Disables Everything else Desync Related except the lower slider)")
            .defaultValue(false)
            .build());

    private final Setting<Integer> desyncWaitMs = sgGeneral.add(new IntSetting.Builder()
            .name("desync-wait-ms")
            .description("How long to wait for a Desync Packet")
            .sliderMax(1000)
            .sliderMin(1)
            .visible(waitForDesyncPacket::get)
            .defaultValue(100)
            .onChanged(v -> publicDesyncWaitTime = v)
            .build());

    // This is useless and never should be used.
    private final Setting<Integer> maxFixAttempts = sgGeneral.add(new IntSetting.Builder()
            .name("max-fix-attempts")
            .description("How often should we try Fixing the Desync (if there is one)")
            .sliderMax(30)
            .sliderMin(1)
            .visible(() -> !waitForDesyncPacket.get())
            .defaultValue(4)
            .build());

    private final Setting<Integer> heightFixOffset = sgGeneral.add(new IntSetting.Builder()
            .name("height-fix-offset")
            .description("How for down should we try to fix Heightoffset")
            .sliderMax(320)
            .sliderMin(1)
            .visible(() -> !waitForDesyncPacket.get())
            .defaultValue(100)
            .build());

    private final Setting<Integer> heightFixDelayMs = sgGeneral.add(new IntSetting.Builder()
            .name("height-fix-delay-ms")
            .description("How long to wait before we try to fix Heightoffset")
            .sliderMax(1000)
            .sliderMin(1)
            .visible(() -> !waitForDesyncPacket.get())
            .defaultValue(300)
            .build());

    private final Setting<Boolean> finalFix = sgGeneral.add(new BoolSetting.Builder()
            .name("final-desync-fix")
            .description("Should we try to fix a Desync for a last time (with a Delay) (doesnt include upper value)")
            .defaultValue(true)
            .visible(() -> !waitForDesyncPacket.get())
            .build());

    private final Setting<Integer> finalFixDelayTicks = sgGeneral.add(new IntSetting.Builder()
            .name("final-fix-delay-ticks")
            .description("How long to wait before fixing the final desync in Ticks")
            .defaultValue(2)
            .sliderMin(1)
            .sliderMax(60)
            .visible(() -> finalFix.get() && !waitForDesyncPacket.get())
            .build());
    // end of useless settings

    private Entity target;
    private Vec3d startPos;
    private Vec3d targetPos;

    private boolean returnNextTick;
    private boolean waitingForFinalFix;
    private int fixAttempts;
    private int ticksSinceDesync;

    public static Vec3d currentTargetPos;
    public static int publicDesyncWaitTime;
    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);

    public static SuperReach INSTANCE;

    public SuperReach() {
        super(DupersUnitedPublicAddon.CATEGORY, "super-reach", "Theoretically infinite reach. WARNING: This WILL flag you server side, is VERY UNSTABLE, and may get you kicked / Banned. AntiServerTp is recommended with this Module. (Else this probably wont work)");
        INSTANCE = this;
    }


    @EventHandler
    private void onLeftClick(MouseButtonEvent event) {
        if (mc.currentScreen != null)
            return;
        if (event.action != KeyAction.Press)
            return;
        if (event.button != GLFW_MOUSE_BUTTON_LEFT)
            return;

        boolean canAct = !returnNextTick && !waitingForFinalFix;
        if (!canAct && !waitForDesyncPacket.get())
            return;

        HitResult hit = RayCastUtils.raycastCrosshair(mc.getCameraEntity(), reach.get(), 1.0F);
        if (hit.getType() == HitResult.Type.MISS)
            return;

        if (hit instanceof EntityHitResult ehr) {
            attemptHit(ehr.getEntity().getEntityPos(), ehr.getEntity());
        } else {
            for (Entity e : getNearbyEntities(mc.world, hit.getPos())) {
                if (!e.isAttackable())
                    continue;
                attemptHit(e.getEntityPos(), e);
                break;
            }
        }
    }

    public void movePlayerSmoothly(Vec3d from, Vec3d target, Runnable onFinish) {
        ArrayList<Vec3d> steps = calculateSteps(from, target);
        if (steps.isEmpty()) {
            ChatUtils.error("Can't reach target position.");
            return;
        }

        List<Packet<?>> packets = new ArrayList<>();
        for (Vec3d step : steps) {
            packets.add(new PlayerMoveC2SPacket.PositionAndOnGround(
                    step.x, step.y, step.z, true, false));
        }

        PacketUtils.queuePackets(packets, onFinish);
    }

    private void attemptHit(Vec3d pos, Entity entity) {
        if (pos.distanceTo(mc.player.getEntityPos()) < aimbotSize.get())
            return;

        ArrayList<Vec3d> steps = calculateSteps(mc.player.getEntityPos(), pos);
        if (steps == null) {
            ChatUtils.error("Unreachable target.");
            return;
        }

        target = entity;
        startPos = mc.player.getEntityPos();
        targetPos = pos;
        currentTargetPos = pos;

        fixAttempts = 0;
        ticksSinceDesync = 0;
        waitingForFinalFix = false;

        moveAndReturn(startPos, targetPos);
    }

    private void moveAndReturn(Vec3d from, Vec3d to) {
        moveSmoothly(from, to, () -> {
            mc.player.setPosition(from);
            sendEntityHitPacket(target);
            target = null;

            returnNextTick = true;
        });
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (returnNextTick && fixAttempts < maxFixAttempts.get()) {
            currentTargetPos = startPos;
            moveSmoothly(targetPos, startPos, () -> {
            });
            scheduleHeightFix();
            fixAttempts++;
            waitingForDesyncPacket = true;
            return;
        }

        if (returnNextTick && fixAttempts >= maxFixAttempts.get() && !waitForDesyncPacket.get()) {
            returnNextTick = false;
            waitingForFinalFix = finalFix.get();
            return;
        }

        if (waitingForFinalFix && !waitForDesyncPacket.get()) {
            if (ticksSinceDesync++ >= finalFixDelayTicks.get()) {
                moveSmoothly(targetPos, startPos, () -> {
                });
                doHeightFix();
                waitingForFinalFix = false;
            }
        }
    }


    public void moveSmoothly(Vec3d from, Vec3d to, Runnable callback) {
        ArrayList<Vec3d> steps = calculateSteps(from, to);
        if (steps == null || steps.isEmpty()) {
            if (callback != null)
                callback.run();
            return;
        }

        List<Packet<?>> packets = new ArrayList<>();
        for (Vec3d step : steps) {
            packets.add(new PlayerMoveC2SPacket.PositionAndOnGround(
                    step.x, step.y, step.z, false, false));
        }

        PacketUtils.queuePackets(packets, callback);
    }

    private ArrayList<Vec3d> calculateSteps(Vec3d from, Vec3d to) {
        ArrayList<Vec3d> steps = new ArrayList<>();
        Vec3d current = from;

        while (current.distanceTo(to) > 0.1) {
            Vec3d dir = to.subtract(current).normalize();
            double dist = current.distanceTo(to);
            current = dist <= stepDistance.get()
                    ? to
                    : current.add(dir.multiply(stepDistance.get()));
            steps.add(current);
        }

        return steps;
    }


    private void scheduleHeightFix() {
        EXECUTOR.schedule(this::doHeightFix, heightFixDelayMs.get(), TimeUnit.MILLISECONDS);
    }

    private void doHeightFix() {
        if (waitForDesyncPacket.get())
            return;

        Vec3d down = startPos.subtract(0, heightFixOffset.get(), 0);
        moveSmoothly(down, startPos, () -> {
        });
    }


    private List<Entity> getNearbyEntities(World world, Vec3d pos) {
        Box box = new Box(
                pos.x - aimbotSize.get(), pos.y - aimbotSize.get(), pos.z - aimbotSize.get(),
                pos.x + aimbotSize.get(), pos.y + aimbotSize.get(), pos.z + aimbotSize.get());
        return world.getOtherEntities(null, box);
    }
}
