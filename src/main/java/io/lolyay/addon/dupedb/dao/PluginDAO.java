package io.lolyay.addon.dupedb.dao;

import java.util.List;

public record PluginDAO(
    String name,
    List<String> versions
) {
}
