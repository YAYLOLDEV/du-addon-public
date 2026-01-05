package io.lolyay.addon.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.lolyay.addon.utils.clip.PaperClipTp;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class TpCommand extends Command {


    public TpCommand() {
        super("tp", "Teleport. 25% Chance to end up at bedrock!", "teleport");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("x", DoubleArgumentType.doubleArg())
               .then(argument("y", DoubleArgumentType.doubleArg())
               .then(argument("z", DoubleArgumentType.doubleArg())
               .executes(context -> {

                   double targetX = context.getArgument("x", Double.class);
                   double targetY = context.getArgument("y", Double.class);
                   double targetZ = context.getArgument("z", Double.class);

                   PaperClipTp.tp(targetX, targetY, targetZ);

                   return SINGLE_SUCCESS;
               }))));
    }

}
