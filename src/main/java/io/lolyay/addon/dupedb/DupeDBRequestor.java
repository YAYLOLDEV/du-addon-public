package io.lolyay.addon.dupedb;

import com.google.gson.reflect.TypeToken;
import io.lolyay.addon.dupedb.dao.ExploitListDao;
import io.lolyay.addon.dupedb.dao.MeDAO;
import meteordevelopment.meteorclient.utils.network.Http;
import org.jetbrains.annotations.Nullable;

public record DupeDBRequestor(String apiKey) {
    private final static String API_URL = "https://dupedb.net/api/";

    public MeDAO getMe() {
        return doRequest(MeDAO.class, createApiUrl("auth/me"), null);
    }

    public ExploitListDao getExploits() {
        return doRequest(ExploitListDao.class, createApiUrl("exploits"), null);
    }

    private String createApiUrl(String sub) {
        return API_URL + sub;
    }

    private <T> T doRequest(Class<T> type, String apiUrl, @Nullable Object json) {
        Http.Request request = json == null ? Http.get(apiUrl) : Http.post(apiUrl);
        request.header("X-App-Token", apiKey);
        if (json != null)
            request.bodyJson(json);
        return request.sendJson(type);
    }


    private <T> T doRequest(TypeToken<T> typeToken, String apiUrl, @Nullable Object json) {
        Http.Request request = json == null ? Http.get(apiUrl) : Http.post(apiUrl);
        request.header("X-App-Token", apiKey);
        if (json != null)
            request.bodyJson(json);
        return request.sendJson(typeToken.getType());
    }
}
