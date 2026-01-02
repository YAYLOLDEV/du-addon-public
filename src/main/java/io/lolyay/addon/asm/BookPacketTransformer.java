package io.lolyay.addon.asm;

import meteordevelopment.meteorclient.asm.AsmTransformer;
import meteordevelopment.meteorclient.asm.Descriptor;
import meteordevelopment.meteorclient.asm.MethodInfo;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.objectweb.asm.Opcodes.LDC;

public class BookPacketTransformer extends AsmTransformer {

    // Use Intermediary names for runtime compatibility
    // class_2820 = BookUpdateC2SPacket
    private final MethodInfo targetMethod = new MethodInfo(
        "net/minecraft/class_2820",
        "<clinit>",
        new Descriptor("()V"),
        false
    );

    public BookPacketTransformer() {
        // Target name must be the intermediary class name
        super("net.minecraft.class_2820");
    }

    @Override
    public void transform(ClassNode klass) {
        // Meteor's getMethod helper
        MethodNode method = getMethod(klass, targetMethod);

        if (method == null) {
            // Debug: If <clinit> isn't found, print all methods to see what we are working with
            System.err.println("[DupersUnited] Failed to find <clinit> in " + klass.name);
            return;
        }

        AtomicBoolean modifiedPageLength = new AtomicBoolean(false);
        AtomicBoolean modifiedTitleLength = new AtomicBoolean(false);

        method.instructions.iterator().forEachRemaining(insn -> {
            if (insn.getOpcode() == LDC) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof Integer value) {
                    // MAX_PAGE_LENGTH
                    if (value == 1024) {
                        ldc.cst = 2048;
                        modifiedPageLength.set(true);
                    }
                    // MAX_TITLE_LENGTH
                    else if (value == 32) {
                        ldc.cst = 128; // Increased for extra safety
                        modifiedTitleLength.set(true);
                    }
                }
            }
        });

        if (modifiedPageLength.get() || modifiedTitleLength.get()) {
            System.out.println("[DupersUnited] Successfully patched BookUpdateC2SPacket");
        } else {
            System.err.println("[DupersUnited] Failed to find constants in <clinit>!");
        }
    }
}
