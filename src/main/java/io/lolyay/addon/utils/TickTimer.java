package io.lolyay.addon.utils;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.orbit.EventHandler;

import java.util.LinkedList;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.EVENT_BUS;

public class TickTimer {
    private static final TickTimer INSTANCE = new TickTimer();

    private final Queue<ScheduledTask> scheduledTasks = new LinkedList<>();
    private int ticksUntilNext = 0;

    private TickTimer() {
    }

    public static TickTimer getInstance() {
        return INSTANCE;
    }

    @PostInit
    public static void init() {
        EVENT_BUS.subscribe(INSTANCE);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tick();
    }

    public void schedule(Runnable runnable, int delayTicks) {
        if (runnable != null) {
            scheduledTasks.add(new ScheduledTask(runnable, Math.max(0, delayTicks)));
        }
    }


    public void schedule(Runnable runnable) {
        schedule(runnable, 0);
    }


    private void tick() {
        if (scheduledTasks.isEmpty())
            return;

        if (ticksUntilNext > 0) {
            ticksUntilNext--;
            return;
        }

        ScheduledTask task = scheduledTasks.poll();
        if (task != null) {
            task.runnable.run();

            ScheduledTask next = scheduledTasks.peek();
            if (next != null) {
                ticksUntilNext = next.delayTicks;
            }
        }
    }


    public boolean hasPendingTasks() {
        return !scheduledTasks.isEmpty();
    }


    public int pendingTaskCount() {
        return scheduledTasks.size();
    }


    public void clear() {
        scheduledTasks.clear();
        ticksUntilNext = 0;
    }

    private record ScheduledTask(Runnable runnable, int delayTicks) {
    }
}
