package io.lolyay.addon;

import com.mojang.logging.LogUtils;
import io.lolyay.addon.commands.*;
import io.lolyay.addon.modules.AttributeSwap;
import io.lolyay.addon.modules.PacketDelay;
import io.lolyay.addon.modules.crashes.BundleCrash;
import io.lolyay.addon.modules.dupes.*;
import io.lolyay.addon.modules.settingsmodules.ForEachSettings;
import io.lolyay.addon.modules.settingsmodules.GuiMacros;
import io.lolyay.addon.modules.settingsmodules.GuiSlotNbt;
import lombok.SneakyThrows;
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
    @SneakyThrows
    public void onInitialize() {
        LOG.info("Initializing DupersUnited Public Addon");

   /*     Method add = Systems.class.getDeclaredMethod("add", System.class);
        add.setAccessible(true);
        add.invoke(null, new DupeDBApi());*/
        //FIXME Not fully implemented

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
        Modules.get().add(new ForEachSettings());

        //Exploits
        Modules.get().add(new AttributeSwap());
        Modules.get().add(new PacketDelay());

        //Crashes
        Modules.get().add(new BundleCrash());

        //Dupes
        Modules.get().add(new PaperBookDupe());
        Modules.get().add(new ShulkerDupe());
        Modules.get().add(new TradeDupe());
        Modules.get().add(new TridentDupe());
        Modules.get().add(new BundleDupe());
    }

    private void initCommands() {
        Commands.add(new ClickSlotCommand());
        Commands.add(new WaitCommand());
        Commands.add(new RepeatCommand());
        Commands.add(new RepeatDelayCommand());
        Commands.add(new ForEachPlayerCommand());
    }
}
