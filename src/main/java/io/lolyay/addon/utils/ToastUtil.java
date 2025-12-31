package io.lolyay.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToastUtil {
    public static void showToast(Text title,@Nullable Text message) {
        MinecraftClient.getInstance().getToastManager().add(
            new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, title,message));
    }

    public static void showToast(Text title) {
        showToast(title,null);
    }

    public static void showToast(String title) {
        showToast(Text.literal(title),null);
    }

    public static void showToast(String title,@NotNull String message) {
        showToast(Text.literal(title), Text.literal(message));
    }
}

