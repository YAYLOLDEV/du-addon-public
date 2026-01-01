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
    private final MethodInfo targetMethod = new MethodInfo(
        "net/minecraft/network/packet/c2s/play/BookUpdateC2SPacket",  // owner
        "<clinit>",                                                   // name
        new Descriptor("()V"),                                        // desc
        false                                                         // isInterface
    );

    public BookPacketTransformer() {
        super("net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket");
    }

    @Override
    public void transform(ClassNode klass) {
        MethodNode method = getMethod(klass, targetMethod);
        if (method == null) {
            System.err.println("Failed to find method: " + targetMethod);
            return;
        }

        AtomicBoolean modifiedPageLength = new AtomicBoolean(false);
        AtomicBoolean modifiedTitleLength = new AtomicBoolean(false);

        method.instructions.iterator().forEachRemaining(insn -> {
            if (insn.getOpcode() == LDC) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof Integer) {
                    int value = (Integer) ldc.cst;
                    if (value == 1024) {
                        System.out.println("Modified page length from 1024 to 2048 in BookUpdateC2SPacket");
                        ldc.cst = 2048;
                        modifiedPageLength.set(true);
                    } else if (value == 32) {
                        System.out.println("Modified title length from 32 to 64 in BookUpdateC2SPacket");
                        ldc.cst = 64;
                        modifiedTitleLength.set(true);
                    }
                }
            }
        });

        if (!modifiedPageLength.get()) {
            System.err.println("Warning: Failed to modify page length in BookUpdateC2SPacket");
        }
        if (!modifiedTitleLength.get()) {
            System.err.println("Warning: Failed to modify title length in BookUpdateC2SPacket");
        }
    }
}
