package io.lolyay.addon.dupedb;

import com.google.gson.Gson;
import io.lolyay.addon.DupersUnitedPublicAddon;
import io.lolyay.addon.dupedb.dao.DupeDBLogin;
import lombok.Getter;
import lombok.SneakyThrows;
import meteordevelopment.meteorclient.utils.network.Http;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class DupeDBLoginManager {
    //                                         30 Days
    private final static long MAX_TTL_TOKEN = (long) (24 * 60 * 60 * 1000) * 30;
    @Getter
    private DupeDBLogin apiKey = null;

    public boolean init() {
        if (apiKey != null)
            return true;
        CompletableFuture<Boolean> cf = new CompletableFuture<>();

        if (!tryLoadSessionId())
            DupeDBOauthLogin.getCode().thenAcceptAsync(codeAndState -> {
                DupeDBLogin cookieData = new DupeDBLogin(codeAndState, System.currentTimeMillis() + MAX_TTL_TOKEN);
                String testResp = Http.get("https://dupedb.net/api/auth/me")
                    .header("X-App-Token", cookieData.Tkey())
                    .sendString();
                if (testResp == null || testResp.contains("Unauthorized")) {
                    cf.complete(false);
                    return;
                }
                apiKey = cookieData;
                cf.complete(true);
                save();
            });
        else return true;
        return cf.join();
    }

    @SneakyThrows
    public void save() {
        File f = new File("dupedb-do-not-share.ddb");
        if (!f.exists())
            f.createNewFile();
        Files.writeString(f.toPath(), new Gson().toJson(apiKey));
    }

    @SneakyThrows
    private boolean tryLoadSessionId() {
        File f = new File("dupedb-do-not-share.ddb");
        if (!f.exists())
            return false;
        String str = Files.readString(f.toPath());
        if (str.isBlank())
            return false;
        DupeDBLogin sess = new Gson().fromJson(str, DupeDBLogin.class);
        if (sess == null) return false;
        if (sess.expTimestamp() < System.currentTimeMillis()) {
            DupersUnitedPublicAddon.LOG.warn("Saved DupeDB Session ID is expired, refreshing");
            return false;
        }
        apiKey = sess;
        return true;
    }

}
