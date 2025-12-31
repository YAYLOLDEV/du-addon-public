package io.lolyay.addon.mixin;

import io.lolyay.addon.ChannelKeeper;
import io.lolyay.addon.VersionKeeper;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.commands.ServerCommand;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ServerCommand.class)
public abstract class ServerCommandMixin extends Command {
    public ServerCommandMixin(String name, String description, String... aliases) {
        super(name, description, aliases);
    }

    @Inject(method = "basicInfo", at=@At("TAIL"), remap = false)
    private void basicInfo(CallbackInfo ci) {
        if(mc.getCurrentServerEntry() != null) {
            info("Advertised Version: %s", mc.getCurrentServerEntry().version.getString());
            info("Actual Version: %s%s", Formatting.RED, VersionKeeper.version);
            if (!VersionKeeper.resourcePacks.isEmpty()) {
                info("Resource Packs: %s", String.join(", ", VersionKeeper.resourcePacks));
            }
            info("Detected Plugin Channels: %s", String.join(", ", ChannelKeeper.channels));
        }
    }

}
