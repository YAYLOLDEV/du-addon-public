package io.lolyay.addon;


import com.mojang.logging.LogUtils;
import io.lolyay.addon.commands.*;
import io.lolyay.addon.commands.clickslot.ClickSlotCommand;
import io.lolyay.addon.modules.*;
import io.lolyay.addon.modules.crashes.BundleCrash;
import io.lolyay.addon.modules.dupes.PaperBookDupe;
import io.lolyay.addon.modules.dupes.ShulkerDupe;
import io.lolyay.addon.modules.dupes.TradeDupe;
import io.lolyay.addon.modules.settingsmodules.ForEachSettings;
import io.lolyay.addon.modules.settingsmodules.GuiMacros;
import io.lolyay.addon.modules.settingsmodules.GuiSlotNbt;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class DupersUnitedPublicAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("DupersUnited");

    @Override
    public void onInitialize() {
        LOG.info("Initializing DupersUnited Public Addon");

        initModules();
        initCommands();
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "io.lolyay.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("YAYLOLDEV", "du-addon-public");
    }


    private void initModules() {
        //Util
        Modules.get().add(new GuiMacros());
        Modules.get().add(new GuiSlotNbt());
        Modules.get().add(new SuperReach());
        Modules.get().add(new ForEachSettings());

        //Exploits
        Modules.get().add(new AttributeSwap());
        Modules.get().add(new PacketDelay());
        Modules.get().add(new AntiSetServerPosition());

        //Crashes
        Modules.get().add(new BundleCrash());

        //Dupes
        Modules.get().add(new PaperBookDupe());
        Modules.get().add(new ShulkerDupe());
        Modules.get().add(new TradeDupe());
    }

    private void initCommands() {
        Commands.add(new ForwardClipCommand());
        Commands.add(new TpCommand());
        Commands.add(new ClickSlotCommand());
        Commands.add(new WaitCommand());
        Commands.add(new RepeatCommand());
        Commands.add(new ForEachPlayer());
    }
}
