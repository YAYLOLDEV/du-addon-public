package io.lolyay.addon.dupedb;

import io.lolyay.addon.DupersUnitedPublicAddon;
import io.lolyay.addon.dupedb.dao.MeDAO;
import lombok.Getter;
import meteordevelopment.meteorclient.systems.System;

public class DupeDBApi extends System<DupeDBApi> {
    private final DupeDBLoginManager dupeDBDiscordLogin;
    @Getter
    private DupeDBRequestor requestor;

    public DupeDBApi() {
        super("DupeDB-Integration");
        dupeDBDiscordLogin = new DupeDBLoginManager();
    }

    @Override
    public void init() {
        if (!dupeDBDiscordLogin.init()) {
            throw new RuntimeException("Couldnt Login to DupeDB");
        }
        requestor = new DupeDBRequestor(dupeDBDiscordLogin.getApiKey().Tkey());
        MeDAO me = requestor.getMe();
        DupersUnitedPublicAddon.LOG.info("Logged in as {} (ID: {}). Role: {}", me.user().discordUsername(), me.user().id(), me.user().role());
    }

}
