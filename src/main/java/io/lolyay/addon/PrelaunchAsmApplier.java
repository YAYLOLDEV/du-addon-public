package io.lolyay.addon;

import io.lolyay.addon.asm.BookPacketTransformer;
import meteordevelopment.meteorclient.asm.Asm;
import meteordevelopment.meteorclient.asm.AsmTransformer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.lang.reflect.Method;

public class PrelaunchAsmApplier implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        // 1. Initialize Meteor's ASM system
        Asm.init();

        try {
            Method addMethod = Asm.class.getDeclaredMethod("add", AsmTransformer.class);

            addMethod.setAccessible(true);

            addMethod.invoke(Asm.INSTANCE, new BookPacketTransformer());

            System.out.println("[DupersUnited] Successfully injected ASM Transformer via Reflection.");
        } catch (NoSuchMethodException e) {
            System.err.println("[DupersUnited] Failed to find 'add' method in Asm class!");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[DupersUnited] Failed to inject ASM Transformer!");
            e.printStackTrace();
        }
    }
}
