package io.lolyay.addon.dupedb.dao;

import javax.annotation.Nullable;

public record LatestActivityDAO(
    Activity activity) {

    public record Activity(
        String id,
        String name,
        String slug,
        String status,
        String dateSubmitted,
        String dateModified,
        String author,
        Integer authorUserId,
        @Nullable Object authorDisplayName,
        @Nullable Object authorCustomAvatar,
        String authorDiscordId,
        String authorDiscordAvatar
    ) {
    }
}
