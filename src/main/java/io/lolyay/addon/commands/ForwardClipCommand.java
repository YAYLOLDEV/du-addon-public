package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.utils.clip.PaperClipTp;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

//25% chance to end up at bedrock (:
// This is due to the last Vclip (UP) failing sometimes, we may need to add an AutoClip System, to find the closest 2-block space to the target in a range of e.g., 3 blocks.
public class ForwardClipCommand extends Command {
    public ForwardClipCommand() {
        super("fclip", "Forward Clip Though Blocks. Can not clip you Into blocks (If youre not on singleplayer). 25% Chance to end up at bedrock!", "forwardclip");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            double blocks = context.getArgument("blocks", Double.class);

            Vec3d forward = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
            double startX = mc.player.getX();
            double startZ = mc.player.getZ();

            double targetX = startX + forward.x * blocks;
            double targetY = mc.player.getY();
            double targetZ = startZ + forward.z * blocks;

            PaperClipTp.tp(targetX, targetY, targetZ);
            return SINGLE_SUCCESS;
        }));
    }
}
