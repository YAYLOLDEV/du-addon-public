package io.lolyay.addon.dupedb.dao;

public record VersionDAO(
    Integer commitcount,
    String lastcommithash,
    String lastcommitmessage,
    String lastcommitdate,
    String branch,
    String authorname,
    String githubusername
) {
}
